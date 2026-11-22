<div align="center">
  <img src="docs/media/logo.png" alt="Qusin" width="88" />
</div>

# Qusin

**Qusin là trợ lý AI chạy trên Android.** Bạn mô tả việc cần làm bằng ngôn ngữ tự nhiên; Qusin có thể làm việc với ứng dụng trên điện thoại, website, tệp và các tác vụ được lên lịch.

Qusin không cung cấp mô hình AI riêng. Bạn kết nối nhà cung cấp bằng API key của mình, đăng nhập bằng tài khoản được hỗ trợ, hoặc dùng mô hình chạy trên máy tính/mạng nội bộ.

## Tính năng chính

- **Điều khiển ứng dụng Android:** thực hiện thao tác trên màn hình nền riêng, đọc giao diện, chạm, nhập văn bản và cuộn trang. Bạn vẫn có thể tiếp tục sử dụng điện thoại trong khi tác vụ chạy. Một số thiết bị hoặc ứng dụng có giới hạn riêng; đăng nhập, CAPTCHA và bước nhạy cảm có thể yêu cầu bạn tiếp quản.
- **Trình duyệt tích hợp:** mở website trong WebView riêng, giữ trạng thái đăng nhập, thao tác bằng đầu vào thật, đọc nội dung trang và hỗ trợ tải lên/tải xuống.
- **Tác vụ dài và song song:** theo dõi tiến độ, xem các bước đã chạy, gửi bổ sung hoặc dừng tác vụ; nhiều phiên có thể hoạt động đồng thời theo giới hạn của thiết bị và ứng dụng.
- **Lịch và điều kiện:** tạo tác vụ một lần, hằng ngày, hằng tuần hoặc theo điều kiện; xem lịch chạy tiếp theo và lịch sử thực thi.
- **Bộ nhớ và kỹ năng:** lưu thông tin người dùng cho phép, dùng kiến thức về ứng dụng/website và bổ sung kỹ năng dạng văn bản.
- **Không gian làm việc Linux:** mỗi cuộc trò chuyện có workspace riêng để xử lý tệp và chạy lệnh. Runtime tích hợp hiện chỉ hỗ trợ thiết bị ARM64.
- **Kết nối từ máy tính:** giao tác vụ qua CLI/Skill hoặc MCP, theo dõi trạng thái và nhận kết quả trên máy tính.

## Mô hình AI và API key

Qusin hỗ trợ các giao thức OpenAI Chat Completions tương thích, Anthropic Messages, Google Gemini `generateContent` và OpenAI Responses. Các preset bao gồm nhiều dịch vụ cloud, dịch vụ tương thích OpenAI và máy chủ cục bộ như Ollama/LM Studio; bạn cũng có thể khai báo endpoint tùy chỉnh.

Trong **Cài đặt → Mô hình → chọn nhà cung cấp**, nhóm **Khóa API** có nút **Thêm API key** để thêm key. Có thể nhập nhiều key, phân tách bằng dấu phẩy, chấm phẩy hoặc xuống dòng. Qusin xoay vòng giữa các key còn dùng được: lỗi xác thực `401/403` đánh dấu key bị từ chối, còn `429` đưa key vào thời gian chờ theo thông tin giới hạn của máy chủ. Khi tất cả key đều không khả dụng, Qusin không tự quay lại dùng key đã bị loại.

Cơ chế pool áp dụng cho các giao thức API key được hỗ trợ; phiên đăng nhập OAuth như ChatGPT/xAI không phải API key để thêm vào pool. OpenRouter có thể vừa đăng nhập tài khoản vừa quản lý các key dự phòng riêng.

## Yêu cầu

- Android 10 trở lên để cài và sử dụng các chức năng nền tảng.
- Android 12 trở lên và hoàn tất hướng dẫn thiết lập điều khiển điện thoại để Qusin thao tác ứng dụng khác.
- Thiết bị ARM64 để sử dụng Linux sandbox tích hợp.
- Một nhà cung cấp mô hình có hỗ trợ tool/function calling để làm mô hình chính.

## Bắt đầu

1. Cài ứng dụng Qusin trên điện thoại và mở ứng dụng.
2. Vào **Cài đặt → Mô hình**, thêm nhà cung cấp hoặc đăng nhập bằng một tài khoản được hỗ trợ.
3. Chọn mô hình chính có hỗ trợ tool calling. Có thể cấu hình riêng mô hình nhìn ảnh, mô hình nhẹ và mô hình chuyển giọng nói thành văn bản.
4. Để Qusin điều khiển ứng dụng Android, hoàn tất hướng dẫn ghép nối Wireless debugging và quyền Trợ năng trong ứng dụng.
5. Gửi yêu cầu đầu tiên trong cuộc trò chuyện.

Tài liệu chi tiết:

- [Hướng dẫn mô hình và đăng nhập](docs/models.vi.md)
- [Thiết lập điều khiển điện thoại](docs/setup-guide.md)
- [Danh sách tính năng](docs/features.md)
- [Câu hỏi thường gặp](docs/faq.md)
- [Từ máy tính: CLI, Skill và MCP](docs/computer.md)
- [Lộ trình phát triển](docs/dev/roadmap.md)

## Xây dựng từ mã nguồn

Yêu cầu JDK 17 và Android SDK phù hợp với cấu hình Gradle của dự án.

```bash
# chạy unit tests
./gradlew :core:test :app:testDebugUnitTest

# tạo APK debug
./gradlew :app:assembleDebug
```

APK debug được tạo tại:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Quyền riêng tư và giới hạn

API key và thông tin bí mật được mã hóa khi lưu trên thiết bị. Nội dung gửi cho mô hình sẽ được chuyển tới nhà cung cấp mà bạn đã cấu hình; Qusin không thay thế chính sách lưu trữ dữ liệu của nhà cung cấp đó. Tác vụ điều khiển điện thoại dùng quyền Android mà bạn cấp. Một số màn hình bảo mật, nội dung DRM hoặc giao diện chặn chụp màn hình có thể không đọc được; Qusin sẽ cần bạn xử lý trực tiếp ở những bước đó.

Qusin đang trong giai đoạn phát triển. Hãy kiểm tra kết quả trước khi dùng cho thao tác quan trọng, đặc biệt là gửi tin nhắn, xóa dữ liệu, đặt hàng hoặc thanh toán.

## Giấy phép

Xem [LICENSE](LICENSE), [NOTICE](NOTICE) và [thông báo giấy phép bên thứ ba](licenses/THIRD_PARTY_NOTICES.md).
