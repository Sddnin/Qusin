package com.wangzan101.qusin.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleStartEffect
import com.wangzan101.qusin.R
import com.wangzan101.qusin.ui.components.BarIconButton
import com.wangzan101.qusin.ui.components.QusinButton
import com.wangzan101.qusin.ui.components.ButtonKind
import com.wangzan101.qusin.ui.components.ButtonSize
import com.wangzan101.qusin.ui.theme.QusinIcons
import com.wangzan101.qusin.ui.theme.QusinShapes
import com.wangzan101.qusin.ui.theme.QusinSpace
import com.wangzan101.qusin.ui.theme.QusinTheme
import com.wangzan101.qusin.ui.theme.QusinType
import com.wangzan101.qusin.voice.SpeechEvent
import com.wangzan101.qusin.voice.SpeechSession
import com.wangzan101.qusin.voice.SpeechEngineSpec
import com.wangzan101.qusin.voice.VoiceSource
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect

private enum class QuickVoicePhase { Ready, Permission, Listening, Processing, Review, Empty, Error }

/** One-shot voice entry. Recording only begins after the visible start button is pressed. */
@Composable
fun QuickVoiceScreen(
    voice: VoiceSource,
    onBack: () -> Unit,
    onOpenChat: () -> Unit,
    onPickSpeech: () -> Unit,
    onSubmit: suspend (String) -> Boolean,
) {
    val context = LocalContext.current
    val c = QusinTheme.colors
    val scope = rememberCoroutineScope()
    var phase by rememberSaveable { mutableStateOf(QuickVoicePhase.Ready) }
    var transcript by rememberSaveable { mutableStateOf("") }
    var message by rememberSaveable { mutableStateOf("") }
    var session by remember { mutableStateOf<SpeechSession?>(null) }
    var eventsJob by remember { mutableStateOf<Job?>(null) }
    var elapsedMs by rememberSaveable { mutableLongStateOf(0L) }
    var submitting by remember { mutableStateOf(false) }
    var permissionDenied by rememberSaveable { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        permissionDenied = !granted
        // Permission grant deliberately does not start the microphone. The user must press Start again.
        phase = if (granted) QuickVoicePhase.Ready else QuickVoicePhase.Permission
    }

    LifecycleStartEffect(phase) {
        if (phase == QuickVoicePhase.Permission &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        ) {
            permissionDenied = false
            phase = QuickVoicePhase.Ready
        }
        onStopOrDispose {}
    }

    fun stopListening() {
        val active = session ?: return
        if (phase != QuickVoicePhase.Listening) return
        phase = QuickVoicePhase.Processing
        active.release()
    }

    fun startListening() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            phase = QuickVoicePhase.Permission
            return
        }
        val spec = voice.spec()
        if (spec is SpeechEngineSpec.Unavailable) {
            message = VoiceCopy.unavailable(spec.why)
            phase = QuickVoicePhase.Error
            return
        }
        eventsJob?.cancel()
        session?.cancel()
        transcript = ""
        message = ""
        elapsedMs = 0L
        val created = voice.start()
        session = created
        phase = QuickVoicePhase.Listening
        eventsJob = scope.launch {
            created.events.collect { event ->
                when (event) {
                    is SpeechEvent.Level -> Unit
                    is SpeechEvent.Partial -> transcript = event.stable + event.tail
                    SpeechEvent.Converting -> phase = QuickVoicePhase.Processing
                    is SpeechEvent.Final -> {
                        transcript = event.text
                        session = null
                        phase = if (event.text.isBlank()) QuickVoicePhase.Empty else QuickVoicePhase.Review
                    }
                    is SpeechEvent.Failed -> {
                        message = VoiceCopy.failure(event.why)
                        session = null
                        phase = if (transcript.isBlank()) QuickVoicePhase.Error else QuickVoicePhase.Review
                    }
                }
            }
        }
    }

    LaunchedEffect(session, phase) {
        val active = session
        if (active == null) {
            // Recording sessions are process-owned and cannot be restored after rotation or process death.
            // Keep a completed draft, but never show a phantom active microphone session.
            if (phase == QuickVoicePhase.Listening || phase == QuickVoicePhase.Processing) {
                phase = if (transcript.isNotBlank()) QuickVoicePhase.Review else QuickVoicePhase.Ready
            }
            return@LaunchedEffect
        }
        if (phase != QuickVoicePhase.Listening) return@LaunchedEffect
        val startedAt = System.currentTimeMillis()
        while (phase == QuickVoicePhase.Listening) {
            delay(250)
            elapsedMs = System.currentTimeMillis() - startedAt
            if (!active.unlimited && elapsedMs >= 60_000L) {
                stopListening()
                break
            }
        }
    }

    val latestSession by rememberUpdatedState(session)
    val latestEventsJob by rememberUpdatedState(eventsJob)
    DisposableEffect(Unit) {
        onDispose {
            latestEventsJob?.cancel()
            latestSession?.cancel()
        }
    }

    val haptics = LocalHapticFeedback.current
    val reduceMotion = QusinTheme.reduceMotion
    val startLabel = stringResource(R.string.quick_voice_start)
    val micInteraction = remember { MutableInteractionSource() }
    val micPressed by micInteraction.collectIsPressedAsState()
    val micScale by animateFloatAsState(
        targetValue = if (micPressed && !reduceMotion) 0.96f else 1f,
        animationSpec = tween(durationMillis = if (reduceMotion) 0 else 160, easing = FastOutSlowInEasing),
        label = "quick-voice-mic-press",
    )
    val activity = context.findActivity()
    val needsPermissionSettings = permissionDenied &&
        (activity == null || !androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.RECORD_AUDIO))

    Column(Modifier.fillMaxSize().background(c.bg).imePadding()) {
        Row(
            Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = QusinSpace.Page, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BarIconButton(QusinIcons.Back, stringResource(R.string.quick_voice_back), onBack)
            Text(
                stringResource(R.string.quick_voice_title),
                Modifier.weight(1f).padding(end = 40.dp),
                style = QusinType.Title,
                color = c.ink,
                textAlign = TextAlign.Center,
            )
        }

        Box(
            Modifier.weight(1f).fillMaxWidth().padding(horizontal = QusinSpace.Page, vertical = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            when (phase) {
                QuickVoicePhase.Ready -> Column(
                    Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(stringResource(R.string.quick_voice_prompt), style = QusinType.LargeTitle, color = c.ink, textAlign = TextAlign.Center)
                    Text(
                        stringResource(R.string.quick_voice_review_hint),
                        Modifier.padding(top = 10.dp),
                        style = QusinType.Body,
                        color = c.ink2,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(32.dp))
                    Box(
                        Modifier.size(120.dp)
                            .graphicsLayer { scaleX = micScale; scaleY = micScale }
                            .clip(CircleShape)
                            .background(c.accentSoft)
                            .border(1.5.dp, c.accent.copy(alpha = 0.25f), CircleShape)
                            .clickable(
                                interactionSource = micInteraction,
                                indication = null,
                                onClickLabel = startLabel,
                                role = Role.Button,
                                onClick = {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    startListening()
                                },
                            )
                            .semantics { contentDescription = startLabel },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(QusinIcons.Mic, startLabel, Modifier.size(48.dp), tint = c.accent)
                    }
                }

                QuickVoicePhase.Listening -> Column(
                    Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(stringResource(R.string.quick_voice_listening), style = QusinType.LargeTitle, color = c.ink, textAlign = TextAlign.Center)
                    Text(
                        formatElapsed(elapsedMs),
                        Modifier.padding(top = 8.dp),
                        style = QusinType.Display.copy(fontSize = 40.sp, fontFeatureSettings = "tnum"),
                        color = c.accent,
                        textAlign = TextAlign.Center,
                    )
                    ListeningIndicator(reduceMotion)
                    if (transcript.isNotBlank()) {
                        Box(
                            Modifier.fillMaxWidth().heightIn(max = 160.dp).clip(QusinShapes.Card)
                                .background(c.surface).border(1.dp, c.edge, QusinShapes.Card)
                                .verticalScroll(rememberScrollState()).padding(16.dp),
                        ) {
                            Text(transcript, style = QusinType.Body, color = c.ink)
                        }
                    } else {
                        Text(stringResource(R.string.quick_voice_listening_hint), style = QusinType.Body, color = c.ink2, textAlign = TextAlign.Center)
                    }
                }

                QuickVoicePhase.Processing -> Column(
                    Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    CircularProgressIndicator(Modifier.size(28.dp), color = c.accent, strokeWidth = 2.5.dp)
                    Text(stringResource(R.string.voice_converting), Modifier.padding(top = 18.dp), style = QusinType.LargeTitle, color = c.ink, textAlign = TextAlign.Center)
                    Text(
                        transcript.ifBlank { stringResource(R.string.quick_voice_processing_hint) },
                        Modifier.fillMaxWidth().padding(top = 12.dp),
                        style = QusinType.Body,
                        color = c.ink2,
                        textAlign = TextAlign.Center,
                    )
                }

                QuickVoicePhase.Review -> Column(
                    Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(stringResource(R.string.quick_voice_review_title), style = QusinType.LargeTitle, color = c.ink, textAlign = TextAlign.Center)
                    Text(
                        stringResource(R.string.quick_voice_edit_hint),
                        Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 18.dp),
                        style = QusinType.Body,
                        color = c.ink2,
                        textAlign = TextAlign.Center,
                    )
                    OutlinedTextField(
                        value = transcript,
                        onValueChange = { transcript = it },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 160.dp)
                            .border(1.5.dp, c.accent.copy(alpha = 0.45f), QusinShapes.Card),
                        shape = QusinShapes.Card,
                        placeholder = { Text(stringResource(R.string.quick_voice_transcript_placeholder)) },
                    )
                    if (message.isNotBlank()) {
                        Row(
                            Modifier.fillMaxWidth().padding(top = 12.dp).clip(QusinShapes.Card)
                                .background(c.badSoft).padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(QusinIcons.Info, null, Modifier.size(18.dp), tint = c.bad)
                            Text(message, Modifier.weight(1f), color = c.bad, style = QusinType.Caption)
                        }
                    }
                }

                QuickVoicePhase.Permission -> InfoState(
                    icon = QusinIcons.Mic,
                    title = stringResource(R.string.quick_voice_permission_title),
                    body = stringResource(R.string.quick_voice_permission_body),
                    isError = false,
                    micOff = true,
                )

                QuickVoicePhase.Empty -> InfoState(
                    icon = QusinIcons.Info,
                    title = stringResource(R.string.quick_voice_empty),
                    body = "",
                    isError = false,
                )

                QuickVoicePhase.Error -> InfoState(
                    icon = QusinIcons.Info,
                    title = stringResource(R.string.voice_unavailable_title),
                    body = message,
                    isError = true,
                )
            }
        }

        Column(
            Modifier.fillMaxWidth().navigationBarsPadding()
                .padding(start = QusinSpace.Page, end = QusinSpace.Page, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            when (phase) {
                QuickVoicePhase.Ready -> {
                    QusinButton(stringResource(R.string.quick_voice_start), ::startListening, Modifier.fillMaxWidth())
                    QusinButton(stringResource(R.string.quick_voice_type), onOpenChat, Modifier.fillMaxWidth(), kind = ButtonKind.Text)
                }
                QuickVoicePhase.Permission -> {
                    if (needsPermissionSettings) {
                        QusinButton(stringResource(R.string.quick_voice_permission_settings), {
                            context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}")))
                        }, Modifier.fillMaxWidth())
                    } else {
                        QusinButton(stringResource(R.string.quick_voice_allow_mic), {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }, Modifier.fillMaxWidth())
                    }
                    QusinButton(stringResource(R.string.quick_voice_type), onOpenChat, Modifier.fillMaxWidth(), kind = ButtonKind.Secondary)
                }
                QuickVoicePhase.Listening -> {
                    QusinButton(stringResource(R.string.quick_voice_stop), ::stopListening, Modifier.fillMaxWidth())
                    QusinButton(stringResource(R.string.quick_voice_cancel), {
                        session?.cancel()
                        session = null
                        eventsJob?.cancel()
                        transcript = ""
                        phase = QuickVoicePhase.Ready
                    }, Modifier.fillMaxWidth(), kind = ButtonKind.Secondary)
                }
                QuickVoicePhase.Processing -> QusinButton(stringResource(R.string.quick_voice_cancel), {
                    session?.cancel(); session = null; eventsJob?.cancel(); phase = QuickVoicePhase.Ready; transcript = ""
                }, Modifier.fillMaxWidth(), kind = ButtonKind.Text)
                QuickVoicePhase.Review -> {
                    QusinButton(stringResource(R.string.quick_voice_submit), {
                        val text = transcript.trim()
                        if (text.isNotEmpty() && !submitting) {
                            submitting = true
                            scope.launch {
                                val sent = runCatching { onSubmit(text) }.getOrElse {
                                    message = it.message ?: context.getString(R.string.quick_voice_submit_failed)
                                    false
                                }
                                submitting = false
                                if (!sent && message.isBlank()) message = context.getString(R.string.quick_voice_submit_failed)
                            }
                        }
                    }, Modifier.fillMaxWidth(), enabled = transcript.isNotBlank() && !submitting)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        QusinButton(stringResource(R.string.quick_voice_again), {
                            session?.cancel(); session = null; phase = QuickVoicePhase.Ready; transcript = ""; message = ""
                            startListening()
                        }, Modifier.weight(1f), kind = ButtonKind.Secondary, size = ButtonSize.Medium)
                        QusinButton(stringResource(R.string.quick_voice_type), onOpenChat, Modifier.weight(1f), kind = ButtonKind.Tonal, size = ButtonSize.Medium)
                    }
                }
                QuickVoicePhase.Empty -> {
                    QusinButton(stringResource(R.string.quick_voice_again), {
                        phase = QuickVoicePhase.Ready
                        startListening()
                    }, Modifier.fillMaxWidth())
                    QusinButton(stringResource(R.string.quick_voice_type), onOpenChat, Modifier.fillMaxWidth(), kind = ButtonKind.Secondary)
                }
                QuickVoicePhase.Error -> {
                    QusinButton(stringResource(R.string.quick_voice_again), {
                        phase = QuickVoicePhase.Ready
                        message = ""
                        startListening()
                    }, Modifier.fillMaxWidth())
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        QusinButton(stringResource(R.string.voice_go_pick), onPickSpeech, Modifier.weight(1f), kind = ButtonKind.Secondary, size = ButtonSize.Medium)
                        QusinButton(stringResource(R.string.quick_voice_type), onOpenChat, Modifier.weight(1f), kind = ButtonKind.Tonal, size = ButtonSize.Medium)
                    }
                }
            }
        }
    }
}

@Composable
private fun ListeningIndicator(reduceMotion: Boolean) {
    val c = QusinTheme.colors
    val pulse = if (reduceMotion) 1f else {
        val transition = rememberInfiniteTransition(label = "quick-voice-listening-rings")
        val animated by transition.animateFloat(
            initialValue = 1f,
            targetValue = 1.25f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1600, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "quick-voice-listening-pulse",
        )
        animated
    }
    Box(Modifier.padding(vertical = 24.dp).size(160.dp), contentAlignment = Alignment.Center) {
        Box(
            Modifier.size(160.dp).graphicsLayer {
                scaleX = pulse
                scaleY = pulse
            }.clip(CircleShape).background(c.accent.copy(alpha = 0.10f)),
        )
        Box(Modifier.size(140.dp).clip(CircleShape).background(c.accent.copy(alpha = 0.18f)))
        Box(Modifier.size(120.dp).clip(CircleShape).background(c.accent), contentAlignment = Alignment.Center) {
            Icon(QusinIcons.Mic, stringResource(R.string.quick_voice_listening), Modifier.size(48.dp), tint = c.onAccent)
        }
    }
}

@Composable
private fun InfoState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    body: String,
    isError: Boolean,
    micOff: Boolean = false,
) {
    val c = QusinTheme.colors
    Column(
        Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            Modifier.size(56.dp).clip(QusinShapes.Card)
                .background(if (isError) c.badSoft else c.neutralSoft),
            contentAlignment = Alignment.Center,
        ) {
            Box(Modifier.size(26.dp), contentAlignment = Alignment.Center) {
                Icon(icon, null, Modifier.size(26.dp), tint = if (isError) c.bad else c.ink2)
                if (micOff) {
                    Canvas(Modifier.size(26.dp)) {
                        drawLine(
                            color = c.ink2,
                            start = Offset(size.width * 0.18f, size.height * 0.18f),
                            end = Offset(size.width * 0.82f, size.height * 0.82f),
                            strokeWidth = 2.dp.toPx(),
                            cap = StrokeCap.Round,
                        )
                    }
                }
            }
        }
        Text(title, Modifier.fillMaxWidth().padding(top = 18.dp), style = QusinType.LargeTitle, color = c.ink, textAlign = TextAlign.Center)
        if (body.isNotBlank()) {
            Text(body, Modifier.fillMaxWidth().padding(top = 10.dp), style = QusinType.Body, color = if (isError) c.bad else c.ink2, textAlign = TextAlign.Center)
        }
    }
}

private fun formatElapsed(milliseconds: Long): String {
    val seconds = (milliseconds / 1000L).coerceAtLeast(0)
    return "%02d:%02d".format(seconds / 60, seconds % 60)
}
