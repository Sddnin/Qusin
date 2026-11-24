---
name: create-files
display: Tạo file
description: Điều phối tạo, sửa, kiểm tra và xuất tệp
aliases: ["tạo file", "tạo tệp", "làm file", "xuất file", "làm thành PDF", "tạo Word", "tạo Excel", "tạo PowerPoint", "tạo ảnh", "tạo ZIP", "create a file", "export file"]
terms: ["DOCX", "ODT", "RTF", "XLSX", "ODS", "CSV", "TSV", "PPTX", "PDF", "TXT", "MD", "HTML", "JSON", "XML", "SQLite", "SVG", "PNG", "JPEG", "WebP", "GIF", "ZIP", "TAR", "EPUB", "SRT", "VTT", "ICS", "VCF"]
---
## Phạm vi

Dùng kỹ năng này khi người dùng yêu cầu tạo, chỉnh sửa, chuyển đổi, đóng gói hoặc xuất một tệp có thể lưu, chia sẻ hay in. Áp dụng cho cả Personal và Work. Personal chỉ chuyển yêu cầu tạo tệp sang tác vụ Work khi công tắc **Tạo file** trong Cài đặt đang bật. Nếu công tắc tắt, không tạo hoặc xuất tệp; giải thích cách bật lại.

Luôn dùng workspace của cuộc trò chuyện hiện tại. Đặt tệp hoàn chỉnh trong `/workspace/out/`; tệp nguồn tải lên nằm trong `/workspace/inbox/` và không được ghi đè.

## Chọn định dạng

- Dữ liệu cần sửa hoặc tính toán: `.xlsx`; dữ liệu trao đổi đơn giản: `.csv` UTF-8-SIG.
- Báo cáo, thư, CV: `.docx`; nội dung cố định để gửi hoặc in: `.pdf`.
- Bài thuyết trình: `.pptx`; trang xem trên điện thoại: `.html` có viewport và không phụ thuộc tài nguyên mạng.
- Ghi chú: `.md`; dữ liệu có cấu trúc: `.json`, `.xml`, `.yaml`, `.sqlite`.
- Hình: dùng công cụ ảnh khi hình minh họa linh hoạt; dùng `.png` khi cần chữ/bố cục chính xác.
- Nhiều tệp: dùng skill `archive-management` để tạo `.zip` hoặc gói phù hợp.

Nếu người dùng chưa chọn định dạng, chọn theo mục đích và nói ngắn gọn định dạng sẽ tạo. Không giả vờ hỗ trợ định dạng độc quyền nếu môi trường không có công cụ phù hợp.

## Quy trình

1. Đọc nội dung nguồn và skill chuyên biệt tương ứng. Giữ nguyên dữ kiện, số liệu, tên và ngày tháng; không tự bịa thông tin còn thiếu.
2. Nội dung văn bản có thể ghi UTF-8 bằng `write_file` trực tiếp vào `/workspace/out/`.
3. Tệp nhị phân như DOCX, XLSX, PPTX, PDF, EPUB hoặc ZIP phải được tạo bằng thư viện/script phù hợp. Ghi script vào `/workspace/tmp/`, sau đó chạy `run_command` hoặc script đã đăng ký bằng `run_bundled_script`.
4. Dùng tài liệu trong `references/office-and-pdf.md` hoặc `references/web-images-data.md` để tra mẫu mã. Với định dạng đặc biệt, đọc skill tương ứng trước khi tạo.
5. Kiểm tra tệp bằng parser hoặc script xác thực. Có thể chạy `scripts/check_file.py` của skill này qua `run_bundled_script` với `source: "skills/create-files"` và đường dẫn đầu ra tương đối workspace. Xem cảnh báo và xử lý trước khi giao.
6. Với PDF, ảnh hoặc slide, xem trước nếu thiết bị có công cụ render/hiển thị phù hợp. Kiểm tra font tiếng Việt, trang/slide rỗng, nội dung bị cắt và hình ảnh sai.
7. Chỉ dùng `share_file` khi người dùng muốn lưu vào Download/thư mục trên điện thoại. Nếu không, trả đường dẫn workspace thực tế để người dùng mở trong cuộc trò chuyện.
8. Trong câu trả lời, nói rõ tệp nào đã tạo và điều gì đã được kiểm tra. Nêu giới hạn nếu chưa thể render hoặc xác thực hoàn toàn.

## An toàn và giới hạn

- Không ghi đè tệp nguồn; dùng tên đầu ra mới, ngắn, không có khoảng trắng.
- Không đưa mật khẩu, token, khóa API, `.env` hoặc dữ liệu riêng tư ngoài yêu cầu vào tệp hay archive.
- Không cài dependency, chạy script không tin cậy, hay tải dữ liệu ngoài mạng khi chưa được cho phép.
- Không đổi phần mở rộng để giả vờ chuyển đổi định dạng. Chỉ nói đã tạo thành công sau khi tệp thực sự tồn tại và kiểm tra được.
- PDF scan, PSD/AI, CAD, font, codec và định dạng độc quyền cần công cụ chuyên dụng; nếu thiếu, báo rõ và đề xuất định dạng thay thế.
