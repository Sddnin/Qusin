---
name: web-files
display: Tạo tệp web và giao diện
description: Tạo HTML, CSS, JavaScript và tài nguyên web
aliases: ["tạo website", "tạo HTML", "tạo CSS", "tạo JavaScript", "làm landing page", "xuất trang web"]
terms: ["HTML", "CSS", "JavaScript", "TypeScript", "JS", "TS", "web", "website", "responsive", "accessibility"]
---
## Quy trình
1. Xác định sản phẩm là một trang tĩnh hay dự án framework; hỏi/diễn giải rõ môi trường chạy nếu ảnh hưởng cấu trúc.
2. Với trang tĩnh, tạo `index.html`, `styles.css`, `script.js` và assets cần thiết; giữ đường dẫn tương đối.
3. Với dự án framework, tạo package/config/README đúng công cụ, chỉ thêm dependency thật sự cần.
4. Dùng semantic HTML, CSS responsive, nhãn form, keyboard navigation, trạng thái focus và alt cho ảnh.
5. Không nhúng secret vào frontend. Không tuyên bố website đã triển khai nếu chỉ tạo source.
6. Kiểm tra HTML/XML, cú pháp JS, đường dẫn asset và chạy preview/test nếu môi trường hỗ trợ.
7. Nếu có nhiều tệp, đóng gói project bằng archive-management; loại bỏ node_modules, cache, secret và output build không cần thiết.
