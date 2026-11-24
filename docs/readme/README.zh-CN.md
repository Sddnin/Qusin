<!-- translated-from: README.md @ 1e3fabff8c4e -->
<div align="center">

<img src="../media/logo.png" alt="Qusin" width="88" />

# Qusin

**Qusin 是一款 Android AI 助手。** 用自然语言描述任务，它就能协助操作手机应用、网站、文件和定时任务。

Qusin 不内置 AI 模型。你可以连接自己的 API 密钥、登录受支持的账户，或使用运行在电脑或局域网中的模型。

## 功能特点

- **Android 应用控制：** 在独立的后台虚拟屏上操作应用，可读取界面、点击、输入和滚动；你仍可同时使用手机主屏。设备与应用本身的限制仍然适用，登录、验证码和敏感步骤可能需要你接手。
- **内置浏览器：** 使用独立 WebView，保留网站登录状态，支持真实输入事件、读取页面、上传和下载。
- **长任务与并行任务：** 查看进度和已执行步骤，补充指令或停止任务；并行数量受设备和应用限制。
- **日程与条件任务：** 创建一次性、每日、每周或按条件触发的任务，并查看下次运行时间和执行历史。
- **记忆与技能：** 保存经你允许的信息，使用应用/网站知识，并添加文本技能。
- **Linux 工作区：** 每段对话都有独立工作区，可处理文件和运行命令；内置运行环境目前仅支持 ARM64 设备。
- **电脑连接：** 通过 CLI/Skill 或 MCP 从电脑发送任务、跟踪状态并获取结果。

## 模型与 API 密钥

Qusin 支持兼容 OpenAI Chat Completions、Anthropic Messages、Google Gemini `generateContent` 和 OpenAI Responses 的协议。预设覆盖多种云服务商、OpenAI 兼容服务，以及 Ollama/LM Studio 等本地服务器，也支持自定义端点。

打开 **设置 → 模型 → 选择服务商**，在 **API 密钥池 → 添加 API 密钥** 中添加密钥。多个密钥可用逗号、分号或换行分隔。Qusin 会在可用密钥间轮换：`401/403` 会将密钥标记为被拒绝，`429` 则按服务器返回的信息让密钥进入冷却期。所有密钥都不可用时，不会回退到已被拒绝的密钥。

密钥池适用于受支持的 API 密钥协议。ChatGPT 和 xAI 等 OAuth 登录会话不是 API 密钥，需要单独管理。OpenRouter 可以同时保留已登录账户和单独添加的备用密钥。

## 内置知识

<!-- generated:knowledge:start -->
<!-- 这一段由 ./gradlew updateReadme 生成，不要手改 -->
| 类别 | 内置 |
|---|---|
| **App** | 阿里巴巴、支付宝、Amazon、Android 设置、百度、百家号、哔哩哔哩、BOSS直聘、ChatGPT、Chrome、得到、大众点评、Discord、抖音、飞书、GitHub、Gmail、Google Calendar、Google Maps、Instagram、京东、即刻、快手、领英、美团、美团外卖、Netflix、拼多多、QQ、QQ邮箱、QQ音乐、Reddit、Slack、Spotify、淘宝、Telegram、TikTok、今日头条、Uber、Uber Eats、微信、微博、WhatsApp、X、闲鱼、小黑盒、小红书、YouTube、知乎 |
| **网站** | 铁路12306、1688、Amazon、百度搜索、哔哩哔哩、Google Calendar、携程旅行、大众点评、豆瓣、抖音、GitHub、Instagram、京东、Gmail、Google Maps、美团、淘宝网、Telegram、百度贴吧、百度热搜、微博、搜狗微信、WhatsApp、小红书、小宇宙、QQ音乐、YouTube、知乎、BOSS直聘 |
| **通用做法**（技能） | Tạo tệp mô hình 3D cơ bản、Tạo và quản lý gói tệp nén、Xử lý hàng loạt tệp、Bản vẽ CAD kỹ thuật、Tạo lịch và danh bạ、Tạo và đóng gói dự án mã nguồn、改 Qusin 自己的设置、把内容整理成文字、Tạo file、Phân tích dữ liệu và bảng tính、Tạo và xử lý tệp cơ sở dữ liệu、Tạo sơ đồ và biểu đồ kỹ thuật、So sánh tài liệu và phiên bản、处理 PDF 文档、Tạo sách điện tử、Tạo tệp email EML、Gom bộ tài liệu và tạo manifest、Chuyển đổi định dạng tệp、Tạo và xuất nhiều loại tệp、Chỉnh sửa tệp có sẵn、Kiểm tra tính hợp lệ của tệp、Tệp phông chữ、Biểu mẫu và mẫu tài liệu、Tệp dữ liệu địa lý、Tạo tệp hình ảnh、查清一件事、Tệp bản địa hóa、Log và tệp kỹ thuật văn bản、Tạo và chuyển đổi âm thanh video、消息与邮件、OCR và trích xuất tài liệu、Tài liệu OpenDocument và RTF、Tạo bài trình chiếu PowerPoint、Dữ liệu khoa học và dạng cột、写技能、Tạo bảng tính Excel、Tạo tệp dữ liệu có cấu trúc、Phụ đề và bản chép lời、Tạo tệp web và giao diện、Tạo tài liệu Word |
<!-- generated:knowledge:end -->

## 服务商预设

<!-- generated:providers:start -->
<!-- 这一段由 ./gradlew updateReadme 生成，不要手改 -->
内置 DeepSeek、通义千问、Kimi、智谱、智谱 Coding Plan、硅基流动、火山方舟、MiniMax、阶跃星辰、腾讯混元、百度千帆、OpenRouter、OpenDesign Go、OpenCode Go、Anthropic、Gemini、OpenAI、xAI、Mistral、Groq、Cloudflare Workers AI、Ollama、LM Studio 等服务商预设；其他服务商选「自定义」，OpenAI 兼容、Anthropic、Gemini、OpenAI Responses 接口都能接。
<!-- generated:providers:end -->

## 系统要求

- Android 10 或更高版本可安装并使用核心功能。
- 控制其他 Android 应用需要 Android 12 或更高版本，并完成应用内设置流程。
- 使用内置 Linux 沙箱需要 ARM64 设备。
- 主助手模型需要支持工具/函数调用。

## 开始使用

1. 在手机上安装并打开 Qusin。
2. 进入 **设置 → 模型**，添加服务商或登录受支持的账户。
3. 选择支持工具调用的主模型；视觉、轻量模型和语音转文字模型可单独配置。
4. 如需控制 Android 应用，请完成应用内的无线调试配对和无障碍设置。
5. 开始对话，用自然语言描述要完成的任务。

文档：

- [模型与登录](../models.en.md)
- [手机控制设置](../setup-guide.en.md)
- [功能说明](../features.en.md)
- [常见问题](../faq.en.md)
- [通过电脑使用 Qusin](../computer.en.md)
- [开发路线图](../dev/roadmap.md)

## 发布说明

GitHub Release 描述可以包含带语言标签的章节。Qusin 会优先显示与应用语言匹配的内容，并在缺少对应语言时回退到英文。发布版本时可使用[多语言发布说明模板](../dev/release-notes-template.md)。

## 从源码构建

需要 JDK 17，以及与 Gradle 项目配置相匹配的 Android SDK。

```bash
# 单元测试
./gradlew :core:test :app:testDebugUnitTest

# 构建 Debug APK
./gradlew :app:assembleDebug
```

Debug APK 输出到：

```text
app/build/outputs/apk/debug/app-debug.apk
```

## 隐私与限制

API 密钥和机密信息在设备上加密保存。发送给模型的提示词和其他数据会由你配置的服务商处理，Qusin 无法覆盖服务商的数据处理政策。手机控制任务使用你授予的 Android 权限。安全屏幕、DRM 内容以及禁止截图的界面可能无法读取，需要你接手。

Qusin 仍在积极开发中。对于发送消息、删除数据、下单或付款等重要操作，请先检查执行结果。

## 许可证

参阅 [LICENSE](../../LICENSE)、[NOTICE](../../NOTICE) 和[第三方许可证声明](../../licenses/THIRD_PARTY_NOTICES.md)。
