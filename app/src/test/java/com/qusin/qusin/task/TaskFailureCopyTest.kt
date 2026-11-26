package com.qusin.qusin.task

import com.qusin.qusin.core.agent.FailKind
import com.qusin.qusin.core.agent.FailureCode
import com.qusin.qusin.core.agent.TaskState
import com.qusin.qusin.i18n.ResFileTexts
import com.qusin.qusin.i18n.UiText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class TaskFailureCopyTest {
    @Test fun rateLimitUsesTheSelectedLanguage() {
        val previous = UiText.source
        try {
            val failure = TaskState.Failed("中文", FailKind.Model, FailureCode.ModelRateLimited)
            UiText.source = ResFileTexts("values-ar", Locale.forLanguageTag("ar"))
            val arabic = localizeTaskFailure(failure) as TaskState.Failed
            assertTrue(arabic.reason.contains("خدمة النموذج"))
            assertEquals(FailKind.Model, arabic.kind)
            assertEquals(FailureCode.ModelRateLimited, arabic.code)
            UiText.source = ResFileTexts.english
            assertTrue((localizeTaskFailure(failure) as TaskState.Failed).reason.contains("rate limit"))
            UiText.source = ResFileTexts.chinese
            assertTrue((localizeTaskFailure(failure) as TaskState.Failed).reason.contains("模型服务限流"))
        } finally {
            UiText.source = previous
        }
    }

    @Test fun modelAuthUsesTheSelectedLanguageInNotificationsAndResults() {
        val previous = UiText.source
        try {
            val failure = TaskState.Failed("模型服务拒绝了请求:API Key 无效", FailKind.Model, FailureCode.ModelAuth)
            UiText.source = ResFileTexts("values-vi", Locale.forLanguageTag("vi"))
            val vietnamese = localizeTaskFailure(failure) as TaskState.Failed
            assertTrue(vietnamese.reason.contains("Dịch vụ mô hình từ chối API key"))
            assertEquals(vietnamese.reason, TaskService.describe(failure).second)
            assertEquals(FailureCode.ModelAuth, vietnamese.code)
            UiText.source = ResFileTexts.chinese
            assertTrue((localizeTaskFailure(failure) as TaskState.Failed).reason.contains("模型服务拒绝了 API Key"))
        } finally {
            UiText.source = previous
        }
    }

    @Test fun unstructuredFailuresAndSuccessfulAnswersRemainUntouched() {
        val failure = TaskState.Failed("User-provided explanation", FailKind.GaveUp)
        val success = TaskState.Succeeded("answer")
        assertSame(failure, localizeTaskFailure(failure))
        assertSame(success, localizeTaskFailure(success))
    }

    @Test fun chineseReasonsWithoutACodeNeverReachANonChineseUi() {
        val previous = UiText.source
        try {
            UiText.source = ResFileTexts("values-vi", Locale.forLanguageTag("vi"))
            // 丢了 code 的模型错误:按开头的字认出来
            val auth = TaskState.Failed("模型服务拒绝了请求:API Key 无效或没有权限。", FailKind.Model)
            assertTrue((localizeTaskFailure(auth) as TaskState.Failed).reason.contains("Dịch vụ mô hình từ chối API key"))
            // 别的中文原因:换成越南语的通用说明
            val guard = TaskState.Failed("已安全停止(repeating_action):同一个操作反复执行仍没有效果。", FailKind.Guard)
            val generic = (localizeTaskFailure(guard) as TaskState.Failed).reason
            assertTrue(generic.none { it in '\u4e00'..'\u9fff' })
            assertTrue(generic.contains("Tác vụ"))
            // 中文界面保持原文
            UiText.source = ResFileTexts.chinese
            assertSame(guard, localizeTaskFailure(guard))
        } finally {
            UiText.source = previous
        }
    }
}
