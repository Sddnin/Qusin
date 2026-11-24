---
name: structured-data-files
display: Tạo tệp dữ liệu có cấu trúc
description: Tạo và kiểm tra JSON, YAML, XML, TOML và INI
aliases: ["tạo JSON", "tạo YAML", "tạo XML", "tạo TOML", "tạo config", "xuất dữ liệu có cấu trúc"]
terms: ["JSON", "YAML", "XML", "TOML", "INI", "config", "schema", "encoding", "UTF-8"]
---
## Quy trình
1. Xác định schema, encoding, kiểu dữ liệu, trường bắt buộc và phần mềm sẽ đọc tệp.
2. Dùng serializer/parser chuẩn thay vì tự nối chuỗi khi định dạng có cấu trúc.
3. JSON: kiểm tra bằng `json.loads`; XML: parse namespace và escaping đúng; TOML: dùng parser tương thích phiên bản; YAML: kiểm tra PyYAML hoặc parser có sẵn.
4. Giữ phân biệt `null`, chuỗi rỗng, số, boolean, mảng và object. Không biến mã định danh có số 0 đầu thành số.
5. Không ghi secret hoặc dữ liệu nhạy cảm vào ví dụ config. Dùng `.env.example` với placeholder không hoạt động.
6. Parse lại tệp đã ghi và kiểm tra nội dung/kiểu dữ liệu trước khi giao.
