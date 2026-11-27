# Qusin v0.1.56 CI fix notes — follow-up

The latest uploaded CI artifacts contain duplicate reports: `logs_124269740431.zip` and `logs_124269740431 (1).zip` have identical SHA-256 hashes. The core test report contains 9 failures across the new and legacy package trees.

Root cause: the repository upload merged the new `com.qusin.qusin` source tree into the existing repository without deleting the old `com.wangzan101.qusin` tree. Both trees remained under Android main/test/AIDL sources and Core main/test/testFixtures/chatgptCheck sources. Android then compiled stale files importing the old generated `R` and `BuildConfig` package; Core scanned duplicate source trees, so `SingleEntryTest` treated the old action package as an out-of-boundary caller. The same merged upload left 12 obsolete `mika_*.md` tool prompt files beside the current Qusin prompt files.

This follow-up adds `tools/ci/cleanup-legacy-package-tree.sh` and invokes it immediately after checkout in every Gradle CI job (`core`, `cli`, `app`, and `relay`). The script removes any stale `com/wangzan101` directories below `app/` and `core/`, then removes obsolete `mika_*.md` prompt descriptions. It leaves the canonical `com/qusin/qusin` source tree untouched and fails fast if stale paths remain.

The clean source tree already contains the corresponding current Android, AIDL, instrumentation-test, unit-test, Core, test-fixture, and chatgptCheck files under `com/qusin/qusin`. The `mcp-lookup` capability is retained. Prompt inventory after cleanup is 122 tool-description files / 66,598 bytes, matching `core/src/test/resources/prompt-surface/sizes.txt`.

Local validation: cleanup script syntax check, simulated removal of legacy source/prompt paths, canonical-source preservation, namespace checks, prompt inventory, CI wiring checks, and ZIP integrity. A full Gradle test/build was not run in this environment because the Gradle distribution is not installed/cached and outbound DNS access is unavailable. Re-run GitHub Actions after uploading this patch.
