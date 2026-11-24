---
name: file-conversion
display: Chuyển đổi định dạng tệp
description: Chuyển đổi tài liệu và dữ liệu giữa các định dạng
aliases: ["đổi định dạng file", "chuyển đổi file", "convert file", "đổi PDF sang Word", "đổi Word sang PDF", "chuyển Excel sang CSV"]
terms: ["convert", "chuyển đổi", "định dạng", "PDF", "DOCX", "XLSX", "CSV", "HTML", "Markdown", "ảnh", "âm thanh", "video"]
---
## Mục tiêu
Chuyển đổi tệp theo đúng định dạng đích, bảo toàn nội dung và cấu trúc quan trọng. Luôn giữ bản gốc.

## Quy trình
1. Xác định định dạng nguồn, đích và mục đích sử dụng. Kiểm tra tệp nguồn thực sự là định dạng gì, không chỉ dựa vào phần mở rộng.
2. Chọn công cụ phù hợp đã có trong môi trường: `pypdf` cho thao tác PDF; `python-docx` cho DOCX; `openpyxl` cho XLSX; `Pillow` cho ảnh; `csv`/`json` cho dữ liệu văn bản. Với chuyển đổi cần trình kết xuất (DOCX/PPTX sang PDF), kiểm tra LibreOffice hoặc công cụ tương đương trước khi chạy.
3. Không đổi tên phần mở rộng để giả làm chuyển đổi. Tạo tệp mới với phần mở rộng đích đúng chuẩn.
4. Giữ nguyên thứ tự trang, bảng, tiêu đề, encoding, ngày tháng, số thập phân và dữ liệu rỗng khi chúng có ý nghĩa.
5. Sau chuyển đổi, mở lại tệp đích bằng thư viện phù hợp; kiểm tra số trang, số hàng/cột, nội dung mẫu và ký tự tiếng Việt.
6. Báo rõ những gì không thể giữ nguyên, như macro, chữ ký số, công thức Excel chưa tính, bố cục phức tạp hoặc lớp OCR.

## Quy tắc
- Không xóa bản gốc hoặc ghi đè nếu chưa được yêu cầu.
- Với PDF scan, cần OCR trước khi chuyển thành văn bản có thể chỉnh sửa; không hứa giữ bố cục tuyệt đối.
- Với bảng tính, phân biệt giá trị hiển thị và công thức. Không chuyển công thức thành giá trị tĩnh nếu người dùng muốn tiếp tục tính toán.
- Với ảnh, giữ tỷ lệ khung hình và thông tin màu khi có thể; hỏi trước khi giảm chất lượng đáng kể.
- Nếu thiếu phần mềm chuyển đổi, nói rõ và đề xuất định dạng trung gian thực tế.
