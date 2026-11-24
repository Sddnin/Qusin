---
name: media-files
display: Tạo và chuyển đổi âm thanh video
description: Xử lý âm thanh và video khi có công cụ phù hợp
aliases: ["tạo video", "cắt video", "ghép video", "chuyển MP4", "đổi MP3", "chuyển đổi âm thanh", "tạo WAV", "nén video"]
terms: ["audio", "video", "MP3", "WAV", "FLAC", "M4A", "MP4", "MOV", "WEBM", "MKV", "FFmpeg", "codec", "bitrate"]
---
## Quy trình
1. Xác định nguồn, định dạng đích, độ phân giải, frame rate, codec, bitrate, thời lượng và chất lượng mong muốn.
2. Kiểm tra `ffmpeg`/`ffprobe` hoặc thư viện media thực sự có sẵn trước khi hứa xử lý.
3. Đọc metadata trước: codec, thời lượng, kích thước khung hình, sample rate, số kênh và rotation.
4. Dùng cấu hình phù hợp; không đổi container khi codec bên trong không tương thích. Giữ nguồn gốc, tạo tệp đầu ra mới.
5. Sau khi xử lý, dùng ffprobe hoặc player kiểm tra thời lượng, codec, âm thanh, hình ảnh và đồng bộ A/V.
6. Nêu rõ có re-encode hay chỉ remux, vì re-encode có thể giảm chất lượng.

## Giới hạn
Không giả lập MP4/MP3 bằng cách đổi đuôi. Không tải nhạc/video có bản quyền hoặc vượt qua DRM. Không khẳng định xử lý media thành công nếu chưa có công cụ và chưa phát/đọc thử đầu ra.
