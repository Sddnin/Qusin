package com.wangzan101.qusin.about

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File
import java.nio.file.Files

class ApkUpdaterRecoveryTest {
    @Test
    fun completedApkFileRestoresInstallReadyState() {
        val apk = Files.createTempFile("qusin-update", ".apk").toFile()
        try {
            apk.writeBytes(byteArrayOf(1, 2, 3, 4))
            assertEquals(
                ApkDownloadState(DownloadPhase.Ready, bytes = 4, total = 4),
                restoredApkDownloadState(apk),
            )
        } finally {
            apk.delete()
        }
    }

    @Test
    fun missingOrEmptyApkDoesNotAppearReadyToInstall() {
        val directory = Files.createTempDirectory("qusin-update").toFile()
        try {
            val empty = File(directory, "empty.apk").apply { createNewFile() }
            val missing = File(directory, "missing.apk")
            assertEquals(ApkDownloadState(DownloadPhase.Failed), restoredApkDownloadState(empty))
            assertEquals(ApkDownloadState(DownloadPhase.Failed), restoredApkDownloadState(missing))
        } finally {
            directory.deleteRecursively()
        }
    }
}
