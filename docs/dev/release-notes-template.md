# Qusin release notes template

The app selects the release-note section that matches the app language. English is the fallback when a translation is unavailable. Keep the language markers exactly as shown; the app removes the markers before rendering the Markdown.

Copy this structure into the GitHub Release description and replace each placeholder with the actual changes for that release:

```markdown
<!-- qusin-lang: en -->
## What's new
- Describe the changes in English.
<!-- /qusin-lang -->

<!-- qusin-lang: vi -->
## Có gì mới
- Mô tả thay đổi bằng tiếng Việt.
<!-- /qusin-lang -->

<!-- qusin-lang: zh -->
## 更新內容
- 使用簡體中文描述本次更新。
<!-- /qusin-lang -->

<!-- qusin-lang: zh-Hant -->
## 更新內容
- 使用繁體中文描述本次更新。
<!-- /qusin-lang -->

<!-- qusin-lang: ja -->
## 更新内容
- 今回の変更を日本語で説明します。
<!-- /qusin-lang -->

<!-- qusin-lang: ko -->
## 새로운 기능
- 이번 변경 사항을 한국어로 설명합니다.
<!-- /qusin-lang -->

<!-- qusin-lang: es -->
## Novedades
- Describe los cambios de esta versión en español.
<!-- /qusin-lang -->

<!-- qusin-lang: pt -->
## Novidades
- Descreva as alterações desta versão em português.
<!-- /qusin-lang -->

<!-- qusin-lang: ru -->
## Что нового
- Опишите изменения этой версии на русском языке.
<!-- /qusin-lang -->

<!-- qusin-lang: tr -->
## Yenilikler
- Bu sürümdeki değişiklikleri Türkçe açıklayın.
<!-- /qusin-lang -->

<!-- qusin-lang: id -->
## Yang baru
- Jelaskan perubahan versi ini dalam bahasa Indonesia.
<!-- /qusin-lang -->

<!-- qusin-lang: ar -->
## الجديد
- اشرح تغييرات هذا الإصدار باللغة العربية.
<!-- /qusin-lang -->

<!-- qusin-lang: fa -->
## تازه‌ها
- تغییرات این نسخه را به زبان فارسی توضیح دهید.
<!-- /qusin-lang -->
```

Use the same format for every language you publish. If a section is missing, Qusin displays English when available; if the release has no language markers, it displays the complete release body for compatibility with older releases.
