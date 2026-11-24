---
name: image-files
display: Tạo tệp hình ảnh
description: Tạo và chỉnh sửa hình ảnh bằng Pillow hoặc SVG
aliases: ["tạo ảnh", "tạo hình", "xuất PNG", "tạo JPG", "tạo WebP", "tạo GIF", "vẽ biểu đồ thành ảnh", "làm ảnh bìa", "tạo thumbnail", "tạo icon"]
terms: ["ảnh", "hình ảnh", "PNG", "JPG", "JPEG", "WebP", "GIF", "BMP", "TIFF", "SVG", "Pillow", "PIL", "thumbnail", "icon", "pixel", "kích thước ảnh"]
---
## Đầu vào

Xác định ảnh dùng ở đâu, kích thước, tỉ lệ, định dạng, nền trong suốt hay không, và nội dung phải xuất hiện. Nếu chỉnh sửa ảnh nguồn, giữ bản gốc và tạo bản mới trong `/workspace/out/`.

## Các bước

1. Dùng Pillow (`from PIL import Image, ImageDraw, ImageFont`) để tạo hoặc xử lý PNG, JPEG, WebP, GIF, BMP, TIFF. Pillow đã có trong runtime Qusin.
2. Với logo, icon hoặc hình vector đơn giản, có thể viết SVG hợp lệ bằng `write_file`; SVG là XML dạng văn bản. Không đổi phần mở rộng SVG thành PNG.
3. Tạo ảnh với kích thước phù hợp đích dùng. Dùng `RGBA` cho trong suốt, `RGB` cho JPEG. JPEG không hỗ trợ alpha; chuyển nền trong suốt sang màu nền rõ ràng trước khi lưu JPG.
4. Với ảnh có chữ tiếng Việt, dùng font Unicode thực sự có trong máy; không tự đoán font. Kiểm tra đường dẫn font trước khi gọi `ImageFont.truetype`.
5. Lưu file theo phần mở rộng đúng với định dạng đã chọn. GIF động cần nhiều frame; không lưu một frame đơn rồi giới thiệu là animation.
6. Mở lại bằng `Image.open(path)`, gọi `verify()` hoặc `load()`, kiểm tra `format`, `size`, `mode` và dung lượng. Nếu cần, tạo thumbnail để xác nhận bố cục.
7. Dùng `share_file` để giao ảnh. Với tệp ảnh được gửi qua kênh nhắn tin có hỗ trợ ảnh, công cụ sẽ xử lý việc gửi ảnh theo khả năng hiện có.

## Lưu ý

- Pillow không phải công cụ sinh ảnh AI. Chỉ vẽ/chỉnh sửa theo khả năng xử lý ảnh thật sự của môi trường; không tuyên bố đã tạo hình ảnh bằng AI.
- SVG phải có phần tử gốc `<svg>`, `viewBox` hợp lệ và các phần tử được đóng đúng. Nếu cần raster hóa, kiểm tra xem có công cụ chuyển đổi SVG nào được cài; không mặc định Pillow đọc được SVG.
- Đừng phóng ảnh nhỏ lên quá lớn rồi nói đó là ảnh độ phân giải cao. Không xóa watermark hoặc dấu sở hữu trên ảnh không thuộc người dùng.

## Hoàn thành

Ảnh mở lại được, đúng kích thước/định dạng/nền, nội dung không bị cắt và `share_file` xác nhận giao thành công.
