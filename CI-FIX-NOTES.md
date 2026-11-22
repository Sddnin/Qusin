# Qusin CI follow-up patch notes

## MCP approval regression

`ApprovalPolicy` now allows no-cost outward actions only for an attended phone UI run. Any outward risk (`External`, `Irreversible`, or `Unknown`) is denied for non-UI origins (including MCP), and for unattended runs. Payment and tool-declared approval requests remain blocked when no user can approve them. The MCP regression test file is unchanged; the approval-policy matrix adds explicit MCP/UI coverage.

## Android resource check

The current `app/src/main/res/values*/strings_chat.xml` files (13 locale/resource directories) contain no literal `\\u` escape sequence. The `chat_example_github_star` values are plain text in the checked source, so there was no invalid escape to double-escape or remove in this snapshot.

## Verification limitation

The requested Gradle commands could not start because the wrapper cannot resolve `services.gradle.org` to download Gradle 8.11.1. Therefore `:app:mergeDebugResources`, `:core:test`, and Gradle-based golden regeneration have not been run in this environment. Golden/snapshot files were intentionally not hand-edited or fabricated.
