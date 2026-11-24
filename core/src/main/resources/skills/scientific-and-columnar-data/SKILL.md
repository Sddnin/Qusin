---
name: scientific-and-columnar-data
display: Dữ liệu khoa học và dạng cột
description: Xử lý Parquet, Feather, HDF5 và dữ liệu khoa học
aliases: ["dữ liệu khoa học và dạng cột", "tạo dữ liệu khoa học và dạng cột", "xử lý dữ liệu khoa học và dạng cột"]
terms: ["PARQUET", "FEATHER", "ARROW", "HDF5", "H5", "NETCDF", "NC", "MAT", "NPY", "NPZ"]
---
## Quy trình
1. Xác định schema, kiểu dữ liệu, đơn vị, chiều dữ liệu, missing-value convention và metadata.
2. Kiểm tra thư viện trước khi thao tác: pyarrow/pandas cho Parquet/Feather, h5py cho HDF5, netCDF4/xarray cho NetCDF, scipy cho một số MAT.
3. Không chuyển dữ liệu số thành chuỗi; giữ độ chính xác, timezone và đơn vị.
4. Với dữ liệu nhiều chiều, ghi rõ tên chiều, tọa độ và thứ tự trục.
5. Sau khi ghi, mở lại và so sánh schema, shape, số dòng, metadata và một số giá trị kiểm tra.
6. Nếu thiếu thư viện, đề xuất CSV/JSON/NPZ hoặc định dạng trao đổi khác chỉ khi không làm mất cấu trúc quan trọng.

Không tự suy diễn đơn vị đo, ý nghĩa cột hoặc cách xử lý missing values.
