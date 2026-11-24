---
name: file-bundling-manifests
display: Gom bộ tài liệu và tạo manifest
description: Đóng gói đầu ra kèm README và checksum
aliases: ["gom nhiều file thành bộ", "tạo gói bàn giao", "tạo manifest", "đóng gói tài liệu", "gom báo cáo và hình"]
terms: ["bundle", "manifest", "SHA-256", "checksum", "README", "deliverable", "ZIP", "TAR"]
---
## Quy trình
1. Liệt kê tất cả đầu ra cần giao và phân nhóm: tài liệu, dữ liệu, hình, source, phụ lục.
2. Tạo cấu trúc thư mục ngắn, dễ hiểu; thêm README mô tả từng tệp, định dạng và cách mở.
3. Tạo manifest JSON gồm tên tương đối, kích thước, SHA-256 và vai trò của từng tệp; không ghi đường dẫn tuyệt đối.
4. Dùng archive-management để tạo ZIP/TAR theo yêu cầu; không đóng gói tệp tạm, cache, secret hoặc bản nháp ngoài ý muốn.
5. Kiểm tra archive, số lượng tệp, manifest và checksum. Giải nén thử vào thư mục tạm khi cần.
6. Nêu rõ gói gồm những gì và phiên bản nào là bản cuối.
