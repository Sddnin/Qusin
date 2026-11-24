---
name: logs-and-technical-text
display: Log và tệp kỹ thuật văn bản
description: Đọc, lọc, phân tích và tạo log/config text an toàn
aliases: ["log và tệp kỹ thuật văn bản", "tạo log và tệp kỹ thuật văn bản", "xử lý log và tệp kỹ thuật văn bản"]
terms: ["LOG", "OUT", "ERR", "TRACE", "DIFF", "PATCH", "INI", "CONF", "ENV example"]
---
## Quy trình
1. Nhận diện encoding, định dạng timestamp, mức log và nguồn phát sinh.
2. Khi phân tích, giữ nguyên log gốc; tạo bản lọc riêng và ghi tiêu chí lọc.
3. Che token, cookie, email, địa chỉ và dữ liệu nhạy cảm trước khi chia sẻ; không làm mất thông tin cần để tái hiện lỗi.
4. Tạo unified diff/patch đúng cú pháp; kiểm tra patch áp dụng được trên đúng phiên bản nguồn.
5. Với `.env`, chỉ tạo `.env.example` bằng placeholder; tuyệt đối không tự điền secret.
6. Với log lớn, xử lý streaming theo dòng, không nạp toàn bộ vào bộ nhớ nếu không cần.

Không sửa timestamp hoặc nội dung log để làm đẹp kết quả; phân biệt log nguyên bản với bản đã chuẩn hóa.
