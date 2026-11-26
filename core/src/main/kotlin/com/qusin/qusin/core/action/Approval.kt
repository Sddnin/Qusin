package com.qusin.qusin.core.action

import com.qusin.qusin.core.gate.ConfirmationRequest
import com.qusin.qusin.core.run.Attendance
import com.qusin.qusin.core.run.RunOrigin
import com.qusin.qusin.core.run.RunProvenance

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
    /** 自主模式:跳过非付款动作的逐次确认;支付仍交给用户亲手完成。 */
    fun autonomous(): Boolean

    companion object {
        /** 询问(默认):只有会花钱的步骤要求逐次确认;其他已授权动作直接执行。 */
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
 * 审批判定:纯函数。手机 UI 中,用户明确要求的免费发送、提交等对外动作直接执行;
 * 来自 MCP、渠道、触发器、子任务或无人值守运行的对外/不可逆动作一律拒绝。
 * 只有可交给在场用户确认的花钱步骤等待确认;工具明确声明的额外确认也不能绕过。
 */
object ApprovalPolicy {
    fun decide(risk: Risk, run: RunProvenance = RunProvenance()): Approval = when {
        // Free outward actions are only relaxed for an attended phone UI run.
        // MCP and other non-UI origins must never perform external side effects,
        // even when the run is marked autonomous. An unattended run cannot act outward either.
        (run.origin != RunOrigin.Ui || run.attendance == Attendance.Unattended) &&
            (risk.payment || risk.ask != null || risk.level.outward) -> Approval.Deny
        !risk.payment && risk.ask == null -> Approval.Allow
        else -> Approval.Ask
    }
}
