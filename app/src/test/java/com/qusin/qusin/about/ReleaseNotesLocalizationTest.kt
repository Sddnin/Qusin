package com.qusin.qusin.about

import org.junit.Assert.assertEquals
import org.junit.Test

class ReleaseNotesLocalizationTest {
    private val notes = """
        <!-- qusin-lang: en -->
        ## What's new
        English update notes.
        <!-- /qusin-lang -->

        <!-- qusin-lang: vi -->
        ## Có gì mới
        Ghi chú cập nhật tiếng Việt.
        <!-- /qusin-lang -->

        <!-- qusin-lang: zh-Hant -->
        ## 更新內容
        繁體中文更新說明。
        <!-- /qusin-lang -->
    """.trimIndent()

    @Test fun selectsRequestedLanguage() {
        assertEquals("## Có gì mới\nGhi chú cập nhật tiếng Việt.", ReleaseNotesLocalization.select(notes, "vi-VN"))
    }

    @Test fun fallsBackToEnglishWhenLanguageIsMissing() {
        assertEquals("## What's new\nEnglish update notes.", ReleaseNotesLocalization.select(notes, "fr-FR"))
    }

    @Test fun recognizesTraditionalChineseLocaleAliases() {
        assertEquals("## 更新內容\n繁體中文更新說明。", ReleaseNotesLocalization.select(notes, "zh-TW"))
    }

    @Test fun leavesUnmarkedReleaseBodiesUntouched() {
        assertEquals("A legacy release note.", ReleaseNotesLocalization.select(" A legacy release note. ", "vi"))
    }
}
