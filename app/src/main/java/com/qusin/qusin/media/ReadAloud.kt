package com.qusin.qusin.media

import com.qusin.qusin.core.model.TtsCapabilities
import com.qusin.qusin.core.model.TtsModel
import com.qusin.qusin.core.model.TtsRequest
import com.qusin.qusin.core.model.TtsResponse
import com.qusin.qusin.settings.VoiceChoice
import com.qusin.qusin.settings.VoiceSettings

/** Check the current master switch before resolving a provider or synthesizing speech. */
internal suspend fun readAloud(
    text: String,
    settings: VoiceSettings,
    model: (VoiceChoice, TtsCapabilities) -> TtsModel?,
    play: suspend (TtsResponse) -> Unit,
) {
    val state = settings.state.value
    if (!state.enabled) return
    val choice = state.choice ?: return
    val caps = settings.capabilities() ?: return
    val tts = model(choice, caps) ?: return
    val response = tts.synthesize(
        TtsRequest(text.take(state.maxCharsPerSpeak), choice.voice.ifBlank { null }, choice.speed, "wav"),
    )
    play(response)
}
