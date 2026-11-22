package com.wangzan101.qusin.core.cap.transcribe

import com.wangzan101.qusin.core.tool.AgentTool

internal object TranscribeGroup {
    val tools: List<AgentTool> = listOf(AudioTranscribe)
}
