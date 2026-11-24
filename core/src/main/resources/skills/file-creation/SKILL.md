---
name: file-creation
display: Tạo và xuất nhiều loại tệp
description: Tạo, xác thực và bàn giao nhiều loại tệp
aliases: ["tạo file", "tạo tệp", "làm file", "xuất file", "xuất tệp", "tạo tài liệu", "tạo Excel", "tạo Word", "tạo PowerPoint", "tạo PDF", "tạo ảnh", "tạo ZIP", "tạo nhiều file", "đóng gói file"]
terms: ["DOCX", "ODT", "RTF", "XLSX", "ODS", "CSV", "TSV", "PPTX", "PDF", "TXT", "MD", "HTML", "CSS", "JS", "PY", "JSON", "YAML", "XML", "TOML", "SQL", "SQLite", "SVG", "PNG", "JPEG", "WebP", "GIF", "TIFF", "ZIP", "TAR", "GZ", "7Z", "EPUB", "SRT", "VTT", "ICS", "VCF", "EML", "OBJ", "STL", "GLTF"]
---
## Mục tiêu
Tạo đúng định dạng người dùng yêu cầu, trong workspace hiện tại; kiểm tra tệp thật sự hợp lệ rồi giao bằng công cụ chia sẻ tệp. Dùng chung cho Cá nhân và Công việc, theo workspace hiện tại. Chỉ tạo hoặc xuất tệp khi công tắc **Tạo file** trong Cài đặt đang bật; nếu tắt, không tìm cách thay thế bằng lệnh shell hay script.

## Chọn định dạng
| Nhu cầu | Định dạng phổ biến | Công cụ thường dùng |
|---|---|---|
| Văn bản, ghi chú, mã nguồn | TXT, MD, RTF, TEX, PY, JS, TS, SQL, SH | Ghi UTF-8; RTF/TEX cần cú pháp đúng |
| Word/văn phòng | DOCX, ODT, RTF | python-docx; LibreOffice; skill opendocument-rtf |
| Bảng tính | XLSX, ODS, CSV, TSV | openpyxl/xlsxwriter; csv; LibreOffice cho ODS |
| Trình chiếu | PPTX, ODP | skill presentations; kiểm tra thư viện hoặc script đi kèm |
| Tài liệu cố định | PDF | reportlab/fpdf2; pypdf để đọc/ghép/tách |
| Web | HTML, CSS, JS, XML, SVG | Văn bản UTF-8, kiểm tra cú pháp và đường dẫn |
| Cấu trúc dữ liệu | JSON, YAML, XML, TOML, INI, SQLite/DB | json, PyYAML nếu có, sqlite3 chuẩn Python |
| Ảnh | PNG, JPG, JPEG, WebP, GIF, BMP, TIFF, SVG | Pillow; SVG là XML; GIF cần xử lý frame |
| Sách điện tử | EPUB | script EPUB đi kèm; MOBI/KPF cần công cụ ngoài |
| Nén/đóng gói | ZIP, TAR, TAR.GZ/TGZ, TAR.BZ2, TAR.XZ, GZ, 7Z | script archive; 7Z chỉ khi có 7z/7zz |
| Phụ đề, lịch, danh bạ, email | SRT, VTT, ICS, VCF, EML | Văn bản theo đúng đặc tả |
| Âm thanh/video | WAV, MP3, M4A, FLAC, MP4, MOV, WEBM, MKV | FFmpeg hoặc thư viện media; kiểm tra lệnh trước khi dùng |
| Mô hình 3D | OBJ, MTL, STL ASCII, GLTF | Sinh văn bản/mesh theo dữ liệu; GLB cần đóng gói nhị phân đúng chuẩn |

Không hứa tạo định dạng chỉ vì biết phần mở rộng. RAR, PSD, AI, CAD, font, codec hoặc định dạng độc quyền cần ứng dụng/thư viện chuyên dụng. Chỉ tạo khi môi trường xác nhận có công cụ hỗ trợ; không đổi đuôi để giả lập.

## Quy trình
1. Xác định định dạng, nội dung, đối tượng nhận, khả năng chỉnh sửa, kích thước và có cần nhiều tệp hay không. 
2. Đọc tệp nguồn nếu có; giữ nguyên bản gốc. Không tự thêm số liệu, dẫn chứng hoặc dữ kiện.
3. Tạo tệp trong thư mục đầu ra của workspace hiện tại. Tên tệp ngắn, dễ hiểu; không ghi đè nếu chưa được yêu cầu.
4. Chọn skill chuyên biệt tương ứng. Nhiều tệp: dùng archive-management để đóng gói ZIP/TAR.
5. Kiểm tra bằng parser/reader đúng định dạng: DOCX/XLSX/PPTX mở lại được; PDF đọc được; ảnh mở được; JSON/XML parse được; SQLite truy vấn được; archive test được.
6. Kiểm tra encoding, font, công thức, số trang/slide và nội dung mẫu. Render nếu có công cụ.
7. Giao đúng đường dẫn thực tế bằng công cụ chia sẻ tệp. Nói rõ định dạng và những phần chưa được kiểm chứng.

## Quy tắc bắt buộc
- Tệp nhị phân phải được tạo bằng script/thư viện phù hợp, không dùng thao tác ghi văn bản để giả lập.
- Không tự cài dependency hoặc chạy tệp không tin cậy nếu chưa được phép. Kiểm tra công cụ có sẵn trước.
- Không đưa mật khẩu, API key, token, `.env`, khóa riêng hoặc dữ liệu ngoài phạm vi vào tệp/gói.
- Với XLSX, phân biệt công thức và giá trị đã tính. Với PDF scan, OCR riêng. Với âm thanh/video, chỉ chuyển mã khi FFmpeg hoặc công cụ tương đương tồn tại.
- ZIP/TAR phải chứa đúng tệp cần giao, không gom cả workspace. Kiểm tra archive trước khi gửi.
- Nếu công cụ thiếu, báo giới hạn và đề xuất định dạng thay thế; không tuyên bố hoàn thành khi chưa tạo và kiểm tra tệp.
