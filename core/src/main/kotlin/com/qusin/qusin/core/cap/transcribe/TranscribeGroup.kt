package com.qusin.qusin.core.cap.transcribe

import com.qusin.qusin.core.tool.AgentTool

internal object TranscribeGroup {
    val tools: List<AgentTool> = listOf(AudioTranscribe)
}
