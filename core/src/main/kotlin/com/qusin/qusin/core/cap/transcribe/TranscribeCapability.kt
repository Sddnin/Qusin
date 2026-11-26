package com.qusin.qusin.core.cap.transcribe

import com.qusin.qusin.core.capability.Capability
import com.qusin.qusin.core.capability.CapabilityKey
import com.qusin.qusin.core.capability.CapabilityStatus
import com.qusin.qusin.core.capability.PromptEnv
import com.qusin.qusin.core.prompt.Prompts
import com.qusin.qusin.core.tool.AgentTool
import com.qusin.qusin.core.tool.ToolContext

/** 转写能力:把工作区里的音频文件转成文字。没有配置服务商转写模型时整个能力不装。 */
const val TRANSCRIBE = "transcribe"

internal class TranscribeCapability(val port: TranscribePort?) : Capability {
    override val id = TRANSCRIBE
    override val folds = true

    override fun status(): CapabilityStatus =
        if (port != null) CapabilityStatus.Available else CapabilityStatus.Unavailable("no transcription model")

    override fun tools(): List<AgentTool> = TranscribeGroup.tools

    override fun sections(env: PromptEnv): List<String> =
        if (env.isOpen(id) && tools().any { it.name in env.offered }) listOf(env.gate(Prompts.capability(TRANSCRIBE))) else emptyList()

    override fun onTaskStart(ctx: ToolContext) {
        ctx[KEY] = this
    }

    companion object {
        val KEY = CapabilityKey<TranscribeCapability>(TRANSCRIBE)
    }
}
