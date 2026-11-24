---
name: file-validation
display: Kiểm tra tính hợp lệ của tệp
description: Kiểm tra định dạng, cấu trúc và tính toàn vẹn tệp
aliases: ["kiểm tra file", "file có lỗi không", "kiểm tra ZIP", "kiểm tra định dạng", "xác thực tệp", "file hỏng"]
terms: ["validate", "validation", "checksum", "magic bytes", "MIME", "CRC", "integrity", "corrupt"]
---
## Quy trình
1. Kiểm tra file tồn tại, kích thước, tên, phần mở rộng và chữ ký định dạng (magic bytes) nếu có.
2. Không tin phần mở rộng do người gửi đặt; xác định định dạng nội dung thực tế bằng parser hoặc file signature.
3. Dùng parser đúng loại: ZIP testzip, TAR list/read, PDF pypdf, DOCX/XLSX/PPTX đọc gói Open XML, ảnh Pillow, JSON/XML parser, SQLite `PRAGMA integrity_check`.
4. Kiểm tra kích thước bất thường, file rỗng, encoding, dữ liệu bị cắt, thành phần thiếu và lỗi giải nén.
5. Nếu có checksum, tính SHA-256 và so sánh với giá trị đáng tin cậy được cung cấp; không tự xem checksum tự sinh là bằng chứng nguồn gốc.
6. Xuất báo cáo trạng thái `pass`, `warning`, `fail`, nêu rõ phép kiểm tra nào đã chạy.

## An toàn
Không thực thi tệp để kiểm tra. Không giải nén vào workspace chính nếu chưa lọc đường dẫn. Kiểm tra cấu trúc không đồng nghĩa quét malware hay chứng minh tệp an toàn tuyệt đối.
