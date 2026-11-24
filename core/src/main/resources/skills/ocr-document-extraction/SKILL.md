---
name: ocr-document-extraction
display: OCR và trích xuất tài liệu
description: Trích xuất chữ, bảng và cấu trúc từ PDF scan hoặc hình ảnh
aliases: ["OCR", "đọc ảnh thành chữ", "trích xuất văn bản", "đọc PDF scan", "ảnh sang Word", "lấy chữ từ ảnh"]
terms: ["OCR", "scan", "PDF", "ảnh", "trích xuất", "Tesseract", "bảng", "nhận dạng ký tự", "tiếng Việt"]
---
## Mục tiêu
Chuyển nội dung trong ảnh hoặc PDF scan thành văn bản có thể tìm kiếm, chỉnh sửa và kiểm chứng.

## Quy trình
1. Kiểm tra trang/tệp có lớp văn bản hay chỉ là ảnh. Nếu đã có lớp chữ, ưu tiên trích xuất trực tiếp trước OCR.
2. Kiểm tra chất lượng ảnh: độ phân giải, xoay trang, độ tương phản, bóng và chữ viết tay. Tách trang khi cần.
3. Chọn OCR có hỗ trợ ngôn ngữ phù hợp, đặc biệt tiếng Việt. Kiểm tra công cụ và gói ngôn ngữ đã cài; không giả định Tesseract luôn có sẵn.
4. Chạy OCR theo trang, giữ thứ tự trang và tọa độ nếu cần tái dựng bố cục.
5. Rà soát các lỗi dễ nhầm: dấu tiếng Việt, số 0/O, 1/I/l, dấu thập phân, ngày tháng, mã số, bảng và công thức.
6. Với bảng, tái dựng hàng/cột thành XLSX hoặc CSV; không biến các ô liền kề thành một đoạn văn không cấu trúc.
7. Xuất bản văn bản và ghi chú phần không chắc chắn. Nếu cần, giữ ảnh gốc cạnh văn bản OCR để đối chiếu.

## Quy tắc
- Không đoán từ bị mờ hoặc mất nét; đánh dấu `[không đọc rõ]`.
- Không khẳng định OCR chính xác tuyệt đối, nhất là chữ viết tay, con dấu và bảng phức tạp.
- Với tài liệu nhiều trang, báo số trang xử lý thành công và trang lỗi.
- Bảo vệ dữ liệu cá nhân trong tài liệu; không đưa nội dung nhạy cảm vào log hoặc tên tệp.
