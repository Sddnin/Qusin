# Qusin v0.1.56 CI fixes (2026-11-27)

## Changes

- Added the missing `McpOAuthRegistration` import in `McpServerStore`, matching the public type declared by the core MCP OAuth module.
- Restored migration support for the previous Mika Feishu connector keys (`mika_url` and `mika_token`). A successful setup writes only the current Qusin keys (`qusin_url` and `qusin_token`). Corrected the regression test so it actually simulates a legacy config instead of renaming each key to itself.
- Registered the existing `mcp-lookup.md` capability in the prompt-layout test and refreshed resource-size and rendered-prompt snapshots against the latest CI diffs.
- Removed the volatile MCP preset count from the v0.1.56 release note.

## Validation

- 47 Feishu connector Python tests passed locally. The SDK serialization integration test was not run locally because `lark_oapi` is not installed in this offline environment; the package declares it as a dependency and CI installs it.
- Static checks passed for the Android namespace/application ID, Kotlin package references, MCP OAuth registration import, prompt resource-size snapshots, legacy snapshot fingerprints, and Python syntax.
- Android Gradle compilation and Kotlin tests were not rerun: the Gradle wrapper could not download Gradle 8.11.1 because DNS resolution for `services.gradle.org` is unavailable in this environment.

## Package identity note

The supplied source archive already has `namespace` and `applicationId` set to `com.qusin.qusin`, and its app sources are under `com/qusin/qusin`. The uploaded Android log still references some pre-migration `com/wangzan101/qusin` source paths. This archive keeps the migrated package identity; it does not revert it to the old namespace.
