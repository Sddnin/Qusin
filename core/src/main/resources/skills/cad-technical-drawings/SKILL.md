---
name: cad-technical-drawings
display: Bản vẽ CAD kỹ thuật
description: Tạo và kiểm tra bản vẽ kỹ thuật CAD
aliases: ["bản vẽ cad kỹ thuật", "tạo bản vẽ cad kỹ thuật", "xử lý bản vẽ cad kỹ thuật"]
terms: ["DXF", "DWG", "STEP", "STP", "IGES", "IGS", "SVG"]
---
## Phạm vi và giới hạn
- DXF có thể được tạo/đọc bằng thư viện như ezdxf nếu đã cài.
- DWG, STEP/STP, IGES/IGS thường cần phần mềm CAD hoặc thư viện chuyên dụng; không giả lập bằng văn bản đổi đuôi.
- SVG phù hợp bản vẽ vector 2D đơn giản, không thay thế đầy đủ CAD tham số.

## Quy trình
1. Hỏi đơn vị, tỷ lệ, mặt phẳng/hệ trục, layer, kích thước và phiên bản định dạng.
2. Kiểm tra công cụ hỗ trợ trước khi hứa tạo DWG/STEP.
3. Tạo layer, entity, dimension và block có tên rõ; tránh hình học tự giao hoặc sai đơn vị.
4. Mở lại bằng parser/CAD viewer; kiểm tra kích thước và tọa độ mẫu.
5. Giao kèm PDF/SVG preview nếu có thể và nói rõ đây là bản trao đổi hay bản CAD chỉnh sửa tham số.

Không tự đưa ra thông số kỹ thuật chịu lực/an toàn nếu người dùng chưa cung cấp và chưa có chuyên gia xác nhận.
