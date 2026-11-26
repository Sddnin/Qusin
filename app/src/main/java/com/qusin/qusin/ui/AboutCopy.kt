package com.qusin.qusin.ui

import com.qusin.qusin.R
import com.qusin.qusin.i18n.tr
import com.qusin.qusin.about.CheckStatus
import com.qusin.qusin.about.QusinLinks
import com.qusin.qusin.about.UpdateState

/** 关于页的文字,以及「检查更新」一行、设置首页「关于 Qusin」一行、侧栏入口上的提示怎么显示。 */
object AboutCopy {
    val TITLE: String get() = tr(R.string.settings_about)
    val TAGLINE: String get() = tr(R.string.about_tagline)
    const val BETA = "Beta"

    val CHECK: String get() = tr(R.string.about_check)
    val CHECK_SUB: String get() = tr(R.string.about_check_sub)
    val CHECK_SUB_NEW: String get() = tr(R.string.about_check_sub_new)
    val UP_TO_DATE: String get() = tr(R.string.about_up_to_date)
    val UP_TO_DATE_TOAST: String get() = tr(R.string.about_up_to_date_toast)
    val OFFLINE: String get() = tr(R.string.about_offline)
    val OFFLINE_TOAST: String get() = tr(R.string.about_offline_toast)
    val CHANGELOG: String get() = tr(R.string.about_changelog)
    val CHANGELOG_SUB: String get() = tr(R.string.about_changelog_sub)
    val CHANGELOG_EMPTY: String get() = tr(R.string.about_changelog_empty)
    val CHANGELOG_ALL: String get() = tr(R.string.about_changelog_all)
    val RELEASE_LATEST: String get() = tr(R.string.about_release_latest)
    val RELEASE_PRERELEASE: String get() = tr(R.string.about_release_prerelease)

    val FEEDBACK: String get() = tr(R.string.about_feedback)
    val FEEDBACK_SUB: String get() = tr(R.string.about_feedback_sub)
    val FEEDBACK_SHEET_SUB: String get() = tr(R.string.about_feedback_sheet_sub)
    val FEEDBACK_GITHUB: String get() = tr(R.string.about_feedback_github)
    val FEEDBACK_GITHUB_SUB: String get() = tr(R.string.about_feedback_github_sub)

    val DIAGNOSTICS: String get() = tr(R.string.diagnostics_copy)
    val DIAGNOSTICS_SUB: String get() = tr(R.string.diagnostics_copy_sub)
    val DIAGNOSTICS_TITLE: String get() = tr(R.string.diagnostics_title)
    val DIAGNOSTICS_NOTE: String get() = tr(R.string.diagnostics_note)
    val DIAGNOSTICS_COPY: String get() = tr(R.string.diagnostics_copy_button)
    val DIAGNOSTICS_SHARE: String get() = tr(R.string.diagnostics_share)
    val DIAGNOSTICS_COPIED: String get() = tr(R.string.diagnostics_copied)
    val DIAGNOSTICS_LOADING: String get() = tr(R.string.diagnostics_loading)

    val REPO: String get() = tr(R.string.about_repo)
    val REPO_SUB: String get() = tr(R.string.about_repo_sub, QusinLinks.REPO_LABEL)
    val GUIDE: String get() = tr(R.string.about_guide)
    val GUIDE_SUB: String get() = tr(R.string.about_guide_sub)

    val DATA_NOTE: String get() = tr(R.string.about_data_note)
    val LICENSE_LINK: String get() = tr(R.string.about_license_link)
    val LICENSE_TITLE: String get() = tr(R.string.about_license_title)

    val UPDATE_DOWNLOAD: String get() = tr(R.string.about_update_download)
    val UPDATE_LATER: String get() = tr(R.string.about_update_later)
    val NO_BROWSER: String get() = tr(R.string.about_no_browser)

    fun version(versionName: String): String = tr(R.string.about_version, versionName)

    fun buildLabel(build: String): String = tr(R.string.about_build, build)

    /** 发布行下面的小字:发布日期(没有时空);滚动发布另写构建编号。 */
    fun releaseSub(r: com.qusin.qusin.about.ReleaseInfo): String? =
        listOfNotNull(r.date, r.build?.let { buildLabel(it) }).joinToString(" · ").ifEmpty { null }

    /** 发布行右边的标记:预发布(nightly)写「预发布」,正式发布写「最新」。 */
    fun releaseBadge(r: com.qusin.qusin.about.ReleaseInfo): String = if (r.prerelease) RELEASE_PRERELEASE else RELEASE_LATEST

    fun available(version: String): String = tr(R.string.about_available, version)
    fun updateTitle(version: String): String = tr(R.string.about_update_title, version)

    /** 「当前 0.1.0 Beta · 2026-11-05 发布」;不知道发布日期时只写当前版本。 */
    fun updateSub(current: String, beta: Boolean, date: String?): String =
        tr(R.string.about_current, current + if (beta) " $BETA" else "") + (date?.let { tr(R.string.about_released_on, it) } ?: "")

    /** 「检查更新」一行右边:正在查转圈,有新版写版本号带红点,查过一次写结果;从没查过什么都不写。 */
    data class CheckValue(val text: String?, val dot: Boolean = false, val spinner: Boolean = false, val highlight: Boolean = false)

    fun checkValue(s: UpdateState): CheckValue = when {
        s.status == CheckStatus.Checking -> CheckValue(null, spinner = true)
        s.update != null -> CheckValue(available(s.update.label), dot = true, highlight = true)
        s.status == CheckStatus.Failed -> CheckValue(OFFLINE)
        s.latest != null -> CheckValue(UP_TO_DATE)
        else -> CheckValue(null)
    }

    /** 设置首页「关于 Qusin」一行右边:有新版写「x 可更新」带红点,不然写当前版本号。 */
    fun settingsValue(updateVersion: String?, versionName: String): CheckValue =
        if (updateVersion != null) CheckValue(available(updateVersion), dot = true, highlight = true)
        else CheckValue(versionName.takeIf { it.isNotEmpty() })

    /** 侧栏「关于 Qusin」上的红点。 */
    fun drawerDot(s: UpdateState): Boolean = s.hasUpdate
}
