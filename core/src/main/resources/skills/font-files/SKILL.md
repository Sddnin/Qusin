---
name: font-files
display: Tệp phông chữ
description: Nhận diện, kiểm tra và đóng gói font an toàn
aliases: ["tệp phông chữ", "tạo tệp phông chữ", "xử lý tệp phông chữ"]
terms: ["TTF", "OTF", "WOFF", "WOFF2", "EOT", "TTC"]
---
## Phạm vi
Nhận diện TTF, OTF, WOFF, WOFF2, TTC; tạo CSS `@font-face` hoặc manifest font khi người dùng có quyền sử dụng font.

## Quy trình
1. Kiểm tra định dạng thật và metadata font bằng fontTools hoặc công cụ sẵn có.
2. Không chuyển đổi font bằng cách đổi phần mở rộng. WOFF/WOFF2 cần bộ mã hóa chuyên dụng.
3. Khi tạo gói web, khai báo đúng `font-weight`, `font-style`, `font-display` và MIME type.
4. Kiểm tra glyph tiếng Việt, dấu kết hợp, bảng ký tự và fallback font.
5. Đóng gói license/notice đi kèm nếu được cung cấp; không tự ý phân phối font thương mại.

Không sửa metadata bản quyền hoặc bỏ qua điều khoản giấy phép.
