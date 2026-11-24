---
name: database-files
display: Tạo và xử lý tệp cơ sở dữ liệu
description: Tạo, nhập, xuất và kiểm tra SQLite cùng dữ liệu dạng bảng
aliases: ["tạo database", "tạo SQLite", "xuất DB", "CSV sang database", "JSON sang SQLite", "đọc file db"]
terms: ["SQLite", "SQL", "DB", "database", "schema", "table", "CSV", "JSON", "query", "sqlite3"]
---
## Quy trình
1. Xác định nguồn dữ liệu, bảng, khóa, kiểu dữ liệu, encoding và các quy tắc duy nhất/null.
2. Dùng SQLite (`sqlite3` của Python) cho DB cục bộ không cần máy chủ. Dùng engine khác chỉ khi có sẵn và người dùng cần.
   Khi nhập CSV vào SQLite, ưu tiên script đi kèm `scripts/csv_to_sqlite.py`, sau đó đối chiếu số dòng và kiểm tra tính toàn vẹn.
3. Tạo schema rõ ràng; dùng parameterized queries cho dữ liệu, không nối chuỗi đầu vào vào SQL.
4. Với CSV/JSON lớn, nhập theo batch và transaction. Báo lỗi dòng/record; không âm thầm bỏ bản ghi.
5. Kiểm tra số bản ghi, cột, khóa, `PRAGMA integrity_check` và truy vấn mẫu.
6. Khi xuất, phân biệt database với dump SQL/CSV/JSON; mỗi loại có mục đích khác nhau.

## An toàn
Không thực thi SQL tùy tiện trong database người dùng. Sao lưu trước thay đổi phá hủy; không DROP/DELETE/UPDATE hàng loạt khi chưa được yêu cầu rõ.
