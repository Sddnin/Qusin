<!-- translated-from: README.md @ 1e3fabff8c4e -->
<div align="center">

<img src="../media/logo.png" alt="Qusin" width="88" />

# Qusin

**Qusin은 Android용 AI 비서입니다.** 자연어로 작업을 설명하면 휴대폰 앱, 웹사이트, 파일, 예약 작업을 처리하도록 도와줍니다.

Qusin에는 자체 AI 모델이 포함되어 있지 않습니다. 내 API 키로 공급자에 연결하거나 지원되는 계정으로 로그인하거나, PC 또는 로컬 네트워크에서 실행 중인 모델을 사용할 수 있습니다.

## 주요 기능

- **Android 앱 제어:** 별도의 백그라운드 가상 화면에서 앱을 조작하고 화면 읽기, 탭, 입력, 스크롤을 수행합니다. 메인 화면에서는 계속 휴대폰을 사용할 수 있습니다. 기기와 앱의 제한에 따라 로그인, CAPTCHA, 민감한 단계에는 사용자의 처리가 필요할 수 있습니다.
- **내장 브라우저:** 별도 WebView에서 사이트 로그인 상태를 유지하고 실제 입력 이벤트, 페이지 읽기, 업로드와 다운로드를 지원합니다.
- **장시간 및 병렬 작업:** 진행 상황과 실행 단계를 확인하고 추가 지시를 보내거나 작업을 중지할 수 있습니다. 병렬 실행 수는 기기와 앱의 제한을 받습니다.
- **일정 및 조건:** 일회성, 매일, 매주 또는 조건 기반 작업을 만들고 다음 실행 시간과 기록을 확인할 수 있습니다.
- **메모리 및 스킬:** 허용한 정보를 저장하고 앱/웹사이트 지식을 활용하며 텍스트 기반 스킬을 추가할 수 있습니다.
- **Linux 작업 공간:** 각 대화에 별도 작업 공간이 있어 파일과 명령을 처리합니다. 내장 런타임은 현재 ARM64 기기만 지원합니다.
- **컴퓨터 연결:** CLI/Skill 또는 MCP로 컴퓨터에서 작업을 보내고 상태를 추적하며 결과를 받을 수 있습니다.

## 모델 및 API 키

Qusin은 OpenAI Chat Completions 호환, Anthropic Messages, Google Gemini `generateContent`, OpenAI Responses 프로토콜을 지원합니다. 여러 클라우드 공급자, OpenAI 호환 서비스, Ollama/LM Studio 같은 로컬 서버의 프리셋과 사용자 지정 엔드포인트를 제공합니다.

**설정 → 모델 → 공급자 선택**을 열고 **API 키 풀 → API 키 추가**에서 키를 등록하세요. 여러 키는 쉼표, 세미콜론 또는 줄바꿈으로 구분해 붙여넣을 수 있습니다. Qusin은 사용 가능한 키를 순환합니다. `401/403`은 키를 거부된 것으로 표시하고, `429`는 서버 응답에 따라 대기 시간에 넣습니다. 모든 키를 사용할 수 없으면 거부된 키로 되돌아가지 않습니다.

키 풀은 지원되는 API 키 프로토콜에 적용됩니다. ChatGPT 및 xAI 로그인과 같은 OAuth 세션은 API 키가 아니므로 별도로 관리합니다. OpenRouter는 로그인 계정과 별도로 추가한 백업 키를 함께 사용할 수 있습니다.

## 내장 지식

내장 앱, 웹사이트 및 일반 작업 스킬 목록은 프로젝트 지식 원본에서 생성되며 원본 데이터에 맞춰 갱신됩니다.

## 공급자 프리셋

DeepSeek, 통의천문, Kimi, Zhipu, SiliconFlow, Volcano Engine, MiniMax, StepFun, Tencent Hunyuan, Baidu Qianfan, OpenRouter, OpenDesign Go, OpenCode Go, Anthropic, Gemini, OpenAI, xAI, Mistral, Groq, Cloudflare Workers AI, Ollama, LM Studio 등의 프리셋을 제공하며 사용자 지정 엔드포인트도 지원합니다.

## 요구 사항

- 설치와 핵심 기능에는 Android 10 이상이 필요합니다.
- 다른 Android 앱을 제어하려면 Android 12 이상과 앱 내 설정 절차가 필요합니다.
- 내장 Linux 샌드박스에는 ARM64 기기가 필요합니다.
- 기본 어시스턴트 모델은 도구/함수 호출을 지원해야 합니다.

## 시작하기

1. 휴대폰에 Qusin을 설치하고 엽니다.
2. **설정 → 모델**로 이동해 공급자를 추가하거나 지원되는 계정으로 로그인합니다.
3. 도구 호출을 지원하는 기본 모델을 선택합니다. 비전, 경량 모델, 음성 텍스트 변환 모델은 별도로 설정할 수 있습니다.
4. Android 앱을 제어하려면 앱 내 무선 디버깅 페어링과 접근성 설정을 완료합니다.
5. 대화를 시작하고 자연어로 작업을 설명합니다.

문서:

- [모델 및 로그인](../models.en.md)
- [휴대폰 제어 설정](../setup-guide.en.md)
- [기능 안내](../features.en.md)
- [자주 묻는 질문](../faq.en.md)
- [컴퓨터에서 Qusin 사용](../computer.en.md)
- [개발 로드맵](../dev/roadmap.md)

## 릴리스 노트

GitHub Release 설명에는 언어 태그가 지정된 섹션을 포함할 수 있습니다. Qusin은 앱 언어와 일치하는 섹션을 표시하고, 없으면 영어로 대체합니다. 릴리스 게시 시 [다국어 릴리스 노트 템플릿](../dev/release-notes-template.md)을 사용할 수 있습니다.

## 소스에서 빌드

JDK 17과 Gradle 프로젝트 설정에 맞는 Android SDK가 필요합니다.

```bash
# 단위 테스트
./gradlew :core:test :app:testDebugUnitTest

# Debug APK 빌드
./gradlew :app:assembleDebug
```

Debug APK는 다음 경로에 생성됩니다.

```text
app/build/outputs/apk/debug/app-debug.apk
```

## 개인정보 및 제한 사항

API 키와 비밀 정보는 기기에 암호화해 저장됩니다. 모델에 전송되는 프롬프트와 기타 데이터는 설정한 공급자가 처리하며, Qusin은 공급자의 데이터 처리 정책을 바꿀 수 없습니다. 휴대폰 제어 작업에는 사용자가 부여한 Android 권한이 사용됩니다. 보안 화면, DRM 콘텐츠, 스크린샷을 차단하는 인터페이스는 읽지 못할 수 있어 사용자의 개입이 필요할 수 있습니다.

Qusin은 계속 개발 중입니다. 메시지 전송, 데이터 삭제, 주문, 결제 등 중요한 작업은 결과를 확인한 뒤 사용하세요.

## 라이선스

[LICENSE](../../LICENSE), [NOTICE](../../NOTICE), [제3자 라이선스 고지](../../licenses/THIRD_PARTY_NOTICES.md)를 참조하세요.
