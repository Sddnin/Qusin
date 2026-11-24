---
name: geospatial-files
display: Tệp dữ liệu địa lý
description: Đọc, tạo và kiểm tra dữ liệu không gian địa lý
aliases: ["tệp dữ liệu địa lý", "tạo tệp dữ liệu địa lý", "xử lý tệp dữ liệu địa lý"]
terms: ["GEOJSON", "KML", "GPX", "SHP", "GPKG", "WKT", "WKB"]
---
## Quy trình
1. Xác định hệ tọa độ (CRS/EPSG), đơn vị, thứ tự trục và phạm vi địa lý; không mặc định mọi tọa độ là WGS84.
2. Ưu tiên GeoJSON cho dữ liệu vector trao đổi đơn giản; KML cho hệ sinh thái bản đồ tương thích; GPX cho track/route/waypoint; GeoPackage cho gói dữ liệu GIS.
3. Shapefile là bộ nhiều tệp tối thiểu gồm SHP, SHX, DBF và thường có PRJ; phải đóng gói tất cả thành phần liên quan.
4. Dùng GDAL/OGR, Fiona, GeoPandas hoặc parser phù hợp nếu có; kiểm tra hình học hợp lệ và thuộc tính.
5. Không âm thầm đổi CRS hoặc hoán đổi latitude/longitude. Khi chuyển đổi, ghi CRS nguồn và đích.
6. Kiểm tra mở lại, số feature, bounding box, encoding và geometry.

Không suy diễn vị trí chính xác từ dữ liệu thiếu CRS; nêu rõ mọi giả định.
