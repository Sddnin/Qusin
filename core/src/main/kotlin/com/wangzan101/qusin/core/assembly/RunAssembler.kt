package com.wangzan101.qusin.core.assembly

import com.wangzan101.qusin.core.capability.Capability
import com.wangzan101.qusin.core.agent.CoreSections
import com.wangzan101.qusin.core.general.GeneralGroup
import com.wangzan101.qusin.core.run.RunSpec
import com.wangzan101.qusin.core.run.Attendance
import com.wangzan101.qusin.core.run.RunOrigin
import com.wangzan101.qusin.core.tool.Verb
import com.wangzan101.qusin.core.tool.Risk
import com.wangzan101.qusin.core.tool.ToolConfig
import com.wangzan101.qusin.core.tool.ToolDispatcher
import com.wangzan101.qusin.core.uistate.Interfaces
import com.wangzan101.qusin.core.uistate.Surface
import com.wangzan101.qusin.core.uistate.ScreenFamily
import com.wangzan101.qusin.core.uistate.ScreenPort
import com.wangzan101.qusin.core.uistate.ScreenSnapshot
import com.wangzan101.qusin.core.uistate.ActionResult

/** 每个 Run 在开局由 Provider 打开能力；Provider 捕获自己的端口与资源，不进入核心循环。 */
internal interface CapabilityProvider {
    val id: String
    fun open(spec: RunSpec): Capability?
}

internal class RunAssembler(
    private val providers: List<CapabilityProvider>,
) {
    fun assemble(spec: RunSpec, config: ToolConfig, stableRefs: Boolean,
        formerPrefix: Map<String, String>, renamed: Map<String, String>,
        retired: Map<String, String>, moved: Map<String, List<String>>): ToolDispatcher {
        check(providers.map { it.id }.distinct().size == providers.size) { "duplicate capability provider ids" }
        val child = spec.provenance.origin == RunOrigin.Child
        val readOnly = spec.readOnly || child
        val capabilities = providers.filter { (spec.capabilities == null || it.id in spec.capabilities) &&
            (!child || it.id in setOf("web", "sandbox")) }
            .mapNotNull { it.open(spec) }
        val general = GeneralGroup.tools.filterNot { (spec.readOnly || child) && it.verb == Verb.MemoryEdit }
            .filterNot { spec.provenance.attendance == Attendance.Unattended &&
            it.verb in setOf(Verb.AskUser, Verb.RequestConfirmation, Verb.HandToUser, Verb.ScheduleTask, Verb.ProfileEdit, Verb.SkillSave, Verb.MemoryEdit) }
            .filterNot { spec.provenance.origin == RunOrigin.Child && it.verb in setOf(Verb.SpawnAgent, Verb.MemorySave, Verb.MemoryEdit, Verb.ProfileEdit, Verb.ScheduleTask, Verb.SkillSave,
                Verb.AskUser, Verb.RequestConfirmation, Verb.HandToUser) }
            .filter { !readOnly || it.risk == Risk.Read }
        val capabilityTools = capabilities.flatMap { it.tools() }.filter { !readOnly || it.risk == Risk.Read ||
            it.capability == "web" && it.verb == Verb.Navigate }
        // Sandbox-only / headless runs still need a UI context for shared dispatcher plumbing.
        // This internal surface is inert and is not a capability, so it exposes no phone tools.
        val surfaces = capabilities.mapNotNull { it.surface }.ifEmpty { listOf(INTERNAL_SURFACE) }
        return ToolDispatcher(capabilities, general + capabilityTools,
            listOf(CoreSections) + capabilities, config, { ctx -> Interfaces.attach(ctx, surfaces, stableRefs) },
            formerPrefix, renamed, retired, moved)
    }
}


private val INTERNAL_SURFACE = Surface(
    family = ScreenFamily(id = "internal", snapshotPrefix = "i"),
    screen = InternalNoSurfacePort,
    declaredTarget = { it },
    targetNote = "internal context",
)

/** No-op screen port used only to satisfy shared UI state in headless runs. */
private object InternalNoSurfacePort : ScreenPort {
    private val empty = ScreenSnapshot("internal", null, emptyList(), 1, 1, hasWindow = false)
    private fun unavailable() = ActionResult.Failed("no_surface")

    override suspend fun snapshot(): ScreenSnapshot = empty
    override suspend fun screenshotPng(): ByteArray? = null
    override suspend fun tapRef(ref: String, snapshotId: String): ActionResult = unavailable()
    override suspend fun tapXY(x: Int, y: Int): ActionResult = unavailable()
    override suspend fun typeText(ref: String?, snapshotId: String?, text: String): ActionResult = unavailable()
    override suspend fun clearText(ref: String?, snapshotId: String?): ActionResult = unavailable()
    override suspend fun scroll(ref: String?, snapshotId: String?, down: Boolean): ActionResult = unavailable()
    override suspend fun swipe(x1: Int, y1: Int, x2: Int, y2: Int, durationMs: Long): ActionResult = unavailable()
    override suspend fun back(): ActionResult = unavailable()
    override suspend fun pressKey(keyCode: Int): ActionResult = unavailable()
    override suspend fun openApp(packageName: String, cold: Boolean): ActionResult = unavailable()
    override suspend fun waitStable(timeoutMs: Long): Boolean = false
    override suspend fun listApps(): List<Pair<String, String>> = emptyList()
}
