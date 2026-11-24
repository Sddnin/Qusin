package com.wangzan101.qusin.core.knowledge

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.file.Files
import java.util.zip.ZipFile

/** Executes the bundled archive-management helper instead of only checking its manifest. */
class BundledArchiveScriptTest {
    @Test fun archiveManagementScriptCreatesZipAndSha256Manifest() {
        val skill = "skills/archive-management"
        val scriptName = "scripts/create_archive.py"
        val scriptUrl = checkNotNull(javaClass.classLoader.getResource("$skill/$scriptName"))
        val script = File(scriptUrl.toURI())
        assertTrue("bundled script must be a file", script.isFile)

        val temp = Files.createTempDirectory("qusin-archive-script-test-").toFile()
        try {
            val source = File(temp, "source").apply { mkdirs() }
            File(source, "hello.txt").writeText("Qusin archive integration test\n")
            val output = File(temp, "result.zip")
            val process = ProcessBuilder(
                "python3", script.absolutePath, "zip", output.absolutePath, source.absolutePath, "--manifest",
            ).redirectErrorStream(true).start()
            val details = process.inputStream.bufferedReader().use { it.readText() }
            val exit = process.waitFor()

            assertEquals("archive script failed: $details", 0, exit)
            assertTrue("archive output missing: $details", output.isFile)
            ZipFile(output).use { zip ->
                val entry = zip.getEntry("source/hello.txt")
                assertNotNull("source file missing from archive", entry)
                assertEquals(
                    "Qusin archive integration test\n",
                    zip.getInputStream(entry).bufferedReader().use { it.readText() },
                )
            }
            val manifest = File(output.absolutePath + ".manifest.json")
            assertTrue("SHA-256 manifest missing", manifest.isFile)
            assertTrue("manifest must include the archived file", manifest.readText().contains("source/hello.txt"))
        } finally {
            temp.deleteRecursively()
        }
    }
}
