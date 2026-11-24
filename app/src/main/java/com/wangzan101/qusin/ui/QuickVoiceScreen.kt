package com.wangzan101.qusin.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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

    Column(
        Modifier.fillMaxSize().background(c.bg).statusBarsPadding(),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = QusinSpace.Page, vertical = 8.dp),
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

        Column(
            Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())
                .padding(horizontal = QusinSpace.Page, vertical = QusinSpace.S20),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            when (phase) {
                QuickVoicePhase.Ready -> {
                    Text(stringResource(R.string.quick_voice_prompt), style = QusinType.LargeTitle, color = c.ink, textAlign = TextAlign.Center)
                    Text(stringResource(R.string.quick_voice_review_hint), Modifier.padding(top = 10.dp), style = QusinType.Body, color = c.ink2, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(34.dp))
                    Box(
                        Modifier.size(116.dp).clip(CircleShape).background(c.accentSoft),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(QusinIcons.Mic, null, Modifier.size(48.dp), tint = c.accent)
                    }
                    Text(stringResource(R.string.quick_voice_start), Modifier.padding(top = 16.dp), style = QusinType.BodyStrong, color = c.ink)
                    Spacer(Modifier.height(28.dp))
                    QusinButton(stringResource(R.string.quick_voice_start), ::startListening, Modifier.fillMaxWidth())
                    QusinButton(stringResource(R.string.quick_voice_type), onOpenChat, Modifier.fillMaxWidth().padding(top = 10.dp), kind = ButtonKind.Secondary)
                }
                QuickVoicePhase.Permission -> {
                    Text(stringResource(R.string.quick_voice_permission_title), style = QusinType.LargeTitle, color = c.ink, textAlign = TextAlign.Center)
                    Text(stringResource(R.string.quick_voice_permission_body), Modifier.padding(top = 12.dp), style = QusinType.Body, color = c.ink2, textAlign = TextAlign.Center)
                    val activity = context.findActivity()
                    if (permissionDenied && (activity == null || !androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.RECORD_AUDIO))) {
                        QusinButton(stringResource(R.string.quick_voice_permission_settings), {
                            context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}")))
                        }, Modifier.fillMaxWidth().padding(top = 24.dp))
                    } else {
                        QusinButton(stringResource(R.string.quick_voice_allow_mic), {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }, Modifier.fillMaxWidth().padding(top = 24.dp))
                    }
                    QusinButton(stringResource(R.string.quick_voice_type), onOpenChat, Modifier.fillMaxWidth().padding(top = 10.dp), kind = ButtonKind.Secondary)
                }
                QuickVoicePhase.Listening -> {
                    Text(stringResource(R.string.quick_voice_listening), style = QusinType.LargeTitle, color = c.ink)
                    Text(formatElapsed(elapsedMs), Modifier.padding(top = 8.dp), style = QusinType.Display, color = c.accent)
                    Box(Modifier.padding(vertical = 28.dp).size(116.dp).clip(CircleShape).background(c.accent), contentAlignment = Alignment.Center) {
                        Icon(QusinIcons.Mic, null, Modifier.size(48.dp), tint = c.onAccent)
                    }
                    if (transcript.isNotBlank()) {
                        Text(transcript, Modifier.fillMaxWidth().padding(bottom = 24.dp), style = QusinType.Body, color = c.ink, textAlign = TextAlign.Center)
                    } else {
                        Text(stringResource(R.string.quick_voice_listening_hint), style = QusinType.Body, color = c.ink2)
                    }
                    QusinButton(stringResource(R.string.quick_voice_stop), ::stopListening, Modifier.fillMaxWidth())
                    QusinButton(stringResource(R.string.quick_voice_cancel), {
                        session?.cancel()
                        session = null
                        eventsJob?.cancel()
                        transcript = ""
                        phase = QuickVoicePhase.Ready
                    }, Modifier.fillMaxWidth().padding(top = 10.dp), kind = ButtonKind.Secondary)
                }
                QuickVoicePhase.Processing -> {
                    Text(stringResource(R.string.voice_converting), style = QusinType.LargeTitle, color = c.ink)
                    Text(transcript.ifBlank { stringResource(R.string.quick_voice_processing_hint) }, Modifier.fillMaxWidth().padding(top = 16.dp), style = QusinType.Body, color = c.ink2, textAlign = TextAlign.Center)
                    QusinButton(stringResource(R.string.quick_voice_cancel), {
                        session?.cancel(); session = null; eventsJob?.cancel(); phase = QuickVoicePhase.Ready; transcript = ""
                    }, Modifier.fillMaxWidth().padding(top = 28.dp), kind = ButtonKind.Secondary)
                }
                QuickVoicePhase.Review -> {
                    Text(stringResource(R.string.quick_voice_review_title), style = QusinType.LargeTitle, color = c.ink)
                    Text(stringResource(R.string.quick_voice_edit_hint), Modifier.padding(top = 8.dp, bottom = 18.dp), style = QusinType.Body, color = c.ink2)
                    OutlinedTextField(
                        value = transcript,
                        onValueChange = { transcript = it },
                        modifier = Modifier.fillMaxWidth().height(190.dp),
                        shape = QusinShapes.Card,
                        placeholder = { Text(stringResource(R.string.quick_voice_transcript_placeholder)) },
                    )
                    if (message.isNotBlank()) Text(message, Modifier.padding(top = 10.dp), color = c.bad, style = QusinType.Caption)
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
                    }, Modifier.fillMaxWidth().padding(top = 18.dp), enabled = transcript.isNotBlank() && !submitting)
                    Row(Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        QusinButton(stringResource(R.string.quick_voice_again), {
                            session?.cancel(); session = null; phase = QuickVoicePhase.Ready; transcript = ""; message = ""
                            startListening()
                        }, Modifier.weight(1f), kind = ButtonKind.Secondary, size = ButtonSize.Medium)
                        QusinButton(stringResource(R.string.quick_voice_type), onOpenChat, Modifier.weight(1f), kind = ButtonKind.Tonal, size = ButtonSize.Medium)
                    }
                }
                QuickVoicePhase.Empty -> {
                    Text(stringResource(R.string.quick_voice_empty), style = QusinType.LargeTitle, color = c.ink, textAlign = TextAlign.Center)
                    QusinButton(stringResource(R.string.quick_voice_again), {
                        phase = QuickVoicePhase.Ready
                        startListening()
                    }, Modifier.fillMaxWidth().padding(top = 24.dp))
                    QusinButton(stringResource(R.string.quick_voice_type), onOpenChat, Modifier.fillMaxWidth().padding(top = 10.dp), kind = ButtonKind.Secondary)
                }
                QuickVoicePhase.Error -> {
                    Text(stringResource(R.string.voice_unavailable_title), style = QusinType.LargeTitle, color = c.ink, textAlign = TextAlign.Center)
                    Text(message, Modifier.padding(top = 12.dp), style = QusinType.Body, color = c.bad, textAlign = TextAlign.Center)
                    QusinButton(stringResource(R.string.quick_voice_again), {
                        phase = QuickVoicePhase.Ready
                        message = ""
                        startListening()
                    }, Modifier.fillMaxWidth().padding(top = 24.dp))
                    QusinButton(stringResource(R.string.voice_go_pick), onPickSpeech, Modifier.fillMaxWidth().padding(top = 10.dp), kind = ButtonKind.Secondary)
                    QusinButton(stringResource(R.string.quick_voice_type), onOpenChat, Modifier.fillMaxWidth().padding(top = 10.dp), kind = ButtonKind.Tonal)
                }
            }
        }
    }
}

private fun formatElapsed(milliseconds: Long): String {
    val seconds = (milliseconds / 1000L).coerceAtLeast(0)
    return "%02d:%02d".format(seconds / 60, seconds % 60)
}
