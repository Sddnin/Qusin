# Qusin MVP implementation notes

## Implemented

- Added the Android home-screen widget `Nói với Qusin` with compact, wide, and large RemoteViews layouts, resize handling, light/dark colors, and launcher preview metadata.
- The widget is only an entry point. It opens `MainActivity`; it never starts microphone capture or creates a task from a receiver or background service.
- Added the `QuickVoiceScreen` flow using the existing `VoiceSource` / `SpeechSession`: contextual microphone permission, explicit start, stop/cancel, live transcript, editable review, retry, and explicit submit. The existing 60-second limit is retained for non-streaming sessions; unlimited streaming sessions are stopped by the user.
- The microphone does not start immediately after permission is granted. The user must press “Bắt đầu nghe” again.
- The “Gõ yêu cầu” fallback and the large widget's “Mở chat để nhập” action open Work and request focus for the composer.
- Added the Cá nhân / Work segmented switch to the existing chat headers. The existing Work chat and Personal assistant runtimes remain separate, and their saveable screen states are keyed independently.
- Added a navigation unit test for the QuickVoice route stack.
- Added English defaults and Vietnamese strings for the new UI, with supplemental translations in several other locale folders.

## Not included yet

- Personal composer image, camera, and file attachments, plus generated-file return routing. The current `PersonalMessage` / `PersonalAgent.send()` contract is text-only. A safe implementation needs a persisted attachment model and multimodal/file ingestion path; this MVP deliberately does not show non-functional attachment buttons.

## Validation

- Parsed all 889 Android resource XML files successfully.
- Checked duplicate resource names and all new widget resource references; no missing references or duplicates were found.
- Added the navigation test source, but could not execute Gradle tests or compile Kotlin in this environment because the Gradle wrapper distribution (8.11.1) is not cached and `services.gradle.org` is unreachable. No Android SDK is installed in this environment. Run `./gradlew :app:testDebugUnitTest :app:compileDebugKotlin` and the Android instrumentation suite in CI before release.
