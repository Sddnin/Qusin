---
name: presentations
display: Tạo bài trình chiếu PowerPoint
description: Tạo slide PowerPoint chỉnh sửa được và kiểm tra tệp
aliases: ["tạo PowerPoint", "làm PowerPoint", "tạo PPTX", "làm slide", "tạo slide", "xuất bài thuyết trình", "presentation", "slide thuyết trình"]
terms: ["PowerPoint", "PPTX", "slide", "trình chiếu", "thuyết trình", "bài giảng", "báo cáo", "pitch deck", "speaker notes", "bố cục"]
---
## Nguyên tắc

Mỗi slide cần một thông điệp chính. Tiêu đề nên nêu kết luận hoặc ý quan trọng, phần thân chỉ giữ dẫn chứng và ý bổ trợ. Ưu tiên nội dung người dùng cung cấp; không bịa số liệu, biểu đồ hay nguồn.

## Tạo PowerPoint

1. Xác định khán giả, mục đích, thời lượng, số slide và phong cách. Nếu người dùng không chỉ định, tạo cấu trúc ngắn gồm mở đầu, nội dung chính, kết luận và bước tiếp theo.
2. Tạo nội dung theo thứ tự: câu chuyện/chủ đề → dàn ý slide → tiêu đề kết luận → các gạch đầu dòng ngắn. Không bê nguyên bài luận thành đoạn chữ dài lên slide.
3. Tạo `/workspace/out/` và file JSON với dạng:

   ```json
   {
     "title": "Tiêu đề bài trình chiếu",
     "author": "Qusin",
     "slides": [
       {"title": "Ý chính 1", "bullets": ["Luận điểm", "Dẫn chứng", "Hàm ý"]},
       {"title": "Kết luận", "text": "Thông điệp cuối cùng"}
     ]
   }
   ```

4. Với slide cơ bản có tiêu đề, đoạn văn và gạch đầu dòng, dùng script `scripts/create_pptx.py` đi kèm skill. Từ `skill_read`, lấy `scriptSource`; chạy `run_bundled_script` với `source` đúng thư mục skill, `script` là `scripts/create_pptx.py`, `args` gồm đường dẫn JSON và đường dẫn `.pptx` đầu ra. Script chỉ dùng Python chuẩn, tạo các hộp văn bản PowerPoint có thể chỉnh sửa và kiểm tra ZIP/XML trước khi thay tệp.
5. Nếu môi trường có `python-pptx`, có thể dùng nó cho ảnh, shape, bảng và đồ họa bổ sung; kiểm tra bằng `python3 -c "import pptx"` trước. Không giả định module có sẵn vì runtime Qusin hiện không đóng gói `python-pptx`.
6. Với đồ họa cần thiết, tạo ảnh/biểu đồ riêng bằng Pillow rồi nhúng nếu công cụ tạo PPTX hiện có hỗ trợ. Nếu chỉ có script cơ bản, giữ bài ở dạng văn bản chỉnh sửa được thay vì giả vờ có biểu đồ/hình ảnh.
7. Kiểm tra bằng `unzip -t`, xác minh các XML và số slide. Nếu có LibreOffice hoặc `python-pptx`, mở/xuất thử để kiểm tra khả năng đọc và hiển thị. Sau đó dùng `share_file` giao `.pptx`.

## Quy tắc thiết kế

- Tỉ lệ mặc định 16:9. Tiêu đề lớn hơn phần thân; chữ phải đọc được trên màn hình điện thoại/laptop.
- Mỗi slide chỉ giữ vài ý ngắn. Dùng biểu đồ thay cho bảng dài khi dữ liệu phù hợp và có dữ liệu thật.
- Không thêm hình trang trí không liên quan; không dùng ảnh có bản quyền không rõ nguồn.
- Không tạo speaker notes, animation, transition, video nhúng, SmartArt hoặc biểu đồ động khi công cụ đang dùng không hỗ trợ chúng.

## Hoàn thành

PPTX là gói Open XML hợp lệ, có số slide đúng, văn bản chỉnh sửa được và `share_file` xác nhận giao. Nói rõ nếu mới kiểm tra cấu trúc mà chưa kết xuất để xem trực quan.
