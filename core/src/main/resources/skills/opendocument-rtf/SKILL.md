---
name: opendocument-rtf
display: Tài liệu OpenDocument và RTF
description: Tạo và xử lý ODT, ODS, ODP, RTF và LaTeX
aliases: ["tài liệu opendocument và rtf", "tạo tài liệu opendocument và rtf", "xử lý tài liệu opendocument và rtf"]
terms: ["ODT", "ODS", "ODP", "RTF", "TEX", "LibreOffice"]
---
## Định dạng và công cụ
- ODT: tài liệu văn bản OpenDocument; ODS: bảng tính; ODP: trình chiếu. Ưu tiên LibreOffice headless hoặc thư viện chuyên dụng nếu đã cài.
- RTF là định dạng văn bản có cấu trúc; phải tạo đúng cú pháp RTF, không chỉ đổi đuôi TXT.
- TEX/LaTeX là mã nguồn; PDF chỉ được tạo khi có engine như `xelatex`, `lualatex` hoặc `pdflatex` phù hợp.

## Quy trình
1. Chọn đúng định dạng dựa trên việc người nhận cần chỉnh sửa hay chỉ đọc.
2. Kiểm tra executable/thư viện hiện có trước khi tạo hoặc chuyển đổi.
3. Giữ font, bảng, mục lục, header/footer, công thức và bố cục trong giới hạn công cụ.
4. Mở lại tệp bằng parser hoặc ứng dụng tương ứng; với ODF kiểm tra gói ZIP và XML nội bộ.
5. Báo rõ nếu macro, font nhúng, track changes hoặc bố cục đặc biệt không được bảo toàn.

Không tạo ODT/ODS/ODP giả bằng cách đổi phần mở rộng. Không tự biên dịch LaTeX có shell-escape hoặc lệnh ngoài không tin cậy.
