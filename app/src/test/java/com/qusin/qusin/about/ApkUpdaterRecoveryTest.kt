package com.qusin.qusin.about

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.file.Files

class ApkUpdaterRecoveryTest {
    @Test
    fun sameReleaseKeepsDownloadIdentityWhenGithubAssetUrlChanges() {
        val original = ReleaseInfo(
            version = "0.1.53", name = "Qusin 0.1.53", notes = "", date = null,
            pageUrl = "https://github.com/Sddnin/Qusin/releases/tag/v0.1.53",
            apkUrl = "https://github.com/Sddnin/Qusin/releases/download/v0.1.53/Qusin-v0.1.53.apk",
        )
        val refreshed = original.copy(apkUrl = "https://github.com/Sddnin/Qusin/releases/download/v0.1.53/Qusin-v0.1.53.apk?download=1")

        assertEquals(apkReleaseIdentity(original), apkReleaseIdentity(refreshed))
        assertTrue(apkReleaseIdentity(original) != apkReleaseIdentity(original.copy(version = "0.1.54")))
        assertTrue(apkReleaseIdentity(original.copy(build = "abc123")) != apkReleaseIdentity(original.copy(build = "def456")))
    }

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
