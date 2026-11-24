---
name: calendar-contact-files
display: Tạo lịch và danh bạ
description: Tạo tệp lịch ICS và danh bạ VCF có thể nhập vào ứng dụng
aliases: ["tạo ICS", "tạo lịch", "xuất lịch", "tạo VCF", "xuất danh bạ", "tạo contact"]
terms: ["ICS", "iCalendar", "VCF", "vCard", "VEVENT", "VALARM", "timezone", "contact"]
---
## Quy trình
1. Xác định múi giờ, ngày giờ, thời lượng, địa điểm, nhắc lịch và thông tin liên hệ được phép đưa vào tệp.
2. Với ICS, dùng `VCALENDAR` và `VEVENT` hợp lệ; với VCF, dùng phiên bản vCard phù hợp ứng dụng đích.
3. Escape dấu phẩy, chấm phẩy, dấu gạch chéo ngược; gấp dòng theo quy tắc chuẩn và dùng CRLF khi cần tương thích.
4. Không tự suy ra timezone, email, số điện thoại hoặc thông tin người liên hệ còn thiếu.
5. Kiểm tra BEGIN/END, UID, DTSTART/DTEND, VERSION và ký tự tiếng Việt. Nếu có ứng dụng kiểm tra, thử import bản mẫu.
6. Bảo vệ dữ liệu liên hệ; chỉ xuất những trường người dùng yêu cầu.
