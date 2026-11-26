package com.qusin.qusin.core.action

import com.qusin.qusin.core.run.Attendance
import com.qusin.qusin.core.run.RunOrigin
import com.qusin.qusin.core.run.RunProvenance
import org.junit.Assert.assertEquals
import org.junit.Test

class ApprovalPolicyTest {
    @Test fun onlyPaymentAsks() {
        for (level in ActionRisk.entries) {
            assertEquals("$level", Approval.Allow, ApprovalPolicy.decide(Risk(level)))
            assertEquals("$level payment", Approval.Ask, ApprovalPolicy.decide(Risk(level, payment = true)))
        }
    }

    @Test fun everyOriginAndAttendance() {
        for (origin in RunOrigin.entries) for (attendance in Attendance.entries) for (level in ActionRisk.entries) {
            val run = RunProvenance(origin, attendance)
            val cannotPerformOutwardAction = origin != RunOrigin.Ui || attendance == Attendance.Unattended
            val noMoney = if (level.outward && cannotPerformOutwardAction) Approval.Deny else Approval.Allow
            assertEquals("$origin $attendance $level", noMoney, ApprovalPolicy.decide(Risk(level), run))
            val expected = if (origin == RunOrigin.Ui && attendance != Attendance.Unattended) Approval.Ask else Approval.Deny
            assertEquals("$origin $attendance $level payment", expected, ApprovalPolicy.decide(Risk(level, payment = true), run))
        }
    }

    @Test fun outwardActionsAreAllowedOnlyFromAnAttendedPhoneUi() {
        assertEquals(
            Approval.Allow,
            ApprovalPolicy.decide(Risk(ActionRisk.External), RunProvenance(RunOrigin.Ui, Attendance.Present)),
        )
        assertEquals(
            Approval.Deny,
            ApprovalPolicy.decide(Risk(ActionRisk.External), RunProvenance(RunOrigin.Mcp, Attendance.Present)),
        )
        assertEquals(
            Approval.Deny,
            ApprovalPolicy.decide(Risk(ActionRisk.External), RunProvenance(RunOrigin.Mcp, Attendance.Unattended)),
        )
        assertEquals(
            Approval.Deny,
            ApprovalPolicy.decide(Risk(ActionRisk.External), RunProvenance(RunOrigin.Ui, Attendance.Unattended)),
        )
    }

    @Test fun declaredOffScreenActionsStillRequireApproval() {
        val request = com.qusin.qusin.core.gate.ConfirmationRequest(
            com.qusin.qusin.core.risk.IrrevAction.delete, "folder", "delete folder", null, "com.example.app",
        )
        assertEquals(Approval.Ask, ApprovalPolicy.decide(Risk(ActionRisk.Change, ask = request)))
        assertEquals(Approval.Deny, ApprovalPolicy.decide(Risk(ActionRisk.Change, ask = request), RunProvenance(RunOrigin.Mcp)))
    }
}
