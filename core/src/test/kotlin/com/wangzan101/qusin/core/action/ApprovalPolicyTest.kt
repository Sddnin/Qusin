package com.wangzan101.qusin.core.action

import com.wangzan101.qusin.core.run.Attendance
import com.wangzan101.qusin.core.run.RunOrigin
import com.wangzan101.qusin.core.run.RunProvenance
import org.junit.Assert.assertEquals
import org.junit.Test

class ApprovalPolicyTest {
    @Test fun readAndReversibleChangesDoNotAsk() {
        for (level in listOf(ActionRisk.Read, ActionRisk.Change)) {
            assertEquals("$level", Approval.Allow, ApprovalPolicy.decide(Risk(level)))
        }
    }

    @Test fun highImpactActionsAskWhenTheUserCanApprove() {
        for (level in listOf(ActionRisk.External, ActionRisk.Irreversible, ActionRisk.Unknown)) {
            assertEquals("$level", Approval.Ask, ApprovalPolicy.decide(Risk(level)))
            assertEquals("$level payment", Approval.Ask, ApprovalPolicy.decide(Risk(level, payment = true)))
        }
        assertEquals(Approval.Ask, ApprovalPolicy.decide(Risk(ActionRisk.Change, payment = true)))
    }

    @Test fun highImpactActionsAreDeniedWhenNoUserCanApprove() {
        for (origin in RunOrigin.entries) for (attendance in Attendance.entries) {
            val run = RunProvenance(origin, attendance)
            val canAsk = origin == RunOrigin.Ui && attendance != Attendance.Unattended
            for (level in listOf(ActionRisk.External, ActionRisk.Irreversible, ActionRisk.Unknown)) {
                val expected = if (canAsk) Approval.Ask else Approval.Deny
                assertEquals("$origin $attendance $level", expected, ApprovalPolicy.decide(Risk(level), run))
            }
            val expectedPayment = if (canAsk) Approval.Ask else Approval.Deny
            assertEquals("$origin $attendance payment", expectedPayment, ApprovalPolicy.decide(Risk(ActionRisk.Read, payment = true), run))
        }
    }

    @Test fun declaredOffScreenActionsStillRequireApproval() {
        val request = com.wangzan101.qusin.core.gate.ConfirmationRequest(
            com.wangzan101.qusin.core.risk.IrrevAction.delete, "folder", "delete folder", null, "com.example.app",
        )
        assertEquals(Approval.Ask, ApprovalPolicy.decide(Risk(ActionRisk.Change, ask = request)))
        assertEquals(Approval.Deny, ApprovalPolicy.decide(Risk(ActionRisk.Change, ask = request), RunProvenance(RunOrigin.Mcp)))
    }
}
