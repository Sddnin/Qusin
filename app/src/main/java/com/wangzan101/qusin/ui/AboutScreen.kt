package com.wangzan101.qusin.ui

import androidx.compose.ui.res.stringResource
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.wangzan101.qusin.R
import com.wangzan101.qusin.about.CheckStatus
import com.wangzan101.qusin.about.QusinLinks
import com.wangzan101.qusin.about.ReleaseInfo
import com.wangzan101.qusin.about.UpdateState
import com.wangzan101.qusin.ui.components.ButtonKind
import com.wangzan101.qusin.ui.components.GroupCard
import com.wangzan101.qusin.ui.components.ListRow
import com.wangzan101.qusin.ui.components.QusinSwitch
import com.wangzan101.qusin.ui.components.QusinButton
import com.wangzan101.qusin.ui.components.PageScaffold
import com.wangzan101.qusin.ui.components.RowChevron
import com.wangzan101.qusin.ui.components.RowValue
import com.wangzan101.qusin.ui.components.ValueTone
import com.wangzan101.qusin.ui.theme.QusinIcons
import com.wangzan101.qusin.ui.theme.QusinTheme
import com.wangzan101.qusin.ui.theme.QusinType
import com.wangzan101.qusin.ui.theme.QusinRadius
import com.wangzan101.qusin.ui.theme.QusinShapes

/** 关于页上点开的底部面板。 */
private enum class AboutSheet { Update, Changelog, Feedback, Diagnostics }

/**
 * 关于 Qusin:顶上图标、名字、一句话介绍、版本(Beta 标记、构建编号);下面是检查更新与更新记录、
 * 反馈问题、开源仓库与使用说明;一段数据说明;最底下是许可证。
 * 外部链接都交给 [onOpenUrl](系统浏览器);手动检查的结果到了([UpdateState.manualRounds] 变了)才提示。
 * [diagnostics] 写出诊断信息(反馈面板里「复制诊断信息」点开预览时在后台线程调)。
 */
@Composable
fun AboutScreen(
    versionName: String,
    build: String,
    beta: Boolean,
    update: UpdateState,
    issueUrl: String,
    onCheck: () -> Unit,
    onOpenUrl: (String) -> Unit,
    onOpenLicense: () -> Unit,
    onBack: () -> Unit,
    diagnostics: () -> String = { "" },
    autoUpdate: Boolean = false,
    onAutoUpdate: (Boolean) -> Unit = {},
    mirrorEnabled: Boolean = false,
    mirrorCount: Int = 0,
    onMirrorEnabled: (Boolean) -> Unit = {},
    onOpenMirrors: () -> Unit = {},
) {
    val c = QusinTheme.colors
    val context = LocalContext.current
    var sheet by rememberSaveable { mutableStateOf<AboutSheet?>(null) }
    // 点了「检查更新」后等这一轮结果:有新版直接弹出说明,没有就提示一句
    var waitingRound by remember { mutableIntStateOf(-1) }
    LaunchedEffect(update.manualRounds) {
        if (waitingRound >= 0 && update.manualRounds > waitingRound) {
            waitingRound = -1
            when {
                update.update != null -> sheet = AboutSheet.Update
                update.status == CheckStatus.Failed ->
                    Toast.makeText(context, AboutCopy.OFFLINE_TOAST, Toast.LENGTH_SHORT).show()
                else -> Toast.makeText(context, AboutCopy.UP_TO_DATE_TOAST, Toast.LENGTH_SHORT).show()
            }
        }
    }
    PageScaffold(AboutCopy.TITLE, onBack) {
        Hero(versionName, build, beta)

        GroupCard {
            val v = AboutCopy.checkValue(update)
            ListRow(
                QusinIcons.Refresh, AboutCopy.CHECK, if (update.hasUpdate) AboutCopy.CHECK_SUB_NEW else AboutCopy.CHECK_SUB, first = true,
                onClick = {
                    if (update.hasUpdate) sheet = AboutSheet.Update
                    else if (update.status != CheckStatus.Checking) {
                        waitingRound = update.manualRounds
                        onCheck()
                    }
                },
            ) {
                TrailingValue(v)
                RowChevron()
            }
            ListRow(
                QusinIcons.Refresh,
                stringResource(R.string.about_auto_update),
                stringResource(R.string.about_auto_update_detail),
            ) {
                QusinSwitch(autoUpdate, onAutoUpdate, Modifier.testTag("auto-update-switch"))
            }
            ListRow(
                QusinIcons.Link,
                stringResource(R.string.about_mirror_download),
                stringResource(R.string.about_mirror_download_detail),
            ) {
                QusinSwitch(mirrorEnabled, onMirrorEnabled, Modifier.testTag("mirror-download-switch"))
            }
            ListRow(
                QusinIcons.Shield,
                stringResource(R.string.about_mirror_sources),
                stringResource(R.string.about_mirror_sources_detail),
                onClick = onOpenMirrors,
            ) {
                RowValue(stringResource(R.string.about_mirrors_count, mirrorCount))
                RowChevron()
            }
            ListRow(QusinIcons.Lines, AboutCopy.CHANGELOG, AboutCopy.CHANGELOG_SUB, onClick = { sheet = AboutSheet.Changelog }) { RowChevron() }
        }
        // 像 GitHub App 那样把最新发布直接摆在页面上:名字、日期、标记;点开看说明,下面一行去 GitHub 看全部发布
        GroupCard {
            val latest = update.latest
            if (latest != null) {
                ListRow(
                    QusinIcons.Lines, latest.label, AboutCopy.releaseSub(latest), first = true,
                    onClick = { sheet = AboutSheet.Changelog }, modifier = Modifier.testTag("about-latest-release"),
                ) {
                    RowValue(AboutCopy.releaseBadge(latest), ValueTone.Ok)
                    RowChevron()
                }
            }
            ListRow(QusinIcons.Code, AboutCopy.CHANGELOG_ALL, first = latest == null, onClick = { onOpenUrl(QusinLinks.RELEASES) }) { ExternalMark() }
        }
        GroupCard {
            ListRow(QusinIcons.Bug, AboutCopy.FEEDBACK, AboutCopy.FEEDBACK_SUB, first = true, onClick = { sheet = AboutSheet.Feedback }) { RowChevron() }
        }
        GroupCard {
            ListRow(QusinIcons.Code, AboutCopy.REPO, AboutCopy.REPO_SUB, first = true, onClick = { onOpenUrl(QusinLinks.REPO) }) { ExternalMark() }
            ListRow(QusinIcons.Book, AboutCopy.GUIDE, AboutCopy.GUIDE_SUB, onClick = { onOpenUrl(QusinLinks.README) }) { ExternalMark() }
        }

        Row(
            Modifier.fillMaxWidth().padding(top = 2.dp).background(c.okSoft, QusinShapes.Button).padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(QusinIcons.Shield, null, Modifier.padding(top = 1.dp).size(18.dp), tint = c.ok)
            Text(AboutCopy.DATA_NOTE, style = QusinType.Caption, color = c.ink2)
        }

        Column(Modifier.fillMaxWidth().padding(top = 20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                buildAnnotatedString {
                    val (before, after) = stringResource(R.string.about_released_under).split("%1\$s").let { it[0] to it.getOrElse(1) { "" } }
                    append(before)
                    withStyle(SpanStyle(color = c.accent)) { append(AboutCopy.LICENSE_LINK) }
                    append(after)
                },
                Modifier.clip(RoundedCornerShape(QusinRadius.BubbleTail)).clickable(onClick = onOpenLicense).padding(horizontal = 6.dp, vertical = 2.dp)
                    .testTag("about-license"),
                style = QusinType.Hint, color = c.ink3, textAlign = TextAlign.Center,
            )
        }
    }

    val close = { sheet = null }
    when (sheet) {
        AboutSheet.Update -> update.update?.let { r ->
            UpdateSheet(r, versionName, beta, onOpenUrl = { close(); onOpenUrl(it) }, onDismiss = close)
        }
        AboutSheet.Changelog -> ChangelogSheet(update.latest, onOpenAll = { close(); onOpenUrl(QusinLinks.RELEASES) }, onDismiss = close)
        AboutSheet.Feedback -> FeedbackSheet(
            onGitHub = { close(); onOpenUrl(issueUrl) },
            onDiagnostics = { sheet = AboutSheet.Diagnostics },
            onDismiss = close,
        )
        AboutSheet.Diagnostics -> DiagnosticsSheet(diagnostics, onDismiss = close)
        null -> Unit
    }
}

@Composable
private fun Hero(versionName: String, build: String, beta: Boolean) {
    val c = QusinTheme.colors
    Column(
        Modifier.fillMaxWidth().padding(top = 14.dp, bottom = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painterResource(R.drawable.qusin_launcher_art), contentDescription = null,
            Modifier.size(72.dp).clip(RoundedCornerShape(QusinRadius.Bubble)),
        )
        Text("Qusin", Modifier.padding(top = 12.dp), style = QusinType.Title, fontWeight = FontWeight.Bold, color = c.ink)
        Text(AboutCopy.TAGLINE, Modifier.padding(top = 4.dp), style = QusinType.Caption, color = c.ink2, textAlign = TextAlign.Center)
        Row(Modifier.padding(top = 10.dp).testTag("about-version"), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(AboutCopy.version(versionName), style = QusinType.Hint, color = c.ink3)
            if (beta) {
                Text(
                    AboutCopy.BETA, Modifier.background(c.accentSoft, QusinShapes.Tile).padding(horizontal = 7.dp, vertical = 1.dp),
                    style = QusinType.Hint, fontWeight = FontWeight.SemiBold, color = c.accent,
                )
            }
            Text("· ${AboutCopy.buildLabel(build)}", style = QusinType.Hint, color = c.ink3)
        }
    }
}

/** 行尾的值:转圈、红点加文字,或一段普通的值。 */
@Composable
internal fun TrailingValue(v: AboutCopy.CheckValue) {
    if (v.spinner) {
        CircularProgressIndicator(Modifier.size(16.dp).testTag("update-spinner"), color = QusinTheme.colors.accent, strokeWidth = 2.dp)
        return
    }
    if (v.dot) UpdateDot()
    v.text?.let { RowValue(it, if (v.highlight) ValueTone.Link else ValueTone.Plain) }
}

/** 有新版本时的小红点。 */
@Composable
internal fun UpdateDot(modifier: Modifier = Modifier) {
    Box(modifier.size(8.dp).background(QusinTheme.colors.bad, CircleShape).testTag("update-dot"))
}

@Composable
private fun ExternalMark() {
    Icon(QusinIcons.External, null, Modifier.size(15.dp), tint = QusinTheme.colors.ink3)
}

// ---------- 底部面板 ----------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AboutSheetFrame(title: String, sub: String?, tag: String, onDismiss: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    val c = QusinTheme.colors
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = c.surface,
        shape = QusinShapes.SheetTop,
    ) {
        // 面板内容太高(矮屏、大字号)时整体能滚,底下的按钮总够得着
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).navigationBarsPadding()
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp).testTag(tag),
        ) {
            Text(title, style = QusinType.Title, color = c.ink)
            sub?.let { Text(it, Modifier.padding(top = 2.dp), style = QusinType.Caption, color = c.ink3) }
            content()
        }
    }
}

/** 更新说明(Markdown),浅底、太长时在框里滚。 */
@Composable
private fun NotesBox(notes: String) {
    val c = QusinTheme.colors
    val languageTag = LocalConfiguration.current.locales[0]?.toLanguageTag()
    val localizedNotes = com.wangzan101.qusin.about.ReleaseNotesLocalization.select(notes, languageTag)
    Box(
        Modifier.padding(top = 12.dp).fillMaxWidth().heightIn(max = 280.dp)
            .background(c.fill, QusinShapes.Chip).verticalScroll(rememberScrollState()).padding(12.dp),
    ) {
        MarkdownText(localizedNotes, QusinType.Caption.copy(color = c.ink2))
    }
}

@Composable
internal fun UpdateSheet(r: ReleaseInfo, current: String, beta: Boolean, onOpenUrl: (String) -> Unit, onDismiss: () -> Unit) {
    AboutSheetFrame(AboutCopy.updateTitle(r.label), AboutCopy.updateSub(current, beta, r.date), "update-sheet", onDismiss) {
        if (r.notes.isNotBlank()) NotesBox(r.notes)
        if (r.apkUrl != null) UpdateDownloadControls(r)
        else QusinButton(AboutCopy.UPDATE_DOWNLOAD, { onOpenUrl(r.pageUrl) }, Modifier.fillMaxWidth().padding(top = 14.dp))
        Text(
            AboutCopy.UPDATE_LATER,
            Modifier.fillMaxWidth().padding(top = 8.dp).clip(QusinShapes.Tile).clickable(onClick = onDismiss).padding(vertical = 10.dp),
            style = QusinType.Caption, color = QusinTheme.colors.ink3, textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ChangelogSheet(latest: ReleaseInfo?, onOpenAll: () -> Unit, onDismiss: () -> Unit) {
    val c = QusinTheme.colors
    AboutSheetFrame(AboutCopy.CHANGELOG, null, "changelog-sheet", onDismiss) {
        if (latest == null) {
            Text(AboutCopy.CHANGELOG_EMPTY, Modifier.padding(top = 10.dp), style = QusinType.Caption, color = c.ink3)
        } else {
            Text(
                latest.label + (latest.date?.let { " · $it" } ?: ""), Modifier.padding(top = 10.dp),
                style = QusinType.Label, color = c.ink,
            )
            if (latest.notes.isNotBlank()) NotesBox(latest.notes)
        }
        QusinButton(AboutCopy.CHANGELOG_ALL, onOpenAll, Modifier.fillMaxWidth().padding(top = 14.dp), kind = ButtonKind.Tonal)
    }
}

@Composable
private fun FeedbackSheet(onGitHub: () -> Unit, onDiagnostics: () -> Unit, onDismiss: () -> Unit) {
    AboutSheetFrame(AboutCopy.FEEDBACK, AboutCopy.FEEDBACK_SHEET_SUB, "feedback-sheet", onDismiss) {
        Column(Modifier.padding(top = 6.dp)) {
            FeedbackOption(QusinIcons.Code, AboutCopy.FEEDBACK_GITHUB, AboutCopy.FEEDBACK_GITHUB_SUB, first = true, onGitHub)
            FeedbackOption(QusinIcons.Copy, AboutCopy.DIAGNOSTICS, AboutCopy.DIAGNOSTICS_SUB, first = false, onDiagnostics)
        }
    }
}

@Composable
private fun FeedbackOption(icon: ImageVector, title: String, sub: String, first: Boolean, onClick: () -> Unit) {
    Box(Modifier.testTag("feedback-option")) {
        ListRow(icon, title, sub, first = first, onClick = onClick)
    }
}
