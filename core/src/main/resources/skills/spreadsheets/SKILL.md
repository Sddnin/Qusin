---
name: spreadsheets
display: Tạo bảng tính Excel
description: Tạo Excel và CSV có công thức, định dạng, biểu đồ
aliases: ["tạo Excel", "làm Excel", "xuất XLSX", "tạo XLSX", "lập bảng tính", "bảng dữ liệu", "làm bảng thống kê", "bảng chấm công", "bảng thu chi", "bảng tính điểm"]
terms: ["Excel", "XLSX", "XLSM", "CSV", "TSV", "openpyxl", "xlsxwriter", "công thức", "hàm", "biểu đồ", "pivot", "bảng", "số liệu", "thống kê"]
---
## Đầu vào

Xác định cột, dòng, sheet, đơn vị, kỳ thời gian, cách tính và người dùng sẽ mở tệp bằng Excel hay phần mềm khác. Với dữ liệu từ tệp nguồn, giữ nguyên giá trị và ghi rõ ô trống, dữ liệu lỗi hoặc cách chuẩn hóa.

## Các bước

1. Tạo `/workspace/out/` và chọn `.xlsx` cho workbook có nhiều sheet, công thức, định dạng hoặc biểu đồ; chọn `.csv`/`.tsv` cho bảng phẳng cần trao đổi giữa hệ thống.
2. Dùng `openpyxl` hoặc `xlsxwriter` (đã có trong runtime Qusin). Dùng Python `csv` cho CSV/TSV.
3. Đặt tên sheet và cột dễ hiểu; cố định hàng tiêu đề khi bảng dài. Dùng kiểu dữ liệu đúng: ngày là ngày, số tiền là số, tỷ lệ là số thập phân có định dạng phần trăm.
4. Nếu dùng công thức, tránh ghi kết quả bằng tay thay cho công thức khi người dùng cần bảng cập nhật. Kiểm tra địa chỉ ô và phạm vi tham chiếu; không dùng công thức có thể tự chạy mã hoặc truy cập ngoài.
5. Áp dụng định dạng có mục đích: tiêu đề, chiều rộng cột, định dạng số, bộ lọc, freeze panes, viền vừa phải và màu để phân biệt trạng thái. Không tô màu toàn bộ ô tùy tiện.
6. Nếu cần biểu đồ, chọn loại phù hợp với câu hỏi, đặt tiêu đề, tên trục, đơn vị và nguồn dữ liệu đúng.
7. Lưu tệp, mở lại bằng `openpyxl.load_workbook(path, data_only=False)`, kiểm tra sheet, vùng ô, công thức, định dạng ngày/tiền và dữ liệu đầu/cuối. Không dùng `data_only=True` để đánh giá công thức đã được tính nếu chưa mở bằng spreadsheet engine.
8. CSV dùng UTF-8-SIG nếu người dùng dự kiến mở bằng Excel Windows; đặt dấu phân cách đúng yêu cầu (thông thường dấu phẩy, có thể dấu chấm phẩy theo locale). Bảo vệ khỏi CSV formula injection: chuỗi không tin cậy bắt đầu bằng `=`, `+`, `-`, `@` cần được xử lý an toàn khi xuất cho người dùng mở bằng bảng tính.
9. Dùng `share_file` để giao đúng tệp.

## Lưu ý

- Không tự suy diễn số liệu bị thiếu. Nếu phải tính từ dữ liệu nguồn, thể hiện cách tính hoặc thêm sheet ghi chú.
- Không tuyên bố công thức đã cho ra kết quả cuối nếu môi trường chỉ ghi công thức mà không tính. Có thể dùng LibreOffice headless nếu `command -v libreoffice` xác nhận có sẵn; luôn mở lại tệp sau khi tính.
- CSV không lưu được nhiều sheet, công thức kiểu Excel, định dạng ô hoặc biểu đồ. Không đổi đuôi CSV thành XLSX.
- Không tạo macro/VBA hoặc tệp XLSM nếu không có yêu cầu và công cụ phù hợp.

## Hoàn thành

Tệp mở lại được, cấu trúc và dữ liệu đúng, công thức đã được kiểm tra ở mức công cụ cho phép, sau đó `share_file` xác nhận giao thành công.
