# Qusin v0.1.56 CI fix notes

This source package incorporates the clean `com.qusin.qusin` Kotlin package tree with matching Android `namespace` and `applicationId`. The legacy `com.wangzan101.qusin` source tree must not be merged back into this tree; doing so recreates duplicate classes and breaks the generated Android `R`/`BuildConfig` package.

The CI log for commit `e43049e4e7ca6f4824d3dd5fd45fea685f2cc09d` reported two loop snapshot metadata changes. The snapshots `b_web_site_knowledge` and `g_browser_script_gated` now use the observed lengths and hashes from that run. The prompt resources in this source tree contain 122 tool descriptions (no obsolete `mika_*` files) and the capability list includes `mcp-lookup`.

Validation performed locally: package/resource consistency checks and ZIP integrity. A full Gradle Android/Core run was not executed in this environment because the Gradle distribution and dependencies are not cached and outbound DNS access is unavailable. Re-run CI after replacing the repository tree with this source package.
