---
name: word-documents
display: Tạo tài liệu Word
description: Soạn và định dạng tài liệu Word có thể chỉnh sửa
aliases: ["tạo Word", "làm Word", "xuất DOCX", "tạo DOCX", "soạn thảo văn bản", "làm báo cáo Word", "hợp đồng Word", "đơn Word"]
terms: ["Word", "DOCX", "python-docx", "văn bản", "báo cáo", "hợp đồng", "đơn", "bìa", "mục lục", "bảng", "định dạng trang"]
---
## Đầu vào

Làm rõ mục đích, người đọc, ngôn ngữ và cấu trúc. Nếu người dùng đưa mẫu DOCX, hãy đọc nội dung và mẫu trước; giữ những phần họ yêu cầu giữ. Nếu đề bài dựa trên tài liệu nguồn, không tự bổ sung dữ kiện ngoài nguồn.

## Các bước

1. Tạo `/workspace/out/` và dùng tên tệp `.docx` dễ nhận biết.
2. Dùng `python-docx` (`from docx import Document`) để tạo tệp Word thật. Tạo style cho Normal và Heading 1–3; thiết lập khổ giấy, lề, font và giãn dòng phù hợp.
3. Tạo cấu trúc rõ: tiêu đề, thông tin đầu tài liệu nếu cần, các heading phân cấp, đoạn văn, danh sách và bảng. Bảng có hàng tiêu đề, không nhồi đoạn quá dài vào một ô.
4. Với nội dung tiếng Việt, dùng font có hỗ trợ Unicode. Khi cần, thiết lập font cho cả `w:rFonts` để Word không thay font ở ký tự Đông Á.
5. Thêm header/footer, số trang, mục lục tự động hoặc trường ngày chỉ khi cần và làm đúng cấu trúc Word.
6. Lưu `.docx`, sau đó mở lại bằng `Document(path)`. Kiểm tra đoạn, bảng, heading và dữ liệu; nếu có LibreOffice trong sandbox thì có thể xuất PDF để kiểm tra trang, nhưng không giả định công cụ đó luôn có.
7. Dùng `share_file` để giao tệp `.docx`; nếu người dùng muốn cả bản PDF, tạo PDF riêng bằng ReportLab hoặc công cụ chuyển đổi thực sự có sẵn.

## Lưu ý

- DOCX là gói Open XML, không phải HTML hay văn bản đổi đuôi.
- `python-docx` tạo và chỉnh sửa phần nội dung phổ biến, nhưng không hỗ trợ mọi chức năng nâng cao của Word. Với biểu mẫu phức tạp, macro, chữ ký số hoặc layout đặc thù, không hứa giữ nguyên hoàn toàn.
- Không tạo mục lục với số trang đoán. Chỉ ghi số trang sau khi có trình kết xuất tính trang.
- Không tự ký, đóng dấu hoặc ghi thông tin pháp lý thay người dùng.

## Hoàn thành

DOCX mở lại được, tiêu đề/bảng/định dạng chính xác, dữ liệu khớp yêu cầu và `share_file` xác nhận đã giao.
