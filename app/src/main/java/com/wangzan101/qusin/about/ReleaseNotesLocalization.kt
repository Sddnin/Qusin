package com.wangzan101.qusin.about

/**
 * Selects the language block embedded in a GitHub release body.
 *
 * Release authors can wrap each translated section with:
 * `<!-- qusin-lang: en -->` ... `<!-- /qusin-lang -->`.
 * Unmarked release bodies remain compatible and are displayed as-is.
 */
internal object ReleaseNotesLocalization {
    private val block = Regex(
        """<!--\s*qusin-lang\s*:\s*([A-Za-z-]+)\s*-->(.*?)<!--\s*/qusin-lang\s*-->""",
        setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL),
    )

    fun select(markdown: String, languageTag: String?): String {
        val matches = block.findAll(markdown).mapNotNull { match ->
            val language = normalize(match.groupValues[1])
            val body = match.groupValues[2].trim()
            if (language.isBlank() || body.isBlank()) null else language to body
        }.toList()
        if (matches.isEmpty()) return markdown.trim()

        val requested = normalize(languageTag.orEmpty())
        val selected = matches.firstOrNull { it.first == requested }
            ?: matches.firstOrNull { it.first == requested.substringBefore('-') }
            ?: matches.firstOrNull { it.first == "en" }
            ?: matches.first()
        return selected.second
    }

    private fun normalize(tag: String): String {
        val parts = tag.trim().replace('_', '-').split('-').filter(String::isNotBlank)
        if (parts.isEmpty()) return "en"
        val language = parts.first().lowercase()
        return when {
            language == "zh" && parts.drop(1).any { it.equals("Hant", true) || it.equals("TW", true) || it.equals("HK", true) || it.equals("MO", true) } -> "zh-Hant"
            language == "zh" -> "zh"
            language == "in" -> "id"
            else -> language
        }
    }
}
