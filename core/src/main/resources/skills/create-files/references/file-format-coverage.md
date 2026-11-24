# Qusin Skill — Bản đồ định dạng tệp

Tài liệu tra cứu nhanh để chọn skill phù hợp. Khả năng thực thi luôn phụ thuộc thư viện/công cụ thật sự có trong môi trường.

| Nhóm | Định dạng | Skill chính |
|---|---|---|
| Văn bản/văn phòng | TXT, MD, DOCX, ODT, RTF, TEX, PDF | word-documents, opendocument-rtf, documents |
| Bảng tính/trình chiếu | XLSX, CSV, TSV, ODS, PPTX, ODP | spreadsheets, opendocument-rtf, presentations |
| Dữ liệu/cơ sở dữ liệu | JSON, YAML, XML, TOML, INI, SQL, SQLite, Parquet, Feather, HDF5, NetCDF, NPY, NPZ | structured-data-files, database-files, scientific-and-columnar-data |
| Web/sơ đồ | HTML, CSS, JS, SVG, Mermaid, DOT, draw.io XML | web-files, diagram-files |
| Hình ảnh/media | PNG, JPEG, WebP, GIF, TIFF, BMP, WAV, MP3, FLAC, MP4, MOV, WEBM, MKV | image-files, media-files |
| Nén | ZIP, TAR, TGZ, TAR.GZ, TAR.BZ2, TAR.XZ, GZ, 7Z, RAR | archive-management |
| Ebook/phụ đề | EPUB, SRT, VTT | ebook-files, subtitles-transcripts |
| Lịch/email/danh bạ | ICS, VCF, EML | calendar-contact-files, email-files |
| Kỹ thuật | OBJ, MTL, STL, glTF, DXF, DWG, STEP, IGES | 3d-model-files, cad-technical-drawings |
| Địa lý | GeoJSON, KML, GPX, SHP, GPKG, WKT | geospatial-files |
| Font/ngôn ngữ | TTF, OTF, WOFF, WOFF2, PO, POT, XLIFF, RESX, ARB | font-files, localization-translation-files |
| Biểu mẫu/log | PDF form, DOCX form, HTML form, LOG, OUT, DIFF, PATCH | forms-and-templates, logs-and-technical-text |

## Các định dạng cần lưu ý
- 7Z, RAR, DWG, STEP, IGES, PSD, AI, MOBI/KPF và định dạng độc quyền cần phần mềm chuyên dụng. Không đổi đuôi để giả lập.
- Âm thanh/video cần codec và công cụ như FFmpeg; OCR cần engine/ngôn ngữ tương ứng.
- PDF có thể là văn bản hoặc ảnh scan; PDF scan cần OCR. PDF phẳng không đồng nghĩa PDF có form tương tác.
- Một số định dạng là gói nhiều tệp (ví dụ shapefile); phải giữ đủ thành phần phụ.
