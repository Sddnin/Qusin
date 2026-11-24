package com.wangzan101.qusin.ui

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wangzan101.qusin.R
import com.wangzan101.qusin.core.personal.PersonalTask
import com.wangzan101.qusin.core.personal.PersonalConversation
import com.wangzan101.qusin.core.personal.PersonalSystemException
import com.wangzan101.qusin.personal.displayText
import com.wangzan101.qusin.personal.displayDetail
import com.wangzan101.qusin.personal.displayResult
import com.wangzan101.qusin.personal.personalSystemText
import com.wangzan101.qusin.personal.personalStopErrorText
import com.wangzan101.qusin.memory.MemoryRuntime
import com.wangzan101.qusin.personal.PersonalPhonePreview
import com.wangzan101.qusin.personal.PersonalRuntime
import com.wangzan101.qusin.ui.components.ActivitySizedDialog
import com.wangzan101.qusin.ui.components.BarIconButton
import com.wangzan101.qusin.ui.components.QusinButton
import com.wangzan101.qusin.ui.components.QusinLogo
import com.wangzan101.qusin.ui.components.cardSurface
import com.wangzan101.qusin.ui.components.floatShadow
import com.wangzan101.qusin.ui.theme.QusinIcons
import com.wangzan101.qusin.ui.theme.QusinShapes
import com.wangzan101.qusin.ui.theme.QusinSpace
import com.wangzan101.qusin.ui.theme.QusinTheme
import com.wangzan101.qusin.ui.theme.QusinType
import com.wangzan101.qusin.ui.theme.QusinRadius
import com.wangzan101.qusin.voice.HoldToTalk
import com.wangzan101.qusin.voice.VoiceSource
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
internal fun personalStatus(task: PersonalTask): String = stringResource(when (task.status) {
    "running" -> R.string.personal_status_running
    "queued" -> R.string.personal_status_queued
    "needs_you" -> R.string.personal_status_needs_you
    "paused" -> R.string.personal_status_paused
    "finishing" -> R.string.personal_status_finishing
    "completed" -> R.string.personal_status_completed
    "cancelled" -> R.string.personal_status_cancelled
    "partial" -> R.string.personal_status_partial
    else -> R.string.personal_status_incomplete
})

@Composable
internal fun personalDayFormat(): DateTimeFormatter {
    val locale = LocalConfiguration.current.locales[0]
    return remember(locale) { DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalAgentScreen(
    runtime: PersonalRuntime,
    voice: VoiceSource,
    onOpenDrawer: () -> Unit,
    onOpenWork: () -> Unit,
    onOpenTask: (String) -> Unit,
    memoryRuntime: MemoryRuntime,
    onModels: () -> Unit,
    onPickSpeech: () -> Unit,
) {
    val state by runtime.state.collectAsState()
    val actionError by runtime.error.collectAsState()
    val c = QusinTheme.colors
    val reduceMotion = QusinTheme.reduceMotion
    val scope = rememberCoroutineScope()
    val keyboard = LocalSoftwareKeyboardController.current
    var draft by rememberSaveable { mutableStateOf("") }
    var voiceHolding by remember { mutableStateOf(false) }
    var profile by rememberSaveable { mutableStateOf(false) }
    var historyOpen by rememberSaveable { mutableStateOf(false) }
    var selectedTask by rememberSaveable { mutableStateOf<String?>(null) }
    var replayTask by rememberSaveable { mutableStateOf<String?>(null) }
    var openingPreview by remember { mutableStateOf<PersonalPhonePreview?>(null) }
    var sendError by remember { mutableStateOf<String?>(null) }
    var followLatest by rememberSaveable { mutableStateOf(true) }
    var composerHeight by remember { mutableIntStateOf(0) }
    var previousComposerHeight by remember { mutableIntStateOf(0) }
    val activeConversationId = state.activeConversationId.ifBlank { "main" }
    val messages = remember(state.messages, activeConversationId) {
        state.messages.asSequence().filter { it.threadId == activeConversationId }.mapNotNull { message ->
            if (message.role == "task") null else message.conversationText().takeIf { it.isNotBlank() }?.let { text ->
                if (text == message.text) message else message.copy(text = text)
            }
        }.toList()
    }
    val liveReply by runtime.liveReply.collectAsState()
    val visibleLiveReply = liveReply?.takeIf { it.threadId == activeConversationId }
    val isBusy by runtime.busy.collectAsState()
    val activeThreadPending = isBusy && messages.any { it.role == "user" && !it.handled }
    // The spacer after the last message is the bottom anchor, at index messages.size.
    val list = rememberLazyListState(initialFirstVisibleItemIndex = messages.size)
    var previousMessageCount by rememberSaveable { mutableIntStateOf(messages.size) }
    val active = state.tasks.filter { !it.terminal && it.personalThreadId == activeConversationId }
    val activeIds = active.map { it.conversationId }
    val phonePreview = remember(runtime, activeIds) { runtime.phonePreview(activeIds) }
    val zone = remember { ZoneId.systemDefault() }
    val dayFormat = personalDayFormat()
    val context = LocalContext.current
    val sendFailed = stringResource(R.string.personal_send_failed)
    fun send() {
        val text = draft.trim()
        if (text.isEmpty()) return
        draft = ""
        keyboard?.hide()
        followLatest = true
        scope.launch {
            runCatching { runtime.send(text) }
                .onFailure {
                    draft = text
                    sendError = personalSystemText(context, (it as? PersonalSystemException)?.systemType, it.message ?: sendFailed)
                }
        }
    }
    LaunchedEffect(list) {
        snapshotFlow { list.isScrollInProgress to list.layoutInfo }.collect { (scrolling, layout) ->
            if (scrolling) followLatest = (layout.visibleItemsInfo.lastOrNull()?.index ?: 0) >= layout.totalItemsCount - 3
        }
    }
    LaunchedEffect(activeConversationId) {
        followLatest = true
        previousMessageCount = messages.size
        list.scrollToItem(messages.size)
    }
    LaunchedEffect(messages.size, visibleLiveReply?.text, activeThreadPending) {
        val countChanged = previousMessageCount != messages.size
        previousMessageCount = messages.size
        if ((countChanged || visibleLiveReply != null) && followLatest) list.scrollToItem(messages.size)
    }
    LaunchedEffect(composerHeight) {
        val growth = composerHeight - previousComposerHeight
        previousComposerHeight = composerHeight
        if (growth > 0 && followLatest && !list.isScrollInProgress) list.scrollBy(growth.toFloat())
    }

    Column(Modifier.fillMaxSize().background(c.bg).statusBarsPadding().testTag("personal-screen")) {
        PersonalHeader(
            active, phonePreview, onOpenDrawer, onOpenWork, { profile = true }, { selectedTask = it.id },
            onOpenPreview = { preview ->
                openingPreview = preview
                replayTask = preview.conversationId
            },
            onHistory = { historyOpen = true },
            onNewConversation = { runtime.newConversation(); draft = ""; keyboard?.hide(); followLatest = true },
        )
        Box(Modifier.weight(1f).fillMaxWidth().voiceDim(voiceHolding, reduceMotion)) {
            LazyColumn(state = list, modifier = Modifier.fillMaxSize().testTag("personal-timeline"),
                contentPadding = PaddingValues(horizontal = QusinSpace.Page, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (messages.isEmpty()) item("welcome") {
                    Column(Modifier.fillMaxWidth().padding(top = 60.dp, bottom = 40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(stringResource(R.string.personal_welcome_title), style = QusinType.LargeTitle, color = c.ink, textAlign = TextAlign.Center)
                        Text(stringResource(R.string.personal_welcome_body),
                            Modifier.padding(top = 12.dp), color = c.ink2, style = QusinType.Body, textAlign = TextAlign.Center)
                        TextButton(onClick = onModels, Modifier.padding(top = 16.dp)) { Text(stringResource(R.string.personal_view_model)) }
                    }
                }
                itemsIndexed(messages, key = { _, message -> message.id }) { index, message ->
                    val date = Instant.ofEpochMilli(message.at).atZone(zone).toLocalDate()
                    val priorDate = messages.getOrNull(index - 1)?.let {
                        Instant.ofEpochMilli(it.at).atZone(zone).toLocalDate()
                    }
                    val task = message.taskId?.let { id -> state.tasks.find { it.id == id } }
                    val user = message.role == "user"
                    Column(Modifier.fillMaxWidth(), horizontalAlignment = if (user) Alignment.End else Alignment.Start) {
                        if (date != priorDate) {
                            Text(dayFormat.format(date), Modifier.align(Alignment.CenterHorizontally).padding(vertical = 8.dp),
                                color = c.ink3, style = QusinType.Micro)
                            Spacer(Modifier.height(12.dp))
                        }
                        if (task != null) Text(task.title + " · " + personalStatus(task),
                            Modifier.padding(start = 4.dp, bottom = 6.dp), style = QusinType.Micro, color = c.ink2)
                        val bubble = Modifier.fillMaxWidth(if (user) 0.8f else 1f)
                            .wrapContentWidth(if (user) Alignment.End else Alignment.Start)
                            .background(if (user) c.accent else c.answer, if (user) QusinShapes.UserBubble else QusinShapes.AnswerBubble)
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                        if (user) {
                            Text(message.text, bubble, style = QusinType.Body, color = c.onAccent)
                        } else {
                            PersonalReply(message.displayText(context), message.error, bubble)
                        }
                        if (!message.handled) Text(stringResource(R.string.personal_received), Modifier.padding(top = 4.dp, end = 5.dp), style = QusinType.Micro, color = c.ink3)
                        if (task != null) TextButton(onClick = { selectedTask = task.id }) { Text(stringResource(R.string.personal_view_task), style = QusinType.Hint) }
                    }
                }
                visibleLiveReply?.let { live ->
                    item("live-reply") {
                        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                            PersonalReply(
                                live.text,
                                error = false,
                                bubble = Modifier.fillMaxWidth()
                                    .background(c.answer, QusinShapes.AnswerBubble)
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                            )
                        }
                    }
                } ?: if (activeThreadPending) {
                    item("reply-pending") {
                        Row(Modifier.fillMaxWidth().padding(start = 6.dp, top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(Modifier.size(14.dp), strokeWidth = 1.7.dp, color = c.ink3)
                        }
                    }
                }
                item("bottom") { Spacer(Modifier.height(4.dp)) }
            }
            if (!followLatest && messages.isNotEmpty()) TextButton(
                onClick = { followLatest = true; scope.launch { list.scrollToItem(messages.size) } },
                modifier = Modifier.align(Alignment.BottomCenter).background(c.surface, CircleShape)) { Text(stringResource(R.string.personal_latest)) }
        }
        Composer(
            modifier = Modifier.onSizeChanged { composerHeight = it.height }.testTag("personal-composer"),
            text = draft,
            onText = { draft = it },
            mode = ComposerMode.Idle,
            hasHistory = messages.isNotEmpty(),
            onSend = ::send,
            onStop = {},
            reduceMotion = reduceMotion,
            voice = voice,
            onVoiceText = { draft = HoldToTalk.join(draft, it) },
            onPickSpeech = onPickSpeech,
            onVoiceHolding = { voiceHolding = it },
            sendFromIme = true,
        )
    }
    if (historyOpen) {
        PersonalHistoryDialog(
            conversations = state.conversations.filter { conversation ->
                conversation.title.isNotBlank() || state.messages.any { it.threadId == conversation.id }
            },
            activeId = activeConversationId,
            onDismiss = { historyOpen = false },
            onSelect = { id -> runtime.selectConversation(id); historyOpen = false; draft = ""; followLatest = true },
            onNew = { runtime.newConversation(); historyOpen = false; draft = ""; followLatest = true },
        )
    }
    if (profile) {
        PersonalProfileDialog(
            state.tasks, memoryRuntime,
            onDismiss = { profile = false },
            onTask = { task -> profile = false; selectedTask = task.id },
        )
    }
    selectedTask?.let { id -> state.tasks.find { it.id == id } }?.let { task ->
        key(task.id) {
            val hasReplay by produceState(false, task.terminal) {
                do {
                    value = runtime.replayFrames(task.conversationId).isNotEmpty()
                    if (!task.terminal && !value) delay(1_000)
                } while (!task.terminal && !value)
            }
            PersonalTaskSheet(task, onDismiss = { selectedTask = null },
                onOpen = { selectedTask = null; onOpenTask(task.conversationId) },
                onStop = { runtime.stop(task.id) },
                onReplay = if (hasReplay) ({ selectedTask = null; replayTask = task.conversationId }) else null)
        }
    }
    replayTask?.let { id ->
        key(id) {
            PersonalReplayDialog(runtime, id, id in activeIds, openingPreview?.frame) {
                replayTask = null
                openingPreview = null
            }
        }
    }
    val actionErrorText = actionError?.let { personalStopErrorText(context, it) }
    (sendError ?: actionErrorText)?.let { error -> AlertDialog(onDismissRequest = { sendError = null; runtime.clearError() }, text = { Text(error) },
        confirmButton = { TextButton(onClick = { sendError = null; runtime.clearError() }) { Text(stringResource(R.string.personal_ok)) } }) }
}

@Composable
internal fun PersonalReply(text: String, error: Boolean, bubble: Modifier = Modifier) {
    val c = QusinTheme.colors
    val context = LocalContext.current
    Column {
        SelectionContainer {
            MarkdownText(text, QusinType.Body.copy(color = if (error) c.bad else c.ink), bubble)
        }
        IconButton(onClick = { copyMessage(context, MarkdownModel.toPlainText(text).ifBlank { text }) }) {
            Icon(QusinIcons.Copy, stringResource(R.string.chat_copy), Modifier.size(18.dp), tint = c.ink2)
        }
    }
}

internal enum class PersonalProfileTab(val label: Int, val icon: ImageVector) {
    Tasks(R.string.personal_tab_tasks, QusinIcons.Lines),
    Memory(R.string.personal_tab_memory, QusinIcons.Book),
    Skills(R.string.personal_tab_skills, QusinIcons.Spark),
}

@Composable
internal fun PersonalProfileDialog(
    tasks: List<PersonalTask>,
    memoryRuntime: MemoryRuntime,
    onDismiss: () -> Unit,
    onTask: (PersonalTask) -> Unit,
) {
    val c = QusinTheme.colors
    var tab by rememberSaveable { mutableStateOf(PersonalProfileTab.Tasks) }
    val dismiss by rememberUpdatedState(onDismiss)
    var panelBounds by remember { mutableStateOf(Rect.Zero) }
    val corner = 28.dp
    val shape = RoundedCornerShape(corner)
    val density = LocalDensity.current
    val radius = CornerRadius(with(density) { corner.toPx() })
    val maxContentHeight = if (tab == PersonalProfileTab.Tasks) 300.dp else 520.dp
    ActivitySizedDialog(onDismiss, dismissOnClickOutside = false) { safeArea ->
        BoxWithConstraints(
            Modifier.fillMaxSize().pointerInput(radius) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val bounds = panelBounds
                    val panel = RoundRect(bounds, radius)
                    if (bounds == Rect.Zero || panel.contains(down.position)) return@awaitEachGesture
                    down.consume()
                    val up = waitForUpOrCancellation()
                    if (up != null && !panel.contains(up.position)) {
                        up.consume()
                        dismiss()
                    }
                }
            }.then(safeArea)
                .padding(QusinSpace.S12),
            contentAlignment = Alignment.Center,
        ) {
            val compact = maxHeight < 360.dp
            val showTabIcons = maxWidth >= 284.dp && density.fontScale <= 1.6f
            Column(
                Modifier.widthIn(max = 460.dp).fillMaxWidth()
                    .onGloballyPositioned { panelBounds = it.boundsInRoot() }
                    .cardSurface(c, shape).clip(shape).testTag("personal-profile-dialog")
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = QusinSpace.S12, vertical = if (compact) QusinSpace.S12 else QusinSpace.S16),
                verticalArrangement = Arrangement.spacedBy(if (compact) QusinSpace.S8 else QusinSpace.S12),
            ) {
                Box(Modifier.fillMaxWidth()) {
                    if (compact) {
                        Row(
                            Modifier.align(Alignment.Center).padding(horizontal = 40.dp),
                            horizontalArrangement = Arrangement.spacedBy(QusinSpace.S8),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            QusinLogo(36.dp, corner = 18.dp)
                            Text("Qusin", style = QusinType.Title, color = c.ink, maxLines = 1)
                        }
                    } else {
                        Column(Modifier.align(Alignment.TopCenter).padding(top = QusinSpace.S4),
                            horizontalAlignment = Alignment.CenterHorizontally) {
                            QusinLogo(52.dp, corner = 26.dp)
                            Text("Qusin", Modifier.padding(top = QusinSpace.S8), style = QusinType.Title, color = c.ink)
                        }
                    }
                    Box(Modifier.align(Alignment.TopEnd)) {
                        BarIconButton(QusinIcons.Close, stringResource(R.string.personal_close), onDismiss)
                    }
                }
                PersonalProfileTabs(tab, showIcons = showTabIcons, onSelect = { tab = it })
                Box(Modifier.fillMaxWidth().heightIn(min = 136.dp, max = maxContentHeight)) {
                    if (tab == PersonalProfileTab.Tasks) {
                        if (tasks.isEmpty()) {
                            Text(stringResource(R.string.personal_no_tasks), Modifier.align(Alignment.Center), style = QusinType.Body, color = c.ink2)
                        } else {
                            LazyColumn(
                                Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = QusinSpace.S8),
                                verticalArrangement = Arrangement.spacedBy(QusinSpace.S12),
                            ) {
                                items(tasks.asReversed(), key = { it.id }) { task -> PersonalTaskCard(task) { onTask(task) } }
                            }
                        }
                    } else {
                        val model by rememberMemoryViewModel(memoryRuntime)
                        val vm = model
                        if (vm == null) {
                            CircularProgressIndicator(Modifier.align(Alignment.Center).size(24.dp))
                        } else {
                            val ui by vm.ui.collectAsState()
                            key(tab) {
                                Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
                                    .padding(horizontal = 6.dp, vertical = QusinSpace.S8)) {
                                    if (tab == PersonalProfileTab.Memory) {
                                        MemoryEntries(vm, ui, allowAdd = false)
                                    } else {
                                        SkillsSection(vm, ui.skills, pending = false)
                                        SkillsSection(vm, ui.skills, pending = true)
                                    }
                                }
                            }
                            MemoryMessage(vm, ui.message)
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun PersonalProfileTabs(selected: PersonalProfileTab, showIcons: Boolean, onSelect: (PersonalProfileTab) -> Unit) {
    val c = QusinTheme.colors
    val labels = PersonalProfileTab.entries.map { stringResource(it.label) }
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        // Long translations at large font scales don't fit three equal slots; stack them instead of clipping or shrinking text.
        val widest = with(density) {
            labels.maxOf { measurer.measure(it, QusinType.Label, maxLines = 1).size.width }.toDp()
        }
        val slot = (maxWidth - 6.dp - 4.dp) / 3
        val stacked = widest + QusinSpace.S4 * 2 + (if (showIcons) 20.dp else 0.dp) > slot
        val item: @Composable (PersonalProfileTab, String, Modifier) -> Unit = { tab, label, modifier ->
            val active = tab == selected
            val ink = if (active) c.ink else c.ink2
            Row(
                modifier.heightIn(min = 42.dp)
                    .then(if (active) Modifier.cardSurface(c, CircleShape) else Modifier)
                    .clip(CircleShape).selectable(selected = active, role = Role.Tab) { onSelect(tab) }
                    .padding(horizontal = QusinSpace.S4),
                horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (showIcons) Icon(tab.icon, null, Modifier.size(16.dp), tint = ink)
                Text(label, style = QusinType.Label, color = ink, maxLines = 1)
            }
        }
        if (stacked) {
            Column(
                Modifier.fillMaxWidth().selectableGroup().clip(QusinShapes.Sheet).background(c.fill).padding(3.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                PersonalProfileTab.entries.forEachIndexed { i, tab -> item(tab, labels[i], Modifier.fillMaxWidth()) }
            }
        } else {
            Row(
                Modifier.fillMaxWidth().selectableGroup().clip(CircleShape).background(c.fill).padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                PersonalProfileTab.entries.forEachIndexed { i, tab -> item(tab, labels[i], Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun PersonalHistoryDialog(
    conversations: List<PersonalConversation>,
    activeId: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit,
    onNew: () -> Unit,
) {
    val sorted = remember(conversations) { conversations.sortedByDescending { it.updatedAt } }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.personal_history)) },
        text = {
            Column(Modifier.heightIn(max = 440.dp)) {
                TextButton(onClick = onNew, modifier = Modifier.fillMaxWidth()) {
                    Icon(QusinIcons.Plus, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.personal_new_chat))
                }
                if (sorted.isEmpty()) {
                    Text(stringResource(R.string.personal_history_empty), Modifier.padding(vertical = 20.dp), color = QusinTheme.colors.ink2)
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(sorted, key = { it.id }) { conversation ->
                            TextButton(
                                onClick = { onSelect(conversation.id) },
                                modifier = Modifier.fillMaxWidth().testTag("personal-history-${conversation.id}"),
                            ) {
                                Column(Modifier.fillMaxWidth()) {
                                    Text(
                                        conversation.title.ifBlank { stringResource(R.string.personal_untitled_chat) },
                                        color = if (conversation.id == activeId) QusinTheme.colors.accent else QusinTheme.colors.ink,
                                        maxLines = 1, overflow = TextOverflow.Ellipsis,
                                    )
                                    if (conversation.updatedAt > 0L) {
                                        Text(
                                            personalDayFormat().format(Instant.ofEpochMilli(conversation.updatedAt).atZone(ZoneId.systemDefault()).toLocalDate()),
                                            color = QusinTheme.colors.ink3, style = QusinType.Micro,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.ok_got_it)) } },
    )
}

@Composable
internal fun PersonalHeader(
    active: List<PersonalTask>,
    phonePreview: Flow<PersonalPhonePreview?>,
    onOpenDrawer: () -> Unit,
    onOpenWork: () -> Unit = {},
    onActivity: () -> Unit,
    onTask: (PersonalTask) -> Unit,
    onOpenPreview: (PersonalPhonePreview) -> Unit,
    onHistory: () -> Unit = {},
    onNewConversation: () -> Unit = {},
) {
    val c = QusinTheme.colors
    val latestPreview by phonePreview.collectAsState(initial = null)
    val context = LocalContext.current
    val preview = latestPreview
    val previewTask = active.firstOrNull { it.conversationId == preview?.conversationId }
    val frame = preview?.frame?.takeUnless { previewTask == null || it.isRecycled }
    val previewWidth = frame?.let { minOf(76.dp, 104.dp * (it.width.toFloat() / it.height)) } ?: 0.dp
    Box(Modifier.fillMaxWidth().padding(horizontal = QusinSpace.Page, vertical = 6.dp)) {
        Box(Modifier.align(Alignment.TopStart).padding(top = 4.dp)) {
            BarIconButton(QusinIcons.Menu, stringResource(R.string.personal_open_sidebar), onOpenDrawer)
        }
        Row(
            Modifier.align(Alignment.TopEnd).padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            BarIconButton(QusinIcons.Clock, stringResource(R.string.personal_history), onHistory)
            BarIconButton(QusinIcons.New, stringResource(R.string.personal_new_chat), onNewConversation)
        }
        Column(Modifier.align(Alignment.TopCenter).widthIn(max = 250.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(QusinSpace.S12),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (frame != null) Spacer(Modifier.width(previewWidth))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        Modifier.size(58.dp).floatShadow(c, CircleShape).background(c.surface, CircleShape)
                            .border(0.5.dp, c.edge, CircleShape).clip(CircleShape).testTag("personal-profile")
                            .clickable(onClickLabel = stringResource(R.string.personal_view_activity), role = Role.Button, onClick = onActivity),
                        contentAlignment = Alignment.Center,
                    ) {
                        QusinLogo(52.dp, corner = 26.dp)
                    }
                    Text("Qusin", Modifier.padding(top = 7.dp), style = QusinType.Title, color = c.ink)
                }
                if (frame != null) {
                    Image(
                        remember(frame) { frame.asImageBitmap() },
                        contentDescription = stringResource(R.string.personal_expand_phone),
                        modifier = Modifier.size(previewWidth, previewWidth * (frame.height.toFloat() / frame.width))
                            .border(1.dp, c.ink.copy(alpha = 0.18f), QusinShapes.Tile)
                            .clip(QusinShapes.Tile).testTag("personal-phone-preview")
                            .clickable(role = Role.Button) { onOpenPreview(preview) },
                        contentScale = ContentScale.Fit,
                    )
                }
            }
            ConversationModeSwitcher(
                personal = true,
                onWork = onOpenWork,
                onPersonal = {},
                modifier = Modifier.padding(top = 8.dp).widthIn(min = 200.dp, max = 250.dp).testTag("personal-mode-switch"),
            )
            val task = previewTask ?: active.firstOrNull()
            if (task != null) {
                val tone = personalTaskTone(task)
                val showAll = active.size > 1 && frame == null
                Row(
                    Modifier.padding(top = QusinSpace.S4).heightIn(min = 32.dp).clip(QusinShapes.Pill)
                        .background(tone.fill).testTag("personal-task-progress")
                        .clickable(role = Role.Button) { if (showAll) onActivity() else onTask(task) }
                        .padding(horizontal = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(tone.icon, null, Modifier.size(14.dp), tint = tone.ink)
                    Text(
                        if (showAll) pluralStringResource(R.plurals.task_active, active.size, active.size) else task.displayDetail(context).ifBlank { task.title },
                        Modifier.weight(1f, fill = false), style = QusinType.Hint, color = tone.ink,
                        maxLines = 1, overflow = TextOverflow.Ellipsis,
                    )
                    Icon(QusinIcons.Chevron, null, Modifier.size(12.dp), tint = tone.ink)
                }
            }
        }
    }
}

private data class PersonalTaskTone(val icon: ImageVector, val ink: Color, val fill: Color)

private val PersonalTask.iconOnlyStatus: Boolean get() = status == "completed" || status == "failed"

@Composable
private fun personalTaskTone(task: PersonalTask): PersonalTaskTone {
    val c = QusinTheme.colors
    return when (task.status) {
        "completed" -> PersonalTaskTone(QusinIcons.Check, c.ok, c.okSoft)
        "needs_you", "paused", "partial" -> PersonalTaskTone(QusinIcons.Info, c.warn, c.warnSoft)
        "failed" -> PersonalTaskTone(QusinIcons.Info, c.bad, c.badSoft)
        "cancelled" -> PersonalTaskTone(QusinIcons.Stop, c.ink2, c.fill)
        "queued" -> PersonalTaskTone(QusinIcons.Clock, c.ink2, c.fill)
        else -> PersonalTaskTone(QusinIcons.Spark, c.accent, c.accentSoft)
    }
}

@Composable
private fun PersonalTaskIcon(tone: PersonalTaskTone, label: String?) {
    Box(Modifier.size(40.dp).background(tone.fill, QusinShapes.Chip), contentAlignment = Alignment.Center) {
        Icon(tone.icon, label, Modifier.size(19.dp), tint = tone.ink)
    }
}

@Composable
private fun PersonalTaskCard(task: PersonalTask, onClick: () -> Unit) {
    val c = QusinTheme.colors
    val tone = personalTaskTone(task)
    val context = LocalContext.current
    val summary = task.displayResult(context)?.takeIf { it.isNotBlank() } ?: task.displayDetail(context).ifBlank { task.goal }
    val description = remember(summary) { MarkdownModel.toPlainText(summary).lineSequence().firstOrNull().orEmpty() }
    Row(
        Modifier.fillMaxWidth().testTag("personal-task-" + task.id).cardSurface(c).clip(QusinShapes.Card)
            .clickable(onClickLabel = stringResource(R.string.personal_task_details), role = Role.Button, onClick = onClick)
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PersonalTaskIcon(tone, personalStatus(task).takeIf { task.iconOnlyStatus })
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(task.title, style = QusinType.BodyStrong, color = c.ink, maxLines = 2, overflow = TextOverflow.Ellipsis)
            if (description.isNotBlank() && description != task.title) {
                Text(description, style = QusinType.Caption, color = c.ink2, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            if (!task.iconOnlyStatus) Text(personalStatus(task), style = QusinType.Label, color = tone.ink)
        }
        Icon(QusinIcons.Chevron, null, Modifier.size(16.dp), tint = c.ink3)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PersonalTaskSheet(
    task: PersonalTask,
    onDismiss: () -> Unit,
    onOpen: () -> Unit,
    onStop: () -> Unit,
    onReplay: (() -> Unit)?,
) {
    val c = QusinTheme.colors
    val tone = personalTaskTone(task)
    val context = LocalContext.current
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = c.surface,
    ) {
        Column(
            Modifier.fillMaxWidth().heightIn(max = 580.dp).padding(horizontal = QusinSpace.S20)
                .testTag("personal-task-detail"),
        ) {
            Column(
                Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()).padding(bottom = QusinSpace.S16),
                verticalArrangement = Arrangement.spacedBy(QusinSpace.S16),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    PersonalTaskIcon(tone, personalStatus(task).takeIf { task.iconOnlyStatus })
                    if (!task.iconOnlyStatus) Text(personalStatus(task), style = QusinType.Label, color = tone.ink)
                }
                Text(task.title, style = QusinType.LargeTitle, color = c.ink)
                if (task.goal.isNotBlank() && task.goal != task.title) PersonalTaskSection(stringResource(R.string.personal_goal), task.goal)
                if (task.detail.isNotBlank() && task.detail != task.goal && task.detail != task.result) {
                    PersonalTaskSection(stringResource(R.string.personal_progress), task.displayDetail(context))
                }
                task.displayResult(context)?.takeIf { it.isNotBlank() }?.let { PersonalTaskSection(stringResource(R.string.personal_result), it) }
            }
            HorizontalDivider(color = c.hair)
            QusinButton(
                if (task.status == "needs_you") stringResource(R.string.personal_handle_task) else stringResource(R.string.personal_view_execution),
                onOpen, Modifier.fillMaxWidth().padding(top = QusinSpace.S12, bottom = QusinSpace.S12),
            )
            onReplay?.let {
                TextButton(onClick = it, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.personal_view_replay)) }
            }
            if (!task.terminal) {
                TextButton(onClick = onStop, Modifier.fillMaxWidth().padding(bottom = QusinSpace.S8)) {
                    Text(stringResource(R.string.personal_stop_task), color = c.bad)
                }
            }
        }
    }
}

@Composable
private fun PersonalTaskSection(label: String, body: String) {
    val c = QusinTheme.colors
    Column(Modifier.fillMaxWidth().background(c.fill, QusinShapes.Card).padding(QusinSpace.S16),
        verticalArrangement = Arrangement.spacedBy(QusinSpace.S8)) {
        Text(label, style = QusinType.Label, color = c.ink2)
        MarkdownText(body, style = QusinType.Body.copy(color = c.ink))
    }
}
