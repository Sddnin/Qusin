package com.wangzan101.qusin.settings

import com.wangzan101.qusin.core.action.ApprovalSettings
import com.wangzan101.qusin.core.gate.ConfirmationGate

/**
 * 一个任务自己的审批设置:自主与否是开工时取全局的,之后跟着开发者选项里的「自主模式」变([set])。
 * 询问时只有花钱的步骤需要逐次确认;自主模式跳过付款前的确认,但付款仍由用户亲手完成。
 */
class RunApproval(startAutonomous: Boolean) : ApprovalSettings {
    /** 开工时就是自主:这个任务没有确认关卡,也不给 request_confirmation;中途关掉自主留给下一个任务。 */
    val startedAutonomous: Boolean = startAutonomous
    @Volatile private var auto = startAutonomous

    override fun autonomous(): Boolean = auto

    fun set(autonomous: Boolean) {
        auto = autonomous
    }

    /** 这个任务(和它的子任务)用的一次性动作确认关卡:开工时是询问才有,每次一份新的。 */
    fun gate(): ConfirmationGate? = if (startedAutonomous) null else ConfirmationGate()
}

/**
 * 询问 / 自主是一个全局开关,存在 [ApprovalStore.autonomous],默认询问。每个任务开工时拿一份自己的([start]);
 * 换开关([setAutonomous]):存下来管之后的任务,正在跑的任务也跟着换(从询问换成自主,下一步起按新设置执行)。
 */
class ApprovalControl(private val store: ApprovalStore) {
    private val runs = mutableSetOf<RunApproval>()

    /**
     * [alwaysAsk] 为 true 的任务不管开关,一律按询问开工、也不跟着开关变。两种:
     * - 电脑经 MCP 交来的任务:付款和不可逆操作在这类非手机运行里不会执行,远端没有办法让它们获得手机上的确认。
     * - 对话里有通知提醒的任务(见 [com.wangzan101.qusin.core.agent.NoticeTurn]):任务从询问模式开始,付款仍需要用户确认;不花钱的已授权操作按统一策略直接执行。
     */
    @Synchronized fun start(alwaysAsk: Boolean = false): RunApproval =
        if (alwaysAsk) RunApproval(false) else RunApproval(store.autonomous.value).also { runs += it }

    @Synchronized fun end(run: RunApproval) {
        runs -= run
    }

    @Synchronized fun setAutonomous(on: Boolean) {
        store.setAutonomous(on)
        runs.forEach { it.set(on) }
    }
}
