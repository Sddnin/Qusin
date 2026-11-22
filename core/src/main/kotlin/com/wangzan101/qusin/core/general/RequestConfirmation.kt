package com.wangzan101.qusin.core.general

import com.wangzan101.qusin.core.gate.ConfirmationRequest
import com.wangzan101.qusin.core.risk.IrrevAction
import com.wangzan101.qusin.core.prompt.Prompts
import com.wangzan101.qusin.core.tool.ArgError
import com.wangzan101.qusin.core.tool.Args
import com.wangzan101.qusin.core.tool.CONFIRM_ACTIONS
import com.wangzan101.qusin.core.tool.CONFIRM_FIELD_MAX
import com.wangzan101.qusin.core.tool.HINT_PAYMENT_TAKEOVER
import com.wangzan101.qusin.core.tool.Outcome
import com.wangzan101.qusin.core.tool.Risk
import com.wangzan101.qusin.core.tool.ToolContext
import com.wangzan101.qusin.core.tool.ToolDef
import com.wangzan101.qusin.core.tool.ToolExecution
import com.wangzan101.qusin.core.tool.Verb
import com.wangzan101.qusin.core.tool.capField
import com.wangzan101.qusin.core.tool.fail
import com.wangzan101.qusin.core.tool.okObj
import com.wangzan101.qusin.core.tool.str
import kotlinx.serialization.json.put
import com.wangzan101.qusin.core.uistate.confirmTarget
import com.wangzan101.qusin.core.uistate.focus
import com.wangzan101.qusin.core.uistate.lastSnapshot
import com.wangzan101.qusin.core.uistate.session

/**
 * 对外、不可逆或未知目标的动作前请用户逐次同意。下单仍须申报屏上应付金额;发送、提交和删除等非付款动作不需要金额。
 * 付款本身不在这里,交给用户亲手付。申报的前台核对用此刻重新读的一屏(不登记编号,不动模型手里的 ref)。
 */
internal object RequestConfirmation : SurfaceAware {
    override val verb = Verb.RequestConfirmation
    override val label = "请你同意"
    override val risk = Risk.Read
    override val def = ToolDef("request_confirmation",
        Prompts.tool("request_confirmation"),
        mapOf(
            "action" to str("Action type: send, submit, delete, order or other. Payment itself must be handed to the user." , CONFIRM_ACTIONS),
            "target" to str("Visible recipient, item, account or target of the action. At most $CONFIRM_FIELD_MAX characters."),
            "content" to str("Exact message or a concise description of what will be submitted, deleted or changed. At most $CONFIRM_FIELD_MAX characters."),
            "amount" to str("Amount and currency due, only when the action spends money."),
            "app" to str(APP_NOTE),
        ), listOf("action", "target", "content", "app"))

    /** app 参数写什么按本任务的屏说:一块屏时写那块的写法,几块屏时各写一句。 */
    override fun def(ctx: ToolContext): ToolDef {
        val base = super.def(ctx)
        val surfaces = ctx.focus.surfaces
        val how = if (surfaces.size == 1) surfaces.single().targetNote
        else surfaces.joinToString("; ") { "on the ${it.id} screen, ${it.targetNote}" }
        return ToolDef(base.name, base.description, base.props + ("app" to str("$APP_NOTE: $how.")), base.required)
    }

    private const val APP_NOTE = "Where the action happens, as on screen now"

    override suspend fun execute(a: Args, ctx: ToolContext): ToolExecution {
        checkSurface(a, ctx)
        val action = a.str("action")!!.trim()
        val kind = IrrevAction.entries.firstOrNull { it.name == action }
            ?: throw ArgError("action", "must be one of ${CONFIRM_ACTIONS.joinToString("|")}")
        if (kind == IrrevAction.pay) {
            ctx.refusedPay = ConfirmationRequest(kind, capField(a.str("target", max = 10000)!!), capField(a.str("content", max = 10000)!!),
                a.str("amount", max = 10000)?.let(::capField), a.str("app")!!.trim())
            return fail("payment_requires_takeover", HINT_PAYMENT_TAKEOVER)
        }
        // 下单必须按屏上金额核对;发送、提交、删除等非付款动作不应编造金额。
        val amount = a.str("amount", max = 10000)?.let(::capField)
        if (kind == IrrevAction.order && amount?.any(Char::isDigit) != true) throw ArgError("amount",
            "required for an order: the amount due shown on screen")
        val app = ctx.confirmTarget(a.str("app")!!.trim())
        val req = ConfirmationRequest(
            kind, capField(a.str("target", max = 10000)!!), capField(a.str("content", max = 10000)!!),
            amount, app,
        )
        val fg = ctx.session.peek()?.packageName ?: ctx.lastSnapshot?.packageName
        return ToolExecution(okObj { put("asked", true) }, outcome = Outcome.Confirm(req, fg))
    }
}
