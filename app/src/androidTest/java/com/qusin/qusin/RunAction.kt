package com.qusin.qusin

import com.qusin.qusin.core.action.ActionExecutor
import com.qusin.qusin.core.action.ActionRecorder
import com.qusin.qusin.core.action.Origin
import com.qusin.qusin.core.action.execution
import com.qusin.qusin.core.guard.OperationGuards
import com.qusin.qusin.core.model.Part
import com.qusin.qusin.core.record.RunSink
import com.qusin.qusin.core.tool.ToolDispatcher
import com.qusin.qusin.core.tool.ToolExecution

/** 设备测试里直接做一个工具调用:和产品一样经执行入口(不配确认关卡,不等锁屏)。 */
suspend fun ToolDispatcher.runAction(call: Part.ToolCall): ToolExecution =
    ActionExecutor(this, OperationGuards(), null, gate = {}, recorder = ActionRecorder(RunSink.NONE)).run(call, Origin.Model(0)).execution()
