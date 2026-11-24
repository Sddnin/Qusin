---
name: archive-management
display: Tạo và quản lý gói tệp nén
description: Tạo, kiểm tra, liệt kê và giải nén gói tệp
aliases: ["nén file", "giải nén", "tạo ZIP", "tạo TAR", "tạo 7Z", "nén thư mục", "gom file", "đóng gói dự án", "tạo file nén"]
terms: ["ZIP", "TAR", "TAR.GZ", "TGZ", "TAR.BZ2", "TAR.XZ", "GZIP", "GZ", "7Z", "RAR", "archive", "checksum", "manifest"]
---
## Chọn định dạng
- ZIP: tương thích rộng, phù hợp chia sẻ nhiều tệp.
- TAR: gom nhiều tệp, không tự nén; TAR.GZ/TGZ, TAR.BZ2, TAR.XZ có nén.
- GZ: nén một tệp, không phải gói nhiều tệp; muốn gom nhiều tệp hãy dùng TAR.GZ.
- 7Z: chỉ tạo/giải nén khi `7z` hoặc `7zz` có sẵn. Không tự cài phần mềm.
- RAR: chỉ xử lý khi công cụ phù hợp có sẵn; không tạo RAR bằng cách đổi đuôi ZIP.

## Tạo archive
1. Xác định các tệp/thư mục nguồn, cấu trúc mong muốn, định dạng, mức nén và có cần mật khẩu hay không. Nếu không chỉ định, ưu tiên ZIP.
2. Dùng `scripts/create_archive.py` đi kèm skill khi có thể; lấy đúng script từ kết quả đọc skill và chạy trong workspace hiện tại. Cú pháp: `create_archive.py FORMAT OUTPUT SOURCE...`. Các format: `zip`, `tar`, `tar.gz`, `tar.bz2`, `tar.xz`, `gz`, `7z`.
3. GZ chỉ nhận một tệp nguồn. 7Z yêu cầu chương trình `7z`/`7zz`. Không ghi mật khẩu vào dòng lệnh/log; nếu công cụ không hỗ trợ nhập mật khẩu an toàn, từ chối mã hóa thay vì tạo gói không bảo vệ.
4. Giữ cấu trúc tương đối, không đưa đường dẫn tuyệt đối hoặc thư mục cha thừa vào archive. Loại trừ `.env`, token, cache, build, `.git`, `node_modules` và dữ liệu nhạy cảm trừ khi người dùng yêu cầu rõ.

## Kiểm tra và giải nén
1. Liệt kê nội dung archive trước khi giải nén; kiểm tra số lượng, kích thước và tên trùng.
2. ZIP: `ZipFile.testzip()` hoặc `unzip -t`. TAR: mở lại với `tarfile` và liệt kê thành phần. 7Z: chạy kiểm tra bằng `7z t`/`7zz t` nếu có.
3. Chặn đường dẫn tuyệt đối, `..`, symlink nguy hiểm và đường dẫn thoát thư mục. Giải nén vào thư mục riêng.
4. Không chạy chương trình/script bên trong archive. Cảnh giác archive bomb, file rất lớn và extension giả.
5. Sau khi tạo, mở lại archive và so sánh danh sách tệp, kích thước; khi cần, tính SHA-256 manifest.
6. Chỉ giao sau khi xác thực. Báo rõ format, danh sách nội dung chính và những loại nào không được hỗ trợ.

## Giới hạn
Không phải mọi định dạng nén đều giống nhau. ZIP/TAR/TAR.GZ/TAR.BZ2/TAR.XZ/GZ có thể dùng Python chuẩn. 7Z/RAR phụ thuộc chương trình ngoài. Không tuyên bố hỗ trợ 7Z/RAR nếu chưa kiểm tra công cụ thực tế.
