---
name: localization-translation-files
display: Tệp bản địa hóa
description: Tạo và chỉnh sửa PO, POT, XLIFF, JSON locale và RESX
aliases: ["tệp bản địa hóa", "tạo tệp bản địa hóa", "xử lý tệp bản địa hóa"]
terms: ["PO", "POT", "MO", "XLIFF", "XLiff", "JSON locale", "RESX", "ARB"]
---
## Quy trình
1. Xác định locale nguồn/đích, quy tắc số/ngày/tiền tệ và framework sử dụng.
2. Giữ nguyên message ID, key, placeholder, biến nội suy, thẻ HTML và plural forms.
3. Không dịch tên biến, đường dẫn, mã định danh hoặc chuỗi kỹ thuật được đánh dấu.
4. Với PO/POT, giữ header charset và plural rule phù hợp locale; MO là dạng biên dịch, cần công cụ gettext.
5. Với XLIFF, giữ cấu trúc XML, ID và trạng thái segment.
6. Kiểm tra placeholder parity, key trùng/thiếu, cú pháp và hiển thị dấu tiếng Việt.

Không tự xóa chuỗi chưa dịch hoặc thay placeholder bằng nội dung đoán.
