---
name: subtitles-transcripts
display: Phụ đề và bản chép lời
description: Tạo, chỉnh sửa và kiểm tra phụ đề SRT/VTT và bản chép lời
aliases: ["tạo phụ đề", "làm subtitle", "chuyển lời nói thành văn bản", "tạo SRT", "tạo VTT", "đồng bộ phụ đề"]
terms: ["SRT", "VTT", "subtitle", "phụ đề", "transcript", "timestamp", "timecode", "lời thoại", "người nói"]
---
## Mục tiêu
Tạo phụ đề dễ đọc, đúng thứ tự và có mốc thời gian hợp lệ; giữ nguyên ý nghĩa lời nói.

## Quy trình
1. Xác định nguồn là âm thanh/video hay bản chép lời có sẵn. Kiểm tra công cụ nhận dạng giọng nói và khả năng xử lý media trước khi hứa tạo transcript.
2. Nếu chỉ có văn bản, không tự bịa mốc thời gian. Hỏi người dùng cung cấp media hoặc chấp nhận bản chép lời không có timecode.
3. Tạo SRT hoặc WebVTT đúng cú pháp; mỗi cue có thời điểm bắt đầu/kết thúc hợp lệ, không âm thời lượng và theo đúng thứ tự.
4. Chia câu theo cụm nghĩa, tránh mỗi dòng quá dài. Giữ tên riêng, thuật ngữ và ngôn ngữ gốc; đánh dấu đoạn không nghe rõ thay vì đoán.
5. Kiểm tra encoding UTF-8, dấu tiếng Việt, timecode, cue trùng/lấn nhau và khả năng mở bằng trình phát phụ đề.
6. Giao kèm transcript TXT/MD nếu hữu ích; nêu rõ phần nào được nhận dạng tự động và phần nào đã được rà soát.

## Quy tắc
- Không khẳng định độ chính xác tuyệt đối của nhận dạng giọng nói.
- Không tự thêm lời thoại, người nói hoặc cảm xúc không có trong nguồn.
- Giữ nguyên nội dung nhạy cảm cần thiết cho nhiệm vụ nhưng không đưa chúng vào tên tệp hoặc log.
