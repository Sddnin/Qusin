---
name: diagram-files
display: Tạo sơ đồ và biểu đồ kỹ thuật
description: Tạo sơ đồ bằng Mermaid, DOT, SVG và XML draw.io khi phù hợp
aliases: ["tạo sơ đồ", "vẽ flowchart", "tạo Mermaid", "tạo sơ đồ quy trình", "vẽ kiến trúc", "tạo UML"]
terms: ["Mermaid", "DOT", "Graphviz", "SVG", "draw.io", "diagrams.net", "flowchart", "UML", "sequence diagram"]
---
## Quy trình
1. Xác định loại sơ đồ, node, quan hệ, hướng, nhóm và người đọc.
2. Chọn định dạng: Mermaid/DOT cho sơ đồ văn bản, SVG cho hình vector, draw.io XML khi cần mở/chỉnh sửa trong diagrams.net.
3. Sinh đúng cú pháp; kiểm tra tên node, ký tự escape, liên kết và chu trình không mong muốn.
4. Nếu cần PNG/PDF, kiểm tra renderer (Mermaid CLI, Graphviz, browser hoặc công cụ khác) có sẵn; không chỉ đổi đuôi SVG.
5. Giao cả source chỉnh sửa được và ảnh kết xuất nếu người dùng cần. Nêu rõ renderer nào đã chạy.
