---
name: ebook-files
display: Tạo sách điện tử
description: Tạo và kiểm tra sách EPUB cùng tài liệu đọc trên thiết bị
aliases: ["tạo ebook", "tạo EPUB", "xuất EPUB", "làm sách điện tử", "chuyển văn bản thành ebook"]
terms: ["EPUB", "ebook", "e-book", "mimetype", "OPF", "XHTML", "Kindle", "MOBI", "KPF"]
---
## Định dạng
EPUB là định dạng mở có thể tạo bằng ZIP + XHTML/OPF. MOBI/KPF cần công cụ chuyên dụng; không đổi đuôi EPUB thành MOBI.

## Quy trình
1. Thu thập tiêu đề, tác giả, ngôn ngữ, nội dung chương, bìa và metadata người dùng muốn hiển thị.
2. Chuẩn hóa chương, heading, mục lục, chú thích và hình; bảo đảm đường dẫn tài nguyên tương đối.
3. Dùng `scripts/create_epub.py` đi kèm khi nguồn là văn bản thuần; hoặc công cụ EPUB phù hợp nếu cần CSS, ảnh và nhiều chương.
4. Với EPUB, mục `mimetype` phải đứng đầu archive và không nén; `META-INF/container.xml`, package OPF, manifest, spine và nav phải liên kết đúng.
5. Kiểm tra ZIP, XML/XHTML parse được, mọi đường dẫn trong manifest tồn tại và số chương chính xác.
6. Nếu có EPUBCheck/reader, kiểm tra thêm bằng công cụ đó; không tuyên bố EPUBCheck pass nếu chưa chạy.

## Chất lượng
Giữ nguyên nội dung nguồn, không tự thêm đoạn văn. Rà soát tiếng Việt, dấu câu, mục lục, tiêu đề và bản quyền ảnh. Nêu rõ script đơn giản chỉ hỗ trợ nội dung văn bản cơ bản, không thay thế dàn trang xuất bản chuyên nghiệp.
