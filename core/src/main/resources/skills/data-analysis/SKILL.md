---
name: data-analysis
display: Phân tích dữ liệu và bảng tính
description: Đọc, làm sạch, phân tích và trình bày dữ liệu từ tệp
aliases: ["phân tích dữ liệu", "phân tích Excel", "thống kê dữ liệu", "làm sạch dữ liệu", "vẽ biểu đồ từ file", "tổng hợp bảng"]
terms: ["CSV", "XLSX", "Excel", "pandas", "openpyxl", "thống kê", "biểu đồ", "pivot", "dữ liệu", "báo cáo"]
---
## Mục tiêu
Biến dữ liệu thô thành kết quả có thể kiểm tra, giải thích và sử dụng để ra quyết định.

## Quy trình
1. Đọc cấu trúc tệp: tên sheet, cột, kiểu dữ liệu, số dòng, đơn vị và các giá trị thiếu. Không giả định ý nghĩa cột khi chưa có căn cứ.
2. Giữ nguyên tệp nguồn; tạo bản làm việc riêng. Ghi lại các bước làm sạch có ảnh hưởng đến dữ liệu.
3. Kiểm tra dữ liệu trùng, sai kiểu, khoảng giá trị bất thường, ngày tháng, đơn vị và lỗi nhập liệu. Không tự xóa ngoại lệ nếu chưa xác định đó là lỗi.
4. Tính toán bằng công cụ phù hợp (`pandas`, `csv`, `openpyxl`). Kiểm tra mẫu thủ công hoặc đối chiếu tổng trước/sau.
5. Chọn biểu đồ phù hợp với câu hỏi: xu hướng theo thời gian, so sánh nhóm, phân bố hoặc quan hệ giữa biến. Ghi rõ đơn vị và phạm vi dữ liệu.
6. Xuất kết quả theo yêu cầu: XLSX có sheet dữ liệu/kết quả, CSV, biểu đồ PNG hoặc báo cáo PDF/DOCX.
7. Nêu phương pháp, giả định, giới hạn và các dòng bị loại/sửa. Không trình bày tương quan như quan hệ nhân quả.

## Chất lượng
- Không bịa dữ liệu còn thiếu; đánh dấu là thiếu hoặc hỏi người dùng.
- Không làm tròn quá sớm trong tính toán.
- Công thức, bộ lọc và biểu đồ phải tham chiếu đúng vùng dữ liệu.
- Khi số liệu mâu thuẫn, nêu rõ thay vì âm thầm chọn một phiên bản.
