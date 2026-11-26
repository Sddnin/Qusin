package com.qusin.qusin.core.agent

import com.qusin.qusin.core.assembly.toolDispatcher
import com.qusin.qusin.core.testing.fakeModel
import com.qusin.qusin.core.tool.FakeScreen
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class PromptTransformTest {
    @Test fun promptTransformAppliesToSystemPrompt() = runTest {
        val m = fakeModel { answer("hi") }
        AgentLoop(m, toolDispatcher(FakeScreen()), SystemPrompt.identity(), promptTransform = { "X" }).run("t")
        assertEquals("X", m.requests.single().system)
    }
}
