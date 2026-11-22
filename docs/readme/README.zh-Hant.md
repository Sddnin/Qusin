<!-- translated-from: README.md @ 1e3fabff8c4e -->
<div align="center">

<img src="../media/logo.png" alt="Qusin" width="88" />

# Qusin

**Qusin 是一款 Android AI 助理。** 以自然語言描述任務，它就能協助操作手機應用程式、網站、檔案與排程任務。

Qusin 不內建 AI 模型。你可以連接自己的 API 金鑰、登入支援的帳戶，或使用在電腦或區域網路上執行的模型。

## 功能特色

- **Android 應用程式控制：** 在獨立的背景虛擬螢幕操作應用程式，可讀取介面、點擊、輸入與捲動；你仍可同時使用手機主畫面。裝置與應用程式本身的限制仍然適用，登入、驗證碼和敏感步驟可能需要你接手。
- **內建瀏覽器：** 使用獨立 WebView，保留網站登入狀態，支援真實輸入事件、讀取頁面、上傳與下載。
- **長任務與平行任務：** 查看進度和已執行步驟，補充指令或停止任務；平行數量受裝置和應用程式限制。
- **排程與條件任務：** 建立一次性、每日、每週或依條件觸發的任務，並查看下次執行時間與歷史紀錄。
- **記憶與技能：** 儲存經你允許的資訊、使用應用程式/網站知識，並新增文字技能。
- **Linux 工作區：** 每段對話都有獨立工作區，可處理檔案與執行命令；內建執行環境目前僅支援 ARM64 裝置。
- **電腦連線：** 透過 CLI/Skill 或 MCP 從電腦傳送任務、追蹤狀態並取得結果。

## 模型與 API 金鑰

Qusin 支援相容 OpenAI Chat Completions、Anthropic Messages、Google Gemini `generateContent` 和 OpenAI Responses 的協定。預設涵蓋多種雲端服務商、OpenAI 相容服務，以及 Ollama/LM Studio 等本機伺服器，也支援自訂端點。

開啟 **設定 → 模型 → 選擇服務商**，在 **API 金鑰池 → 新增 API 金鑰** 中新增金鑰。多把金鑰可用逗號、分號或換行分隔。Qusin 會在可用金鑰間輪替：`401/403` 會將金鑰標記為遭拒，`429` 則依伺服器回應讓金鑰進入冷卻期。所有金鑰都不可用時，不會回退至已遭拒的金鑰。

金鑰池適用於支援的 API 金鑰協定。ChatGPT 和 xAI 等 OAuth 登入工作階段不是 API 金鑰，必須分開管理。OpenRouter 可以同時保留已登入帳戶與另外新增的備用金鑰。

## 內建知識

內建 App、網站與通用操作技能清單會從專案知識來源產生，並隨原始資料更新。

## 服務商預設

內建 DeepSeek、通義千問、Kimi、智譜、硅基流動、火山方舟、MiniMax、階躍星辰、騰訊混元、百度千帆、OpenRouter、OpenDesign Go、OpenCode Go、Anthropic、Gemini、OpenAI、xAI、Mistral、Groq、Cloudflare Workers AI、Ollama 和 LM Studio 等服務商預設，也可選擇自訂端點。

## 系統需求

- Android 10 或更新版本可安裝並使用核心功能。
- 控制其他 Android 應用程式需要 Android 12 或更新版本，並完成應用程式內設定流程。
- 使用內建 Linux 沙箱需要 ARM64 裝置。
- 主要助理模型需要支援工具/函式呼叫。

## 開始使用

1. 在手機上安裝並開啟 Qusin。
2. 前往 **設定 → 模型**，新增服務商或登入支援的帳戶。
3. 選擇支援工具呼叫的主要模型；視覺、輕量模型與語音轉文字模型可另外設定。
4. 如需控制 Android 應用程式，請完成應用程式內的無線除錯配對與無障礙設定。
5. 開始對話，以自然語言描述要完成的任務。

文件：

- [模型與登入](../models.en.md)
- [手機控制設定](../setup-guide.en.md)
- [功能說明](../features.en.md)
- [常見問題](../faq.en.md)
- [透過電腦使用 Qusin](../computer.en.md)
- [開發路線圖](../dev/roadmap.md)

## 發布說明

GitHub Release 說明可以包含帶有語言標籤的章節。Qusin 會優先顯示與應用程式語言相符的內容，缺少對應語言時則回退到英文。發布版本時可使用[多語言發布說明範本](../dev/release-notes-template.md)。

## 從原始碼建置

需要 JDK 17，以及符合 Gradle 專案設定的 Android SDK。

```bash
# 單元測試
./gradlew :core:test :app:testDebugUnitTest

# 建置 Debug APK
./gradlew :app:assembleDebug
```

Debug APK 輸出至：

```text
app/build/outputs/apk/debug/app-debug.apk
```

## 隱私與限制

API 金鑰和機密資訊會在裝置上加密儲存。傳送給模型的提示詞和其他資料會由你設定的服務商處理，Qusin 無法覆蓋服務商的資料處理政策。手機控制任務使用你授予的 Android 權限。安全畫面、DRM 內容及禁止截圖的介面可能無法讀取，需要你接手。

Qusin 仍在積極開發中。對於傳送訊息、刪除資料、下單或付款等重要操作，請先檢查執行結果。

## 授權條款

請參閱 [LICENSE](../../LICENSE)、[NOTICE](../../NOTICE) 和[第三方授權聲明](../../licenses/THIRD_PARTY_NOTICES.md)。
