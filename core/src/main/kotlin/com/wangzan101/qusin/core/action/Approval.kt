package com.wangzan101.qusin.core.action

import com.wangzan101.qusin.core.gate.ConfirmationRequest
import com.wangzan101.qusin.core.run.Attendance
import com.wangzan101.qusin.core.run.RunOrigin
import com.wangzan101.qusin.core.run.RunProvenance

/**
 * 一个动作的风险等级(记进审批记录,核查据此知道任务做过对外的事)。[Read] 只看或只在界面间移动;[Change] 在界面上改东西(点、打字);
 * [External] 把东西交给别人(发送、提交);[Irreversible] 做了收不回(付款、下单、删除);[Unknown] 看不清点的是什么。
 */
enum class ActionRisk { Read, Change, External, Irreversible, Unknown;

    /** 会影响屏幕之外的世界(或拿不准会不会)。 */
    val outward: Boolean get() = this == External || this == Irreversible || this == Unknown
}

/**
 * [payment] 为 true 时这一步要花钱(付款、下单,或屏上有应付金额时的对外动作):询问时先问用户。
 * [ask] 不为空时是不在屏上、改了收不回的一步(工具按这次的参数声明,如会改动东西的 root 命令),询问时也先问用户,
 * 给用户看的就是它;同意只放行一字不差的这一步。
 */
data class Risk(val level: ActionRisk, val payment: Boolean = false, val ask: ConfirmationRequest? = null)

/** 用户在确认卡片上的选择:同意这一次,或不同意。 */
enum class ConfirmChoice { Once, Deny;

    val approved: Boolean get() = this != Deny
}

/** 询问 / 自主:此刻是不是自主模式(中途换成自主时,正在跑的任务从下一步起照它)。由设置提供,测试里换成固定值。 */
fun interface ApprovalSettings {
    /** 自主模式:跳过对外和不可逆动作的逐次确认。 */
    fun autonomous(): Boolean

    companion object {
        /** 询问(默认):对外、不可逆和工具声明要问的动作(见 [Risk.ask])先问。 */
        val ASK = ApprovalSettings { false }
    }
}

sealed interface Approval {
    /** 不用问。 */
    data object Allow : Approval
    /** 要用户这一次的同意(有一次性同意就核对并用掉它)。 */
    data object Ask : Approval
    /** 本次运行不能向用户要同意，动作必须停下。 */
    data object Deny : Approval
}

/**
 * 审批判定:纯函数。询问模式下,对外动作(发送、发布、提交)、不可逆动作(删除、下单)和未知目标都要用户逐次同意。
 * 花钱仍走原来的专门流程,付款本身交给用户亲手完成。没有用户可问的后台、渠道和 MCP 运行,上述动作一律不做。
 */
object ApprovalPolicy {
    fun decide(risk: Risk, run: RunProvenance = RunProvenance()): Approval {
        val needsUser = risk.payment || risk.ask != null || risk.level.outward
        if (!needsUser) return Approval.Allow
        if (run.origin != RunOrigin.Ui || run.attendance == Attendance.Unattended) return Approval.Deny
        return Approval.Ask
    }
}
