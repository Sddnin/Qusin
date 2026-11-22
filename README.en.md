<div align="center">
  <img src="docs/media/logo.png" alt="Qusin" width="88" />
</div>

# Qusin

[GitHub repository](https://github.com/Sddnin/Qusin) · [Releases](https://github.com/Sddnin/Qusin/releases) · [Contributing guide](https://github.com/Sddnin/Qusin/blob/main/.github/CONTRIBUTING.md)

**Qusin is an AI assistant for Android.** Describe a task in natural language and Qusin can work with phone apps, websites, files, and scheduled tasks.

Qusin does not bundle its own AI model. Connect a provider with your API key, sign in with a supported account, or use a model hosted on your computer or local network.

## Features

- **Android app control:** operate apps on a separate background display, inspect screens, tap, type, and scroll while you keep using the phone. Device and app restrictions still apply; sign-in, CAPTCHA, and sensitive steps may require your input.
- **Built-in browser:** use a dedicated WebView with persistent site sessions, real input events, page reading, uploads, and downloads.
- **Long-running and parallel tasks:** inspect progress and steps, send follow-up instructions, or stop a task. Multiple sessions can run within device and app limits.
- **Schedules and conditions:** create one-time, daily, weekly, or condition-based tasks; preview upcoming runs and inspect execution history.
- **Memory and skills:** save user-approved information, consult app/website knowledge, and add text-based skills.
- **Linux workspace:** each conversation has its own workspace for files and commands. The bundled runtime currently supports ARM64 devices only.
- **Computer connectivity:** send tasks through CLI/Skill or MCP, track progress, and retrieve results from a computer.

## Models and API keys

Qusin supports OpenAI Chat Completions-compatible, Anthropic Messages, Google Gemini `generateContent`, and OpenAI Responses protocols. Presets cover multiple cloud providers, OpenAI-compatible services, local servers such as Ollama/LM Studio, and custom endpoints.

Open **Settings → Models → a provider** and use **API Key Pool → Add API Key** to add keys. Multiple keys can be pasted with commas, semicolons, or newlines between them. Qusin rotates through usable keys: `401/403` marks a rejected key, while `429` places a key on cooldown based on the server response. When every key is unavailable, Qusin does not fall back to a rejected key.

The pool is for supported API-key protocols. OAuth sessions such as ChatGPT and xAI sign-ins are not API keys and are managed separately. OpenRouter can keep a signed-in account and separately added backup keys.

## Requirements

- Android 10 or later for installation and core app functionality.
- Android 12 or later, plus the in-app setup flow, for controlling other Android apps.
- ARM64 device for the bundled Linux sandbox.
- A model with tool/function-calling support for the primary assistant model.

## Getting started

1. Install Qusin and open it on your phone.
2. Go to **Settings → Models** and add a provider or sign in with a supported account.
3. Select a primary model that supports tool calling. Vision, lightweight, and speech-to-text models can be configured separately.
4. To control Android apps, complete the in-app Wireless debugging pairing and Accessibility setup.
5. Start a conversation and describe the task.

Documentation:

- [Models and sign-in](docs/models.en.md)
- [Phone-control setup](docs/setup-guide.en.md)
- [Feature reference](docs/features.en.md)
- [FAQ](docs/faq.en.md)
- [Use Qusin from a computer](docs/computer.en.md)
- [Development roadmap](docs/dev/roadmap.md)

## Release notes

GitHub release descriptions can include language-tagged sections. Qusin shows the section matching the app language and falls back to English. Use the [multilingual release-notes template](docs/dev/release-notes-template.md) when publishing a release.

## Build from source

You need JDK 17 and an Android SDK matching the Gradle project configuration.

```bash
# unit tests
./gradlew :core:test :app:testDebugUnitTest

# build a debug APK
./gradlew :app:assembleDebug
```

The debug APK is written to:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Privacy and limitations

API keys and secrets are encrypted when stored on-device. Prompts and other data sent to a model are processed by the provider you configured; Qusin cannot override that provider's data-handling policy. Phone-control tasks use the Android permissions you grant. Secure screens, DRM content, and interfaces that block screenshots may not be readable and can require you to take over.

Qusin is under active development. Review results before relying on them for consequential actions, especially sending messages, deleting data, placing orders, or making payments.

## License

See [LICENSE](LICENSE), [NOTICE](NOTICE), and [third-party license notices](licenses/THIRD_PARTY_NOTICES.md).
