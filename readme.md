# Lucky Draw

## Mô tả

Ứng dụng Lucky Draw là một chương trình bốc thăm may mắn được thiết kế để tổ chức các sự kiện, chương trình khuyến mãi hoặc các hoạt động nội bộ của công ty. Ứng dụng cho phép người dùng tạo danh sách người tham gia, thực hiện bốc thăm ngẫu nhiên và hiển thị kết quả một cách rõ ràng. Đặc biệt, ứng dụng hỗ trợ cấu hình tỉ lệ trúng thưởng khác nhau cho từng loại quà, xử lý tình huống hết quà, tăng xác suất trúng thưởng trong giờ vàng và giới hạn số lượng quà theo thời gian.

## Tính năng

* **Tạo danh sách người tham gia**: Nhập danh sách người tham gia bằng tay hoặc tải lên từ tệp.
* **Bốc thăm ngẫu nhiên**: Chọn ngẫu nhiên một hoặc nhiều người từ danh sách.
* **Hiển thị kết quả**: Hiển thị kết quả bốc thăm rõ ràng trên màn hình.
* **Tùy chỉnh số lượng người trúng thưởng**: Cho phép người dùng tùy chỉnh số lượng người được chọn trong mỗi lần bốc thăm.
* **Chia giải thưởng**: Bổ sung tính năng chia giải thưởng khác nhau cho những người trúng thăm.
* **Lưu lịch sử bốc thăm**: Lưu lại lịch sử các lần bốc thăm trước đó.
* **Xác thực người dùng**: Giới hạn quyền truy cập vào chương trình.
* **Bảo mật dữ liệu**: Đảm bảo dữ liệu người dùng được lưu trữ và xử lý an toàn.
* **Tỉ lệ trúng thưởng khác nhau**: Mỗi phần thưởng có xác suất trúng thưởng riêng, được cấu hình trong file cấu hình.
* **Xử lý quà hết**: Nếu quay trúng phần thưởng nhưng số lượng quà đã hết, khách hàng sẽ không trúng thưởng.
* **Giờ vàng**: Trong khung giờ vàng, xác suất trúng thưởng của các phần thưởng sẽ được tăng thêm một tỉ lệ nhất định.
* **Giới hạn số lượng quà**: Mỗi phần thưởng có giới hạn số lượng phát trong một khoảng thời gian cụ thể.
* **Báo cáo về số lượng người trúng thưởng, số phần thưởng còn lại**: Trang báo cáo thể hiện danh sách các lần quay thưởng. người sử dụng có thể lọc được các lần quay trúng thưởng. Trang cũng có báo cáo về số phàn quà còn lại. Trang cũng có thống kê số lượt quay còn lại của khách hàng.

## Công nghệ sử dụng

* **Backend**: Java 11+, Spring, Hibernate, MySQL
* **Frontend**: HTML, CSS, JavaScript

## Cấu hình & Lưu trữ

* **File cấu hình**: Chứa thông tin về phần thưởng và giờ vàng, có thể upload qua trang quản trị.
    * **Bảng cấu hình phần thưởng**: Tên phần thưởng, tổng số lượng, xác suất (%), xác suất giờ vàng (%), ngày bắt đầu, ngày kết thúc, giới hạn từ ngày, giới hạn đến ngày, số lượng tối đa trong giai đoạn.
    * **Bảng cấu hình giờ vàng**: Ngày bắt đầu, ngày kết thúc, giờ vàng sáng, giờ vàng tối.
* **Lưu trữ**: File cấu hình được lưu vào database để dễ dàng truy xuất và cập nhật. Theo dõi số lượng quà đã phát để tạo báo cáo.

## Giao diện web

* **Trang quản trị (Admin)**:
    * Upload file cấu hình: Cho phép admin upload file cấu hình phần thưởng và giờ vàng.
    * Theo dõi số lượng quà còn lại: Hiển thị số lượng quà đã phát và còn lại cho từng phần thưởng.
* **Trang khách hàng**:
    * Nút quay thưởng: Khách hàng nhấn nút để quay thưởng.
    * Hiển thị kết quả quay thưởng (trúng hoặc không trúng).

## Cài đặt

1. Clone repository: `git clone https://github.com/your-username/lucky-draw.git`
2. Import project vào IDE (ví dụ: IntelliJ IDEA, Eclipse)
3. Cấu hình database connection trong file `application.properties`
4. Build project: `mvn clean install`
5. Chạy ứng dụng: `java -jar lucky-draw.jar`

## Hướng dẫn sử dụng

1. Truy cập trang web của ứng dụng.
2. Đăng nhập (nếu có).
3. Tạo danh sách người tham gia hoặc tải lên từ tệp.
4. Chọn số lượng người trúng thưởng (nếu có).
5. Nhấn nút "Bốc thăm".
6. Xem kết quả bốc thăm.

## Ví dụ cấu hình

* **Bảng cấu hình phần thưởng**:

| Tên phần thưởng | Tổng số lượng | Xác suất (%) | Giờ vàng (%) | Ngày bắt đầu | Ngày kết thúc | Giới hạn từ ngày | Giới hạn đến ngày | Số lượng tối đa |
|---|---|---|---|---|---|---|---|---|
| Xe Máy Viaro | 1 | 20.0001% | +50% | 01-02-2024 | 31-04-2024 | 01-02-2024 | 31-04-2024 | 1 xe mỗi tuần |
| Voucher 20K | 5000 | 25% | +20% | 01-02-2024 | 31-04-2024 | 01-02-2024 | 31-04-2024 | Không giới hạn |
| Voucher 50K | 2000 | 10% | +15% | 15-02-2024 | 30-04-2024 | 15-02-2024 | 15-03-2024 | 500 cái |
| Voucher 100K | 500 | 3% | +10% | 10-03-2024 | 25-04-2024 | 10-03-2024 | 25-03-2024 | 100 cái |
| Voucher 200K | 200 | 1% | +10% | 01-04-2024 | 20-04-2024 | 01-04-2024 | 10-04-2024 | 10 cái mỗi ngày |

* **Bảng cấu hình giờ vàng**:

Tài liệu này mô tả cách cấu hình giờ vàng cho chương trình quay thưởng. Giờ vàng cho phép tăng xác suất trúng thưởng cho một phần thưởng cụ thể trong một khoảng thời gian xác định.

## Định dạng file CSV

Cấu hình giờ vàng được lưu trữ trong file CSV với định dạng sau:

Tên cột        | Kiểu dữ liệu | Mô tả
----------------|--------------|-----------------------------------------------------------------------------
rewardName     | String       | Tên của phần thưởng áp dụng giờ vàng.
startTime      | DateTime     | Thời gian bắt đầu giờ vàng (yyyy-MM-dd HH:mm).
endTime        | DateTime     | Thời gian kết thúc giờ vàng (yyyy-MM-dd HH:mm).


**Ví dụ:**

rewardName|startTime| endTime
----------------|--------------|-----------------------------------------------------------------------------
Voucher 20K|2024-07-26 12:00|2024-07-26 14:00|
|Voucher 50K|2024-07-27 18:00|2024-07-27 20:00|
|Voucher 20K|2024-07-28 08:00|2024-07-28 10:00|
**Giải thích:**

* **rewardName:** Tên phần thưởng được cấu hình giờ vàng. Phải khớp với `name` trong file cấu hình phần thưởng.
* **startTime:** Thời gian bắt đầu áp dụng giờ vàng. Định dạng `yyyy-MM-dd HH:mm`.
* **endTime:** Thời gian kết thúc áp dụng giờ vàng. Định dạng `yyyy-MM-dd HH:mm`.


**Lưu ý:**

* Mỗi dòng trong file CSV đại diện cho một khung giờ vàng.
* Có thể có nhiều khung giờ vàng cho cùng một phần thưởng.
* Khoảng thời gian của giờ vàng có thể chồng lấp hoặc không chồng lấp.
* Nếu thời gian hiện tại nằm trong bất kỳ khung giờ vàng nào được cấu hình cho phần thưởng, xác suất trúng thưởng sẽ được tăng thêm giá trị `goldenHourProbability` được định nghĩa trong file cấu hình phần thưởng.

## Ví dụ sử dụng

Ví dụ trên cấu hình giờ vàng cho "Voucher 20K" từ 12:00 đến 14:00 ngày 26/07/2024 và "Voucher 50K" từ 18:00 đến 20:00 ngày 27/07/2024. Ngoài ra còn có giờ vàng cho "Voucher 20K" từ 08:00 đến 10:00 ngày 28/07/2024.  Điều này minh họa việc có thể có nhiều giờ vàng cho cùng một phần thưởng vào các ngày khác nhau.
## Lưu ý khi triển khai

* **Xử lý đồng thời**: Đảm bảo xử lý đồng thời khi nhiều người dùng quay thưởng cùng lúc.
* **Bảo mật**: Kiểm tra và xác thực file cấu hình trước khi upload để tránh lỗi hoặc tấn công.
* **Logging**: Ghi lại log các hoạt động quay thưởng để dễ dàng kiểm tra và báo cáo.

## Đóng góp

Mọi đóng góp đều được hoan nghênh. Vui lòng tạo pull request để đóng góp cho dự án.

## Liên hệ

* Tác giả: Nguyễn Minh Đức
  * Email: dnguyenminh@gmail.com

## Giấy phép

[License]