# mô hình và đăng nhập

Qusin không kèm mô hình AI riêng. bạn kết nối dịch vụ bằng API key, đăng nhập bằng tài khoản được hỗ trợ hoặc dùng máy chủ mô hình trong mạng nội bộ. mô hình chính cần hỗ trợ tool/function calling để thực hiện tác vụ.

## thêm nhà cung cấp

mở **cài đặt → mô hình**, chọn một preset hoặc **tùy chỉnh**. giao thức được hỗ trợ gồm OpenAI-compatible Chat Completions, OpenAI Responses, Anthropic Messages và Google Gemini `generateContent`. với máy chủ như Ollama hoặc LM Studio, nhập địa chỉ của máy tính; API key có thể để trống nếu máy chủ không yêu cầu.

nhấn **kiểm tra và lưu** để kiểm tra kết nối và lấy danh sách mô hình. nếu nhà cung cấp không có danh sách hoặc thiếu model cần dùng, nhập model ID theo tài liệu của nhà cung cấp. có thể lưu mà không kiểm tra, nhưng mô hình chưa được xác minh sẽ không tự trở thành mô hình chính.

## quản lý nhiều API key

trong trang chi tiết của nhà cung cấp hỗ trợ API key, nhấn **thêm API key** để mở ô nhập. có thể dán một key hoặc nhiều key, phân tách bằng dấu phẩy, dấu chấm phẩy hoặc xuống dòng. tính năng áp dụng cho cả bốn giao thức API key ở trên. thông tin đăng nhập OAuth được quản lý riêng; OpenRouter có thể giữ key tài khoản và các key dự phòng mà bạn tự thêm.

Qusin chọn luân phiên giữa các key đang khả dụng. khi máy chủ trả `401` hoặc `403`, key đó được đánh dấu không còn dùng được và không được chọn lại cho đến khi ứng dụng khởi động lại hoặc bạn xóa rồi thêm lại chính key đó. khi trả `429`, key vào thời gian chờ; Qusin ưu tiên `Retry-After` từ máy chủ, nếu không có thì mặc định 60 giây. request hiện tại có thể thử key tiếp theo sau các lỗi này, mỗi key tối đa một lần cho một request logic. nếu không còn key khác khả dụng, lỗi ban đầu được trả về. lỗi mạng và các mã HTTP khác không kích hoạt xoay key.

trạng thái key là trạng thái chạy trong bộ nhớ, không có nghĩa nhà cung cấp gộp hạn mức giữa các tài khoản. từng key vẫn tuân theo giới hạn, cách tính phí và quyền truy cập của nhà cung cấp tương ứng.

## tài khoản đăng nhập

một số dịch vụ hỗ trợ đăng nhập mà không cần nhập API key. ChatGPT dùng hạn mức gói ChatGPT được hỗ trợ; xAI/Grok yêu cầu gói đăng ký phù hợp và có thể vẫn bị từ chối API tùy cấp gói; OpenRouter dùng key do OpenRouter cấp sau khi bạn chấp thuận quyền truy cập. đăng xuất chỉ xóa thông tin đăng nhập tương ứng trên điện thoại. với OpenRouter, key dự phòng do bạn tự thêm vẫn được giữ lại.

## vai trò mô hình

**mô hình chính** lập kế hoạch và gọi công cụ, vì vậy cần hỗ trợ tool calling. **mô hình nhìn ảnh** có thể mô tả ảnh chụp màn hình; nếu mô hình chính đã nhìn ảnh được, có thể dùng chung. **mô hình nhẹ** dành cho các bước nhỏ như phân loại kết quả và trích xuất thông tin. ngoài ra có thể cấu hình mô hình chuyển giọng nói thành văn bản và mô hình tạo giọng nói/ảnh trong các phần cài đặt tương ứng.

## giới hạn và chi phí

API key được lưu mã hóa trên điện thoại. nội dung gửi đến mô hình được chuyển cho nhà cung cấp mà bạn cấu hình và tuân theo chính sách dữ liệu của họ. một số endpoint tự host hoặc trong mạng nội bộ có thể dùng HTTP; với dịch vụ bên ngoài, ưu tiên HTTPS. kiểm tra giá và hạn mức trực tiếp ở nhà cung cấp trước khi dùng thường xuyên.
