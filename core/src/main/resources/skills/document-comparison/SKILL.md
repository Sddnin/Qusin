---
name: document-comparison
display: So sánh tài liệu và phiên bản
description: So sánh tài liệu và tìm khác biệt giữa các bản
aliases: ["so sánh file", "so sánh hai bản", "diff tài liệu", "so sánh Word", "so sánh PDF", "tìm điểm khác nhau"]
terms: ["diff", "compare", "so sánh", "phiên bản", "thêm", "xóa", "thay đổi"]
---
## Quy trình
1. Xác định hai phiên bản và mục tiêu: so sánh nội dung, cấu trúc, metadata hay hình thức hiển thị.
2. Trích xuất văn bản bằng parser phù hợp: DOCX dùng python-docx; PDF dùng pypdf; XLSX so sheet/ô/công thức; PPTX so slide và nội dung. Không so nhị phân nếu cần hiểu nội dung.
3. Chuẩn hóa khoảng trắng/line ending chỉ cho phép so sánh; giữ bản trích xuất nguyên gốc để đối chiếu.
4. Phân loại phần thêm, xóa, sửa, di chuyển và thay đổi định dạng. Đừng coi khác biệt metadata là thay đổi nội dung.
5. Xuất báo cáo Markdown/DOCX/HTML hoặc bảng XLSX, nêu chính xác trang/slide/sheet/ô nếu xác định được.
6. Mở lại báo cáo và kiểm tra các khác biệt quan trọng bằng cách quay về nguồn.

## Quy tắc
Không tự hòa giải hai phiên bản hoặc chọn bản mới hơn nếu người dùng chưa yêu cầu. Khi parser không đọc được bố cục/hình ảnh, nói rõ chỉ so sánh phần văn bản trích xuất.
