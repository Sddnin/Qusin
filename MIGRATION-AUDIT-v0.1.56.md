# Qusin v0.1.56 migration audit

## Scope of this checkpoint

This checkpoint completes the package and product-identity migration. It does not claim full feature parity or a complete UI redesign. The app version remains 0.1.56 / versionCode 57.

## Verified identity migration

- `namespace` and `applicationId` are both `com.qusin.qusin`.
- Kotlin, Java and AIDL source roots were moved to `com/qusin/qusin`.
- 2,217 package declarations were checked against their source paths; no mismatches were found.
- Manifest actions, provider paths, tests, scripts, prompt identifiers and relay protocol labels were updated.
- Text-source scan found no remaining old product or package identifiers, apart from the original copyright-owner name in the two NOTICE files.

## Source comparison findings

The reference source and Qusin are not simple copies of the same revision. Their internal architecture has diverged.

- App source: 632 Kotlin files in Qusin versus 846 in the reference source. A normalized path comparison shows 355 reference-side file paths without a direct same-path Qusin file. This is a path-level delta, not proof of 355 missing features: many screens and helpers were split into separate files in the reference branch while Qusin still has consolidated screen files.
- Core source: 541 Kotlin files in Qusin versus 574 in the reference source. The normalized path comparison shows 225 reference-side paths and 192 Qusin-side paths without direct counterparts. Several core packages were reorganized, so copying files by path would create duplicate or incompatible APIs.
- Qusin has its own `QusinApp`, navigation, sandbox, phone-call stack, MCP runtime, provider store and UI components. The reference branch has additional `frontdesk` and `voicecall` modules, while Qusin implements related call and voice work in its own `calls` and `voice` packages. These require functional mapping, not blind file copying.
- The Qusin chat screen is a consolidated 2,142-line screen; the reference chat screen is 929 lines and delegates more behavior to extracted UI files. Replacing one with the other without porting the associated ViewModel, app runtime and settings APIs would be unsafe.
- The reference resource tree contains 146 additional resource-file paths. These need key-by-key merging with Qusin's translations to avoid duplicate IDs and untranslated screens.

## Why full parity is not marked complete

The source trees have different package layouts, model APIs, application runtime objects and screen navigation. A wholesale copy would risk losing Qusin-only behavior or introducing unresolved symbols. The correct next work is a feature-by-feature port with its tests and resource strings, not a global text replacement.

## Remaining migration batches

1. UI architecture: migrate adaptive navigation, page transitions, drawer and chat composition while preserving Qusin-specific attachments, task states, file viewer and sandbox flows.
2. Core agent features: map the reference-side agent, context, safety and task lifecycle changes onto Qusin's existing core APIs; retain Qusin-only tools and provider logic.
3. Voice and calls: compare the separate voice-call experience with Qusin's current telecom and realtime-call implementation; port only missing behavior and tests.
4. Files and workspace: reconcile file library, device browser, granted folders and workspace routing.
5. MCP and tools: compare catalog, OAuth, package manager, relay and conversation result handling, then merge tests and locale strings.
6. Resources and release: merge translations, run all unit/UI/device tests, rebuild native sandbox artifacts with the pinned NDK, run Gradle lint and assemble tasks, then validate on a real Android device.

## Validation limits

The local checks listed in `BUILD-STATUS-v0.1.56.txt` passed. Gradle and Go tests could not run because their required downloads were blocked by DNS. No Android APK build or device test is claimed.
