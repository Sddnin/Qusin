---
name: 3d-model-files
display: Tạo tệp mô hình 3D cơ bản
description: Tạo và kiểm tra mô hình 3D OBJ, STL và glTF
aliases: ["tạo mô hình 3D", "xuất OBJ", "tạo STL", "tạo glTF", "file 3D", "mô hình in 3D"]
terms: ["OBJ", "MTL", "STL", "GLTF", "GLB", "mesh", "vertex", "face", "normal", "3D"]
---
## Quy trình
1. Xác định đối tượng, kích thước, đơn vị, hệ trục, mức chi tiết và mục đích (xem, game, CAD hay in 3D).
2. Chọn OBJ/MTL cho mesh văn bản, STL cho in 3D đơn giản, glTF cho scene có material; GLB cần đóng gói binary đúng chuẩn.
3. Tạo mesh với đỉnh/mặt hợp lệ, winding nhất quán và normals phù hợp. STL cần mặt tam giác và đơn vị được ghi rõ.
4. Kiểm tra file parse được, số vertex/face, bounding box, mesh có mặt lỗi/non-manifold nếu có validator.
5. Nếu cần render, kiểm tra Blender/renderer có sẵn. Không tuyên bố mô hình đã được kiểm tra vật lý hoặc in thử nếu chưa.
6. Không tự suy ra kích thước kỹ thuật quan trọng; hỏi hoặc ghi rõ giả định.
