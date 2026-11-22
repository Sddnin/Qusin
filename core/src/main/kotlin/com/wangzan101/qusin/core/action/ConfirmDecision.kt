package com.wangzan101.qusin.core.action

import com.wangzan101.qusin.core.gate.ConfirmationGate
import com.wangzan101.qusin.core.gate.ConfirmationRequest
import com.wangzan101.qusin.core.risk.IrrevAction
import com.wangzan101.qusin.core.gate.Irreversible
import com.wangzan101.qusin.core.gate.IrreversibleDetector
import com.wangzan101.qusin.core.gate.Permit
import com.wangzan101.qusin.core.uistate.ScreenSnapshot
import com.wangzan101.qusin.core.uistate.interfaces
import com.wangzan101.qusin.core.tool.ToolDispatcher
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import com.wangzan101.qusin.core.tool.Verb
import com.wangzan101.qusin.core.run.Attendance
import com.wangzan101.qusin.core.run.RunOrigin
import com.wangzan101.qusin.core.run.RunProvenance

/** 一次动作执行前的审批结论。 */
sealed interface Decision {
    /** 不用判,或判过放行(含付款时用户的同意对上了)。 */
    data object Clear : Decision
    /** 放行,但找不到点在哪个元素上:运行记录里留痕。 */
    data class Traced(val detail: String) : Decision
    /** 找不到点在哪个元素上,这一来源不盲点。 */
    data object Unresolved : Decision
    /**
     * 这一步要用户这一次的同意(花钱,或 [ask] 不为空时是工具声明要问的不在屏上的一步),没有可用的。
     * 有 [ask] 时调用方能问人就当场问(见 [FollowUp.approve])。
     */
    data class Required(val hit: Irreversible, val ask: ConfirmationRequest? = null) : Decision
    /** 有同意,但和这一步对不上;那次同意已作废。 */
    data class Mismatch(val hit: Irreversible, val reason: String) : Decision
    /** 这一步需要用户同意,但本次运行无法询问用户,直接拒绝。 */
    data class Denied(val hit: Irreversible) : Decision
    /** 作用在 Qusin 自己身上,不做。 */
    data object OwnApp : Decision
}

/**
 * 不可逆判定、审批与同意核对。整个代码库里只有这里调用 [IrreversibleDetector] 的判定和 [ConfirmationGate.permits]
 * (有测试守着),各来源只在 [OriginPolicy] 里写明哪里不同;问不问由 [ApprovalPolicy.decide] 定(高影响动作、付款和工具声明要问的动作)。
 */
class ConfirmDecision(
    private val tools: ToolDispatcher,
    private val confirmations: ConfirmationGate?,
    private val approvals: ApprovalSettings = ApprovalSettings.ASK,
    private val risks: RiskJudge = ClassifiedRisk,
    private val recorder: ActionRecorder? = null,
    private val provenance: RunProvenance = RunProvenance(),
    /** 开工就是自主执行:有用户在场的 UI 运行可跳过逐次确认;无用户的渠道、触发器、MCP 和子任务仍受 [ApprovalPolicy] 硬限制。Qusin 自身界面保护始终生效。 */
    private val autonomous: Boolean = false,
    /** Qusin 自己的包名:作用在它上面的写操作一律不做(不自己给自己点同意、改设置)。 */
    private val selfPackage: String? = null,
) {
    /**
     * 刚判过的这一步是对外动作(发送、提交:判定命中、放行后要做的)时它所在的网站或 App(见 [OutwardPacing.siteOf]),
     * 执行入口据此和上一次对外动作隔开;不是为 null。自主执行时也认(按模型手里那一屏判,不另读屏),节奏不因此失效。
     */
    var outwardSite: String? = null
        private set

    /** 判定放在锁屏等待之后:按此刻重新读的一屏判。 */
    suspend fun judge(tool: String, args: JsonObject, origin: Origin): Decision {
        outwardSite = null
        val target = tools.gateTarget(tool)
        // Qusin 自身保护属于硬边界,自主模式也不能绕过。写屏工具才需要解析目标屏。
        val aim = tools.interfaces.actionTarget(tools.declared(tool), args)
        if (ownApp(tool, args, aim.aimed)) return Decision.OwnApp
        if (autonomous || approvals.autonomous()) {
            val declared = tools.approvalRisk(tool, aim.args)
            // A tool may explicitly declare a payment without exposing a screen target.
            // Payments are always handed to a present user; unattended runs must not reach them.
            val declaredPayment = declared?.takeIf { it.ask?.action == IrrevAction.pay }
            if (declaredPayment != null) {
                val payment = Irreversible(IrrevAction.pay, declaredPayment.ask?.content?.takeIf { it.isNotBlank() } ?: tool)
                return if (ApprovalPolicy.decide(declaredPayment, provenance) == Approval.Deny) Decision.Denied(payment)
                else Decision.Required(payment)
            }
            // A declared external/irreversible/unknown tool must not execute from a run
            // that cannot obtain approval, even when global autonomous mode is enabled.
            if (declared != null && ApprovalPolicy.decide(declared, provenance) == Approval.Deny) {
                return Decision.Denied(asIrreversible(declared, tool))
            }
            // Ordinary navigation and non-risky actions need no fresh screen read.
            // Only refresh the screen when an irreversible target must be classified.
            if (!IrreversibleDetector.needsCheck(target, args)) return Decision.Clear
            val fresh = aim.now()
            if (ownApp(tool, args, fresh)) return Decision.OwnApp
            val hit = IrreversibleDetector.classify(target, aim.args, aim.aimed, fresh)
            val unresolved = IrreversibleDetector.unresolved(target, aim.args, aim.aimed, fresh)
            val cannotAskUser = provenance.origin != RunOrigin.Ui || provenance.attendance == Attendance.Unattended
            val observedRisk = when {
                hit != null -> risks.assess(tools.verb(tool), hit) { fresh ?: aim.aimed }
                unresolved || cannotAskUser -> Risk(ActionRisk.Unknown)
                else -> risks.assess(tools.verb(tool), null) { fresh ?: aim.aimed }
            }
            val risk = stricter(declared, observedRisk)
            if (ApprovalPolicy.decide(risk, provenance) == Approval.Deny) {
                return Decision.Denied(hit ?: asIrreversible(risk, tool))
            }
            // Autonomous UI runs skip one-time approvals, but actual payment controls
            // always require the user to take over and complete the payment themselves.
            if (hit?.action == IrrevAction.pay) return Decision.Required(hit)
            outwardSite = outward(hit, fresh ?: aim.aimed)
            return Decision.Clear
        }
        val policy = origin.policy
        // 这一步落在哪里由界面一侧给出;点看图找到的 @vN 时参数已换成它登记的坐标和找的文字
        val checked = aim.args
        tools.approvalRisk(tool, checked)?.let { declared ->
            return approve(tool, checked, origin, null, declared, null, null, null) ?: Decision.Clear
        }
        if (confirmations == null && !policy.judgeWithoutGate) return Decision.Clear
        val verb = tools.verb(tool)
        val typing = if (verb == Verb.TypeText) (args["text"] as? JsonPrimitive)?.contentOrNull else null
        // 没有可判的不可逆字样的动作(一般改动、界面间移动)不花钱,不用问
        if (!IrreversibleDetector.needsCheck(target, args)) return Decision.Clear
        val snap = aim.aimed
        val fresh = aim.now()
        if (ownApp(tool, args, fresh)) return Decision.OwnApp
        // 复看确认过的所见文字(@vN 才有):读屏里没有文字时(自绘界面)付款的同意按它核对象
        val seen = aim.seen
        val hit = IrreversibleDetector.classify(target, checked, snap, fresh)
        val risk = risks.assess(verb, hit) { fresh ?: snap }
        outwardSite = outward(hit, fresh ?: snap)
        approve(tool, checked, origin, hit, risk, fresh ?: snap, typing, seen)?.let { return it }
        if (hit != null || !IrreversibleDetector.unresolved(target, checked, snap, fresh)) return Decision.Clear
        return if (policy.refuseUnresolved) Decision.Unresolved else Decision.Traced(unresolvedDetail(args, aim.readAgeMs, fresh != null))
    }


    private fun asIrreversible(risk: Risk, fallback: String): Irreversible {
        val ask = risk.ask
        return if (ask != null) Irreversible(ask.action, ask.content.ifBlank { fallback })
        else Irreversible(if (risk.payment) IrrevAction.pay else IrrevAction.other, fallback)
    }

    private fun stricter(declared: Risk?, observed: Risk): Risk {
        if (declared == null) return observed
        val priority = mapOf(
            ActionRisk.Read to 0,
            ActionRisk.Change to 1,
            ActionRisk.External to 2,
            ActionRisk.Irreversible to 3,
            ActionRisk.Unknown to 4,
        )
        return Risk(
            level = if (priority.getValue(declared.level) >= priority.getValue(observed.level)) declared.level else observed.level,
            payment = declared.payment || observed.payment,
            ask = declared.ask ?: observed.ask,
        )
    }

    /** 命中的是发送、提交时这一屏的网站或 App。 */
    private fun outward(hit: Irreversible?, screen: ScreenSnapshot?): String? =
        if (hit?.action == IrrevAction.send || hit?.action == IrrevAction.submit) OutwardPacing.siteOf(screen?.packageName, screen?.web != null) else null

    /** 写操作作用在 Qusin 自己身上:屏上是 Qusin,或要打开 Qusin(含它的应用详情页)。 */
    private fun ownApp(tool: String, args: JsonObject, screen: ScreenSnapshot?): Boolean {
        val self = selfPackage ?: return false
        if (!tools.writesScreen(tool)) return false
        if (screen?.packageName == self) return true
        return listOf("package", "uri").any { k -> (args[k] as? JsonPrimitive)?.contentOrNull?.let { self in it } == true }
    }

    /** 按来源判;需要用户同意时核对一次性授权。放行为 null。 */
    private fun approve(
        tool: String, checked: JsonObject, origin: Origin, hit: Irreversible?, risk: Risk, screen: ScreenSnapshot?,
        typing: String?, seen: String?,
    ): Decision? {
        val policy = origin.policy
        // 没命中不可逆判定的(工具自己声明的风险):有申报按申报记,否则按动作落在的元素记成「其他」
        val asked = hit ?: risk.ask?.let { Irreversible(it.action, it.content) } ?: Irreversible(IrrevAction.other,
            screen?.let { ClassifiedRisk.target(tools.verb(tool), checked, it) }?.name?.trim()?.takeIf { it.isNotEmpty() } ?: tool)
        // 对外、要花钱和没放行的都记下;界面上的改动不记
        fun audit(decision: String) {
            if (decision != "allowed" || risk.level.outward || risk.payment) recorder?.approval(origin, tool, asked, risk, decision)
        }
        when (ApprovalPolicy.decide(risk, provenance)) {
            Approval.Allow -> return null.also { audit("allowed") }
            Approval.Deny -> return Decision.Denied(asked).also { audit("denied") }
            Approval.Ask -> Unit
        }
        val required = Decision.Required(asked, risk.ask)
        val gate = confirmations?.takeIf { policy.usesGrant } ?: return required.also { audit("required") }
        val permit = risk.ask?.let { gate.permitsExactly(it, origin.step) } ?: gate.permits(asked, screen, origin.step, typing, listOfNotNull(seen))
        return when (val p = permit) {
            Permit.Allowed -> null.also { audit("allowed") }
            Permit.NoGrant -> required.also { audit("required") }
            is Permit.Mismatch -> Decision.Mismatch(asked, p.reason).also { audit("mismatch") }
        }
    }

    /** 执行了一个写操作:同意按它的规则作废或保留。 */
    fun noteWrite(tool: String) {
        confirmations?.noteWrite(tools.verb(tool))
    }

    /** 留痕:点的是哪个编号、那次读屏多久以前,或点的坐标。 */
    private fun unresolvedDetail(args: JsonObject, readAgeMs: Long?, freshRead: Boolean): String {
        fun arg(k: String) = args[k]?.let { (it as? JsonPrimitive)?.contentOrNull ?: it.toString() }
        val ref = arg("ref")
        return if (ref != null) {
            val sid = arg("snapshotId")
            "ref=$ref snapshotId=$sid age=${readAgeMs?.let { "${it}ms" } ?: "unknown"}"
        } else "x=${arg("x")},y=${arg("y")} freshRead=$freshRead"
    }

    companion object {
        /**
         * 不读屏的预判(大小模型分工用):按上一次读屏,这一步可能是不可逆的,或不知道点的是什么(看图得来、找不到元素);
         * 这样的一步交主模型做。
         */
        fun mayBeIrreversible(tools: ToolDispatcher, tool: String, args: JsonObject): Boolean {
            val target = tools.gateTarget(tool)
            if (!IrreversibleDetector.needsCheck(target, args)) return false
            val aim = tools.interfaces.actionTarget(tools.declared(tool), args)
            val checked = aim.args
            val snap = aim.aimed
            return aim.bySight || IrreversibleDetector.classify(target, checked, snap) != null ||
                IrreversibleDetector.unresolved(target, checked, snap)
        }
    }
}
