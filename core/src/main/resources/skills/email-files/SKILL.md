---
name: email-files
display: Tạo tệp email EML
description: Soạn email thành tệp .eml với header và nội dung tương thích
aliases: ["tạo EML", "xuất email", "lưu email thành file", "tạo tệp thư", "email file"]
terms: ["EML", "RFC 5322", "MIME", "email", "subject", "From", "To", "attachment"]
---
## Quy trình
1. Xác định From/To/Cc/Bcc, Subject, charset, plain text/HTML và tệp đính kèm. Không tự tạo địa chỉ email.
2. Dùng `email.message.EmailMessage` trong Python để tạo MIME; không tự nối header tùy tiện.
3. Ghi UTF-8 và encoding MIME chuẩn; thêm attachment từ tệp do người dùng cung cấp hoặc đã tạo.
4. Kiểm tra header, MIME boundary, charset, nội dung và attachment bằng cách parse lại `.eml`.
5. Không gửi email chỉ vì đã tạo EML. Gửi thư là hành động riêng cần công cụ và sự cho phép rõ.
6. Không đưa mật khẩu, token hoặc header giả mạo vào email.
