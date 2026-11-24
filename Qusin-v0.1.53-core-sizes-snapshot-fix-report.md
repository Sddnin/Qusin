# Qusin 0.1.53 – Core prompt size snapshot fix

## Source
GitHub Actions run: https://github.com/Sddnin/Qusin/actions/runs/45374804325
Commit tested by that run: `2a1885d14555d4cdb2a185f19ed2aaae23b2a15f`

## Root cause
The Core test report contains exactly one failure:
`PromptSurfaceTest.layeredSurfaces`.

The runtime prompt's `all` combination is 111 bytes and 28 tokens larger than the committed expected size. Tool count and total tool-definition bytes match, so the difference is in the generated system prompt only.

Observed by the test:
- Expected system prompt: 49,232 bytes
- Actual system prompt: 49,343 bytes
- Tool definitions: 84 tools / 103,589 bytes (unchanged)
- Expected combined total: 152,821 bytes
- Actual combined total: 152,932 bytes
- Expected token estimate: 38,189
- Actual token estimate: 38,217

This is a stale size snapshot, not a prompt-generation or application behavior failure. The runtime prompt snapshot (`layers/all.txt`) already contains the file-creation instruction; only `layers/sizes.txt` had the old measured values.

## Change
Updated only:
`core/src/test/resources/prompt-surface/layers/sizes.txt`

The `all` row now records the values observed in the failing CI run. No tests were removed, skipped, disabled, or weakened. No application source, UI, behavior, Gradle, Kotlin, or Compose versions were changed. Version remains 0.1.53 / versionCode 54.

## Results from the supplied CI artifacts
- Core XML reports: 4,133 tests, 2 skipped, 1 failure, 0 errors (4,130 passed).
- The sole failure is `PromptSurfaceTest.layeredSurfaces`.
- Android job in the supplied log: `BUILD SUCCESSFUL`; unit tests, debug assemble and Android test-source compilation passed.
- Direct APK publishing job: succeeded for the tested commit.
- This patch has not yet been run by GitHub Actions. A green Core result after applying it is not claimed.

## Apply
From the repository root:
```bash
git apply Qusin-v0.1.53-core-sizes-snapshot-fix.patch
```
