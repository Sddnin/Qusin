package com.wangzan101.qusin.core.knowledge

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.file.Files
import java.util.zip.ZipFile

/** Runs the file-generation helpers shipped with skills against small temporary fixtures. */
class BundledFileSkillsScriptsTest {
    private fun bundled(skill: String, script: String): File {
        val url = checkNotNull(javaClass.classLoader.getResource("$skill/$script"))
        return File(url.toURI()).also { assertTrue("missing bundled script $skill/$script", it.isFile) }
    }

    private fun run(script: File, vararg args: String): Pair<Int, String> {
        val process = ProcessBuilder(listOf("python3", script.absolutePath) + args)
            .redirectErrorStream(true)
            .start()
        val output = process.inputStream.bufferedReader().use { it.readText() }
        return process.waitFor() to output
    }

    @Test fun createPowerPointScriptBuildsAnOpenablePresentation() {
        val script = bundled("skills/presentations", "scripts/create_pptx.py")
        val temp = Files.createTempDirectory("qusin-pptx-script-test-").toFile()
        try {
            val input = File(temp, "input.json").apply {
                writeText("""{"title":"Qusin test","slides":[{"title":"First slide","bullets":["One","Two"]}]}""")
            }
            val output = File(temp, "test.pptx")
            val (exit, details) = run(script, input.absolutePath, output.absolutePath)
            assertEquals(details, 0, exit)
            assertTrue(details, output.isFile && output.length() > 0)
            ZipFile(output).use { zip ->
                val names = zip.entries().asSequence().map { it.name }.toSet()
                assertTrue(names.contains("ppt/presentation.xml"))
                assertTrue(names.contains("ppt/slides/slide1.xml"))
                assertTrue(zip.getInputStream(zip.getEntry("ppt/presentation.xml")).use { it.readBytes().isNotEmpty() })
            }
        } finally { temp.deleteRecursively() }
    }

    @Test fun createEpubScriptBuildsAnEpub3Archive() {
        val script = bundled("skills/ebook-files", "scripts/create_epub.py")
        val temp = Files.createTempDirectory("qusin-epub-script-test-").toFile()
        try {
            val input = File(temp, "book.md").apply { writeText("# Qusin\n\nA short test chapter.") }
            val output = File(temp, "book.epub")
            val (exit, details) = run(script, input.absolutePath, output.absolutePath, "--title", "Qusin test", "--language", "en")
            assertEquals(details, 0, exit)
            ZipFile(output).use { zip ->
                assertEquals("application/epub+zip", zip.getInputStream(zip.getEntry("mimetype")).bufferedReader().readText())
                assertTrue(zip.getInputStream(zip.getEntry("OEBPS/chapter.xhtml")).bufferedReader().readText().contains("A short test chapter."))
            }
        } finally { temp.deleteRecursively() }
    }

    @Test fun csvToSqliteScriptImportsRowsAndChecksIntegrity() {
        val script = bundled("skills/database-files", "scripts/csv_to_sqlite.py")
        val temp = Files.createTempDirectory("qusin-sqlite-script-test-").toFile()
        try {
            val input = File(temp, "records.csv").apply { writeText("name,value\nalpha,1\nbeta,2\n") }
            val output = File(temp, "records.sqlite")
            val (exit, details) = run(script, input.absolutePath, output.absolutePath)
            assertEquals(details, 0, exit)
            assertTrue(details, output.isFile && output.length() > 0)
            assertTrue(details, details.contains("\"rows\": 2") && details.contains("\"integrity_check\": \"ok\""))
        } finally { temp.deleteRecursively() }
    }

    @Test fun checkFileScriptInspectsAPlainTextOutput() {
        val script = bundled("skills/create-files", "scripts/check_file.py")
        val temp = Files.createTempDirectory("qusin-check-file-script-test-").toFile()
        try {
            val input = File(temp, "result.txt").apply { writeText("Qusin file validation\n") }
            val (exit, details) = run(script, input.absolutePath)
            assertEquals(details, 0, exit)
            assertTrue(details, details.contains("\"ok\": true") && details.contains("\"type\": \"txt\""))
        } finally { temp.deleteRecursively() }
    }

    @Test fun findFontScriptReturnsStructuredResultForText() {
        val script = bundled("skills/create-files", "scripts/find_font.py")
        val (exit, details) = run(script, "Qusin Việt Nam")
        assertTrue("unexpected exit=$exit output=$details", exit == 0 || exit == 3)
        assertTrue("expected JSON output, got: $details", details.trimStart().startsWith("{"))
        assertTrue(details, details.contains("\"path\"") || details.contains("\"missing\""))
    }
}
