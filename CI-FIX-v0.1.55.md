# Qusin v0.1.55 CI fix

This patch keeps the existing release identity unchanged:

- `versionName`: `0.1.55`
- `versionCode`: `56`

## Android Kotlin compilation

The Android CI log reported unresolved `heightIn` references in:

- `app/src/main/java/com/qusin/qusin/ui/ChatDrawer.kt`
- `app/src/main/java/com/qusin/qusin/ui/components/Controls.kt`

Both files now import `androidx.compose.foundation.layout.heightIn`.
The type-inference and `clip` diagnostics in `Controls.kt` were downstream compiler errors from the unresolved modifier extension; no unrelated UI logic was changed.

## Verification notes

The provided CI log shows the core JVM build, CLI suite (89 tests), translation tool suite (12 tests), and relay integration build succeeded. The Android job stopped at `:app:compileDebugKotlin` due to the missing imports.

A local Gradle compile was attempted after the fix but could not start because this environment could not resolve `services.gradle.org`. A successful Android rebuild still needs confirmation in CI.
