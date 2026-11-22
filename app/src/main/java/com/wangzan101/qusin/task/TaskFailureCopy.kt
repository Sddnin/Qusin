package com.wangzan101.qusin.task

import com.wangzan101.qusin.R
import com.wangzan101.qusin.core.agent.FailureCode
import com.wangzan101.qusin.core.agent.TaskState
import com.wangzan101.qusin.i18n.tr

/** Translate structured failures before publishing or saving the result. */
internal fun localizeTaskFailure(state: TaskState): TaskState {
    if (state !is TaskState.Failed) return state
    val reason = when (state.code) {
        FailureCode.ModelRateLimited -> tr(R.string.task_model_rate_limited)
        null -> return state
    }
    return state.copy(reason = reason)
}
