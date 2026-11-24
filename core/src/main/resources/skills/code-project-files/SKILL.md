---
name: code-project-files
display: Tạo và đóng gói dự án mã nguồn
description: Tạo dự án mã nguồn, cấu hình và gói bàn giao
aliases: ["tạo project", "tạo dự án code", "xuất source code", "đóng gói source", "tạo ứng dụng mẫu", "scaffold project"]
terms: ["project", "source code", "Python", "JavaScript", "TypeScript", "HTML", "CSS", "SQL", "README", "package.json", "requirements.txt", "ZIP"]
---
## Mục tiêu
Tạo dự án có cấu trúc rõ, hướng dẫn chạy được và không chứa bí mật hoặc phụ thuộc không cần thiết.

## Quy trình
1. Làm rõ nền tảng, ngôn ngữ, mục tiêu, môi trường chạy và yêu cầu đầu ra. Nếu người dùng yêu cầu chỉnh dự án có sẵn, đọc cấu trúc và quy ước hiện tại trước.
2. Tạo cấu trúc tối thiểu phù hợp; không sinh hàng loạt tệp mẫu không có tác dụng.
3. Viết mã nguồn, cấu hình phụ thuộc, README và ví dụ cấu hình an toàn (`.env.example`, không chứa giá trị bí mật thật).
4. Thêm kiểm thử phù hợp và chạy được các kiểm thử trong môi trường hiện có. Không tuyên bố build thành công nếu chưa chạy.
5. Kiểm tra cú pháp, import, đường dẫn tương đối, tên tệp, lệnh khởi chạy và các phụ thuộc.
6. Tạo ZIP chỉ gồm mã nguồn và tài liệu cần thiết. Loại bỏ cache, thư mục build, `node_modules`, `.venv`, tệp tạm và bí mật.
7. README phải nêu yêu cầu môi trường, cách cài đặt, cách chạy, cách kiểm thử và giới hạn đã biết.

## An toàn và chất lượng
- Không nhúng API key, token, mật khẩu, khóa riêng hoặc thông tin người dùng vào source.
- Không tải và chạy dependency không rõ nguồn gốc nếu chưa được yêu cầu.
- Không giả lập kết quả kiểm thử. Phân biệt rõ kiểm tra tĩnh, unit test, build và chạy thực tế.
- Ưu tiên thay đổi nhỏ, dễ xem xét; không phá vỡ cấu trúc hoặc quy ước hiện hữu.
