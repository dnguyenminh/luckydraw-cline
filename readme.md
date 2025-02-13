# Lucky Draw

## Mô tả

Ứng dụng Lucky Draw là một chương trình bốc thăm may mắn được thiết kế để tổ chức các sự kiện, chương trình khuyến mãi hoặc các hoạt động nội bộ của công ty. Ứng dụng cho phép người dùng tạo danh sách người tham gia, thực hiện bốc thăm ngẫu nhiên và hiển thị kết quả một cách rõ ràng. Đặc biệt, ứng dụng hỗ trợ cấu hình tỉ lệ trúng thưởng khác nhau cho từng loại quà, xử lý tình huống hết quà, tăng xác suất trúng thưởng trong giờ vàng và giới hạn số lượng quà theo thời gian.

## Tính năng

* **Quản lý người tham gia:**
  * Quản lý sự kiện, có ngày bắt đầu và kết thúc 
  * Hệ thống tự động trong 1 số khung giờ hằng ngày đọc từ 1 table ADHOC_CONTEST_TET2025 để tạo hoăc cập nhật ngưởi tham gia vào sự kiện
    ```CREATE TABLE ADHOC_CONTEST_TET2025 (
      AGREEMENTNO VARCHAR2(20) PRIMARY KEY,
      ACCOUNT_NO NUMBER,
      CIFID VARCHAR2(64),
      AGREEMENTNO_U VARCHAR2(64),
      MAX_SPIN NUMBER,
      EVENT VARCHAR2(255),
      EXPORT_TIME DATETIME
    );
    ```
  * Đảm bảo không import lại data đã import lần trước
* **Bốc thăm:**
  * Bốc thăm ngẫu nhiên cho một hoặc nhiều người.
  * Tùy chỉnh số lượng người trúng thưởng cho mỗi lần bốc thăm.
  * Khách hàng tự quay thưởng sau khi đăng nhập.
  * Khách hàng có thể gộp nhiều lượt quay để hệ thống tụ động quay.
  * Hệ thống tự động xác định vị trí khách hàng để áp dụng cấu hình phần quà theo khu vực.
* **Quản lý phần thưởng:**
  * Quản lý số lượng tổng số lần quay.
  * Nhập thêm cấu hình phần thưởng trong khi sụ kiên xãy ra. 
  * Mỗi phần thưởng sẽ có tỉ lệ trúng dựa vào tổng số quà trên tổng số lượt quay còn lại.  
    Ví dụ:
 
    | Quà | Số Lượng |
    |---|---|
    |Phần quà 1| 4|
    |Phần quà 2| 10|
    
    Tổng số lần quay là 100.
    Phần quà 2 sẽ đặt ngẫu nhiên những vị trí từ 0-99   
    Phần quà 1 sẽ đặt ngẫu nhiên những vị trí từ 0-99 và không trùng với vị trí của phần quà 2   
    lần đầu quay trúng hay không trúng thì lần quay kế tiếp sẽ:
    Phần quà 2 sẽ đặt ngẫu nhiên những vị trí từ 0-98   
    Phần quà 1 sẽ đặt ngẫu nhiên những vị trí từ 0-98 và không trùng vói vị trí của phần quà 2
    
    Công thức tổng quát:
    Với mọi i=[0, tổng số lượt quay) 
    Đặt vị trí cho phần quà thú k = [0, tổng số phần quà) = [0, tổng số lượt quay - tồng số lượt đã quay) và không trùng với những vị trí đã đặt trước cho các phần quà (t < k)  
  * Xử lý trường hợp quà hết.
  * Giờ vàng: Tăng xác suất trúng thưởng trong khung giờ cụ thể (giảm số Tổng số lượng quay).
  * Giới hạn số lượng quà theo thời gian và khu vực địa lý.
  * Mỗi lần quay thưởng của khách hàng thì tổng số lần quay phải giảm xuống 1 (chú ý giải quyết nhiều request cùng lúc)
  
  **[Tại liệu chi tiết](DistributedGiftSolution.md)**  

* **Quản lý chương trình:**
  * Lưu lịch sử bốc thăm.
  * Xác thực người dùng để giới hạn quyền truy cập.
  * Bảo mật dữ liệu người dùng.
* **Báo cáo:**
  * Báo cáo số lượng người trúng thưởng và số phần thưởng còn lại.
  * Danh sách các lần quay thưởng có thể lọc theo tiêu chí.
  * Thống kê số lượt quay còn lại của mỗi khách hàng.
  

## Công nghệ sử dụng

* **Backend**: Java 11+, Spring Boot, Spring Data JPA, Spring Security, SpringDoc OpenAPI
* **Frontend**: HTML, CSS, JavaScript, Thymeleaf
* **Database**: PostgreSQL (production), H2 (development/test)
* **Build tool**: Gradle

## Cấu hình & Lưu trữ

Ứng dụng sử dụng ba file CSV để cấu hình phần thưởng, giờ vàng, và người tham gia, có thể được upload thông qua giao diện quản trị. Dữ liệu từ file CSV sẽ được lưu trữ vào cơ sở dữ liệu để tiện truy xuất và quản lý.

* **File cấu hình phần thưởng (`rewards.csv`):**

| Tên cột | Kiểu dữ liệu | Mô tả |
|---|---|---|
| `eventName` | String | Tên sự kiện (bắt buộc). |
| `name` | String | Tên phần thưởng (bắt buộc). |
| `totalQuantity` | Integer | Tổng số lượng phần thưởng (bắt buộc). |
| `probability` | Double | Xác suất trúng thưởng (số thập phân từ 0 đến 1, bắt buộc). Ví dụ: 0.25 tương đương 25%. |
| `startDate` | Date (yyyy-MM-dd) | Ngày bắt đầu chương trình (bắt buộc). |
| `endDate` | Date (yyyy-MM-dd) | Ngày kết thúc chương trình (bắt buộc). |
| `limitFromDate` | Date (yyyy-MM-dd) | Ngày bắt đầu áp dụng giới hạn số lượng (nếu có). |
| `limitToDate` | Date (yyyy-MM-dd) | Ngày kết thúc áp dụng giới hạn số lượng (nếu có). |
| `maxQuantityInPeriod` | Integer | Số lượng tối đa được phát trong giai đoạn `limitFromDate` đến `limitToDate` (nếu có). |
| `applicableProvinces` | String | Danh sách các tỉnh/thành áp dụng, phân cách bởi dấu phẩy. Ví dụ: "Hà Nội,Hồ Chí Minh,Đà Nẵng" |


* **File cấu hình giờ vàng (`golden_hours.csv`):**

| Tên cột | Kiểu dữ liệu | Mô tả |
|---|---|---|
| `eventName` | String | Tên sự kiện (bắt buộc). |
| `rewardName` | String | Tên phần thưởng áp dụng giờ vàng (bắt buộc, phải trùng với `name` trong `rewards.csv`). |
| `startTime` | DateTime (yyyy-MM-dd HH:mm) | Thời gian bắt đầu giờ vàng (bắt buộc). |
| `endTime` | DateTime (yyyy-MM-dd HH:mm) | Thời gian kết thúc giờ vàng (bắt buộc). |
| `bonusProbability` | Double | Tỉ lệ phần trăm cộng thêm vào xác suất trúng thưởng trong giờ vàng (ví dụ: 0.1 cho 10%). |

* **File cấu hình người tham gia (`participants.csv`):**

| Tên cột | Kiểu dữ liệu | Mô tả |
|---|---|---|
| `eventName` | String | Tên sự kiện (bắt buộc). |
| `contractNumber` | String | Số hợp đồng/tài khoản (bắt buộc). |
| `numberOfTurns` | Integer | Số lượt quay của người tham gia (bắt buộc). |


## Ví dụ cấu hình

* **`rewards.csv`:**

| eventName | name | totalQuantity | probability | startDate | endDate | limitFromDate | limitToDate | maxQuantityInPeriod | applicableProvinces |
|---|---|---|---|---|---|---|---|---|---|
| Event_A | Xe Máy Viaro | 1 | 0.0000200001 | 2024-02-01 | 2024-04-31 | 2024-02-01 | 2024-04-31 | 1 | Hà Nội,Hồ Chí Minh |
| Event_A | Voucher 20K | 5000 | 0.25 | 2024-02-01 | 2024-04-31 | 2024-02-01 | 2024-04-31 |  | Toàn quốc |
| ... | ... | ... | ... | ... | ... | ... | ... | ... | ... |


* **`golden_hours.csv`:**

| eventName | rewardName | startTime | endTime | bonusProbability |
|---|---|---|---|---|
| Event_A | Voucher 20K | 2024-07-26 12:00 | 2024-07-26 14:00 | 0.1 |
| Event_A | Voucher 50K | 2024-07-27 18:00 | 2024-07-27 20:00 | 0.15 |
| ... | ... | ... | ... | ... |

* **`participants.csv`:**

| eventName | contractNumber | numberOfTurns |
|---|---|---|
| Event_A | HD12345 | 2 |
| Event_A | TK98765 | 5 |
| ... | ... | ... |

## Giao diện web

Ứng dụng cung cấp hai giao diện chính: giao diện quản trị (Admin) và giao diện khách hàng.

* **Trang quản trị (Admin):**

  * **Quản lý sự kiện:**
    * Tạo mới sự kiện: Nhập tên, thời gian bắt đầu và kết thúc.
    * Xem/Chỉnh sửa/Xóa sự kiện.
    * Export danh sách sự kiện ra file CSV.
  * **Quản lý phần thưởng:**
    * Tạo/Chỉnh sửa/Xóa phần thưởng:  Nhập tên, số lượng, xác suất, v.v.  và upload hình ảnh minh họa (nếu có).
    * Upload/Export file cấu hình `rewards.csv`.
  * **Quản lý giờ vàng:**
    * Tạo/Chỉnh sửa/Xóa giờ vàng:  Chọn sự kiện, phần thưởng, thời gian bắt đầu, thời gian kết thúc, và tỉ lệ bonus.
    * Upload/Export file cấu hình `golden_hours.csv`.
  * **Quản lý người tham gia:**
    * Thêm người tham gia bằng tay: Nhập số hợp đồng/tài khoản và số lượt quay.
    * Upload/Export file cấu hình `participants.csv`.
    * Xem/Chỉnh sửa/Xóa người tham gia.
  * **Bốc thăm:**
    * Chọn sự kiện để bốc thăm.
    * Chọn số lượng người trúng thưởng.
    * Thực hiện bốc thăm.
    * Xem/Export kết quả bốc thăm ra file CSV/Excel.
  * **Báo cáo:**
    * Xem báo cáo số lượng người trúng thưởng và số phần thưởng còn lại theo sự kiện.
    * Lọc báo cáo theo các tiêu chí khác nhau (ví dụ: thời gian, phần thưởng).
    * Xem/Export lịch sử bốc thăm ra file CSV/Excel.
    * Xem thống kê số lượt quay còn lại của từng khách hàng.


* **Trang khách hàng:**

  * **Đăng nhập:** Khách hàng đăng nhập bằng tài khoản được cung cấp.
  * **Quay thưởng:**
    * Hiển thị số lượt quay còn lại.
    * Chọn số lượt quay muốn sử dụng (tối đa bằng số lượt quay còn lại).
    * Nhấn nút "Quay thưởng".
    * Hiển thị kết quả quay thưởng (trúng thưởng, không trúng thưởng, hoặc quà đã hết).
    * Xem/Export lịch sử quay thưởng của bản thân ra file CSV/Excel.
  * **Thông tin cá nhân:** Khách hàng có thể xem và cập nhật thông tin cá nhân.


## Hướng dẫn cho nhà phát triển
### ### Cài đặt môi trường

1. Clone repository: `git clone https://github.com/your-username/lucky-draw.git`
2. **Import project:** Import project vào IDE (IntelliJ IDEA, Eclipse,...).  Nếu dùng IntelliJ IDEA, hãy chắc chắn đã cài đặt plugin Lombok.
3. **Cấu hình database:**
  * **Production:** Cấu hình thông tin kết nối PostgreSQL trong file `application.properties`.  Tạo file này trong thư mục `src/main/resources` nếu chưa có.
  * **Development:** Ứng dụng sử dụng H2 embedded database cho môi trường development. Cấu hình nằm trong `application-dev.properties` (đã có sẵn).  Để chạy với profile `dev`, thêm `-Dspring.profiles.active=dev` vào VM Options khi chạy ứng dụng từ IDE, hoặc sử dụng argument `-Pdev` khi chạy với Gradle.
  * **Testing:** Ứng dụng sử dụng Potgress cho môi trường test.  Cấu hình nằm trong `application-test.properties` hoặc `src/test/resources/application-test.properties`. Spring Boot tự động kích hoạt profile `test` khi chạy test.
4. **Build project:**
  * Dùng IntelliJ IDEA: Build project (Build > Build Project).
  * Dùng command line: `./gradlew build`
5. **Chạy ứng dụng:**
  * **Từ IntelliJ IDEA:** Chạy class `LuckydrawApplication` trực tiếp.  Đảm bảo đã cấu hình profile `dev` trong VM Options (như bước 3).
  * **Từ command line:**  `./gradlew bootRun -Pdev` (cho môi trường development) hoặc `java -jar build/libs/luckydraw-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod` (cho môi trường production - thay `prod` bằng profile mong muốn).

### Chạy ứng dụng với profile khác

Để chạy ứng dụng với profile khác (ví dụ: `prod`, `test`), sử dụng `-Dspring.profiles.active=<profile>` trong VM Options (IntelliJ) hoặc `--spring.profiles.active=<profile>` khi chạy JAR, hoặc  `-P<profile>` với Gradle.  Ví dụ: `./gradlew bootRun -Pprod`.

### Cấu trúc thư mục
```
src 
├── main
│ ├── java
│ │ └── vn.com.fecredit.app 
│ │ └── ... (source code) 
│ ├── resources
│ │ ├── application.properties 
│ │ ├── application-dev.properties
│ │ └── ... (resources)
│ └── webapp
│   └── ... (web resources)
└── test
  ├── java
  │ └── vn.com.fecredit.app
  │   └── ... (test code)
  └── resources
    └── application-test.properties
```
### Chạy test

* **Chạy tất cả test:** `./gradlew test`

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