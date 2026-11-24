---
name: batch-file-processing
display: Xử lý hàng loạt tệp
description: Xử lý hàng loạt tệp theo cùng một quy trình
aliases: ["xử lý hàng loạt", "đổi tên hàng loạt", "chuyển đổi nhiều file", "nén nhiều file", "resize nhiều ảnh"]
terms: ["batch", "bulk", "hàng loạt", "nhiều tệp", "nhiều file", "manifest"]
---
## Quy trình
1. Xác định phạm vi file, quy tắc chọn tệp, thao tác, thư mục đích và cách xử lý lỗi.
2. Liệt kê trước các tệp dự kiến xử lý; bỏ qua thư mục hệ thống, tệp tạm và tệp không đúng loại.
3. Tạo bản sao hoặc đầu ra mới, không ghi đè nguồn. Dùng tên ổn định, tránh trùng tên và giữ phần mở rộng đúng.
4. Xử lý theo lô nhỏ nếu số lượng lớn; ghi manifest gồm nguồn, đầu ra, trạng thái và lỗi, không lưu bí mật.
5. Kiểm tra số lượng đầu vào/đầu ra, mở ngẫu nhiên và kiểm tra toàn bộ tệp lỗi.
6. Nếu có lỗi một phần, không báo toàn bộ thành công; giao danh sách file thành công và file lỗi riêng.

## An toàn
Không chạy macro/script trong tệp. Không xóa hay ghi đè hàng loạt nếu chưa có xác nhận rõ. Với đổi tên, kiểm tra xung đột trước khi thực hiện.
