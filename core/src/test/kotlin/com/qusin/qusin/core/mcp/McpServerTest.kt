package com.qusin.qusin.core.mcp

import com.qusin.qusin.core.assembly.toolDispatcher
import com.qusin.qusin.core.cap.mcp.McpAnnotations
import com.qusin.qusin.core.cap.mcp.McpConnection
import com.qusin.qusin.core.cap.mcp.McpEndpoint
import com.qusin.qusin.core.cap.mcp.McpResult
import com.qusin.qusin.core.cap.mcp.McpToolDescriptor
import com.qusin.qusin.core.cap.mcp.McpTrust
import com.qusin.qusin.core.record.RunEvent
import com.qusin.qusin.core.record.RunSink
import com.qusin.qusin.core.run.Attendance
import com.qusin.qusin.core.run.MemoryRunStore
import com.qusin.qusin.core.run.RunEventBus
import com.qusin.qusin.core.run.RunOrigin
import com.qusin.qusin.core.run.RunPhase
import com.qusin.qusin.core.run.RunSpec
import com.qusin.qusin.core.tool.FakeScreen
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class McpServerTest {
    private class Endpoint : McpEndpoint {
        override val serverId = "fake"
        var calls = 0
        override fun tools() = listOf(
            McpToolDescriptor("read", "Read", buildJsonObject { put("type", "object") }, McpAnnotations(readOnlyHint = true)),
            McpToolDescriptor("send", "Send", buildJsonObject { put("type", "object") }, McpAnnotations(openWorldHint = true)),
        )
        override suspend fun call(name: String, arguments: JsonObject): McpResult { calls++; return McpResult(name) }
    }

    @Test fun lowLevelToolCallsRespectTrustAndEmitEvents() = runBlocking {
        val endpoint = Endpoint()
        val store = MemoryRunStore()
        val server = McpServer({ spec -> toolDispatcher(FakeScreen(), runSpec = spec,
            mcpServers = listOf(McpConnection(endpoint, trust = McpTrust.TrustAnnotations))) }, McpTaskHost { error("not used") }, store)
        val session = server.openToolSession("external-thread")
        val events = mutableListOf<RunEvent>()
        session.events.subscribe(object : RunSink { override fun event(e: RunEvent) { events += e } })
        assertEquals(RunOrigin.Mcp, session.spec.provenance.origin)
        assertEquals(Attendance.Unattended, session.spec.provenance.attendance)
        // Explicitly trusted read-only annotations permit reads in an unattended MCP session.
        val read = session.call("mcp_fake__read", "{}")
        assertEquals("true", read["ok"].toString())
        assertEquals(1, endpoint.calls)

        // openWorldHint means an external side effect: it must be denied before the endpoint runs.
        val send = session.call("mcp_fake__send", "{}")
        assertEquals("false", send["ok"].toString())
        assertEquals("approval_unavailable", send["error"]?.toString()?.trim('"'))
        assertEquals(1, endpoint.calls)
        assertTrue(events.any { it is RunEvent.ToolResult && it.tool == "mcp_fake__send" && "approval_unavailable" in it.resultJson })
        assertFalse(events.any { it is RunEvent.Approval && it.tool == "mcp_fake__send" })
        session.close()
        assertEquals(RunPhase.Ended, store.get(session.spec.id)?.phase)
        assertFalse(store.get(session.spec.id)?.roundInProgress == true)
    }

    @Test fun wholeTaskEntryKeepsMcpOriginAndPushesEvents() {
        var submitted: RunSpec? = null
        val bus = RunEventBus()
        val server = McpServer({ spec -> toolDispatcher(FakeScreen(), runSpec = spec) }, McpTaskHost { spec ->
            submitted = spec
            McpTaskLink(spec.id, bus)
        })
        val link = server.submitTask("查资料", "remote")
        assertEquals(RunOrigin.Mcp, submitted?.provenance?.origin)
        assertEquals(Attendance.Unattended, submitted?.provenance?.attendance)
        val received = mutableListOf<RunEvent>()
        link.events.subscribe(object : RunSink { override fun event(e: RunEvent) { received += e } })
        bus.event(RunEvent.State(1, "progress", "reading"))
        assertEquals(1, received.size)
    }
}
