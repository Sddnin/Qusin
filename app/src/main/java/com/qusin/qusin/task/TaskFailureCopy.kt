package com.qusin.qusin.task

import com.qusin.qusin.R
import com.qusin.qusin.core.agent.FailureCode
import com.qusin.qusin.core.agent.TaskState
import com.qusin.qusin.i18n.tr
import com.qusin.qusin.i18n.uiLocale

/**
 * Translate failures before publishing or saving the result.
 *
 * Structured failures ([FailureCode]) get their own text. The core still writes every other reason in Chinese,
 * and a failure can reach here without its code (rebuilt from storage, copied by a helper), so two safety nets
 * keep Chinese out of a non-Chinese UI: the two known model messages are recognised by their opening words,
 * and any other reason that still contains Chinese characters is replaced by a generic line in the app language.
 */
internal fun localizeTaskFailure(state: TaskState): TaskState {
    if (state !is TaskState.Failed) return state
    val code = state.code ?: recognizedCode(state.reason)
    val reason = when (code) {
        FailureCode.ModelRateLimited -> tr(R.string.task_model_rate_limited)
        FailureCode.ModelAuth -> tr(R.string.task_model_auth)
        null -> return if (needsGenericText(state.reason)) state.copy(reason = tr(R.string.task_failure_generic)) else state
    }
    return state.copy(reason = reason, code = code)
}

/** The opening words of the Chinese texts that `userReason` writes for the two coded failures. */
private fun recognizedCode(reason: String): FailureCode? = when {
    reason.startsWith("模型服务拒绝了请求") -> FailureCode.ModelAuth
    reason.startsWith("模型服务限流") -> FailureCode.ModelRateLimited
    else -> null
}

private fun needsGenericText(reason: String): Boolean =
    reason.any(::isCjk) && !uiLocale().language.equals("zh", ignoreCase = true)

private fun isCjk(c: Char): Boolean = Character.UnicodeScript.of(c.code) == Character.UnicodeScript.HAN
