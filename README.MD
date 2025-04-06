# Tài liệu Database

## Tổng quan

Database này được thiết kế để quản lý một hệ thống dịch vụ (ví dụ: salon, spa, hoặc cửa hàng dịch vụ tương tự). Nó hỗ trợ các chức năng như quản lý người dùng, cửa hàng, dịch vụ, lịch hẹn, hóa đơn, lương nhân viên, và đánh giá. Các bảng được liên kết chặt chẽ thông qua khóa ngoại để đảm bảo tính toàn vẹn dữ liệu.

Link view database: https://www.dbdiagram.io/d/Copy-of-sem4-67f29b384f7afba18485fa8c

## Mô tả các bảng

### 1. Role (Vai trò)

- **Mô tả**: Lưu trữ các vai trò trong hệ thống (admin, manager, employee, customer).
- **Cột**:
  - `role_id` (INT, PK, AUTO_INCREMENT): Mã vai trò.
  - `role_name` (VARCHAR(50), UNIQUE, NOT NULL): Tên vai trò.
  - `description` (TEXT): Mô tả vai trò.

### 2. User (Người dùng)

- **Mô tả**: Lưu thông tin người dùng (bao gồm cả khách hàng và quản trị viên).
- **Cột**:
  - `user_id` (INT, PK, AUTO_INCREMENT): Mã người dùng.
  - `first_name` (VARCHAR(50), NOT NULL): Tên.
  - `last_name` (VARCHAR(50), NOT NULL): Họ.
  - `email` (VARCHAR(255), UNIQUE, NOT NULL): Email.
  - `phone_number` (VARCHAR(20), NOT NULL): Số điện thoại.
  - `password` (VARCHAR(255), NOT NULL): Mật khẩu (đã mã hóa).
  - `membership_type` (ENUM: 'regular', 'pro', 'vip', DEFAULT 'regular'): Loại thành viên.
  - `role_id` (INT, FK, NOT NULL): Liên kết đến bảng `Role`.
  - `loyalty_points` (INT, DEFAULT 0): Điểm tích lũy.
  - `created_at` (DATETIME, DEFAULT CURRENT_TIMESTAMP): Thời gian tạo.

### 3. Store (Cửa hàng)

- **Mô tả**: Lưu thông tin các cửa hàng.
- **Cột**:
  - `store_id` (INT, PK, AUTO_INCREMENT): Mã cửa hàng.
  - `store_name` (VARCHAR(255), NOT NULL): Tên cửa hàng.
  - `phone_number` (VARCHAR(20), NOT NULL): Số điện thoại.
  - `latitude` (DECIMAL(10,8), NOT NULL): Vĩ độ.
  - `longitude` (DECIMAL(11,8), NOT NULL): Kinh độ.
  - `opening_time` (TIME, NOT NULL): Giờ mở cửa.
  - `closing_time` (TIME, NOT NULL): Giờ đóng cửa.
  - `description` (TEXT): Mô tả.
  - `average_rating` (DECIMAL(3,2), DEFAULT 0): Điểm đánh giá trung bình.
  - `created_at` (DATETIME, DEFAULT CURRENT_TIMESTAMP): Thời gian tạo.

### 4. Service (Dịch vụ)

- **Mô tả**: Lưu thông tin các dịch vụ cung cấp.
- **Cột**:
  - `service_id` (INT, PK, AUTO_INCREMENT): Mã dịch vụ.
  - `service_name` (VARCHAR(255), NOT NULL): Tên dịch vụ.
  - `description` (TEXT): Mô tả.
  - `duration_minutes` (SMALLINT, NOT NULL): Thời gian thực hiện (phút).

### 5. StoreService (Dịch vụ tại cửa hàng)

- **Mô tả**: Liên kết dịch vụ với cửa hàng và lưu giá hiện tại.
- **Cột**:
  - `store_service_id` (INT, PK, AUTO_INCREMENT): Mã dịch vụ tại cửa hàng.
  - `store_id` (INT, FK, NOT NULL): Mã cửa hàng.
  - `service_id` (INT, FK, NOT NULL): Mã dịch vụ.
  - `price` (DECIMAL(10,2), NOT NULL): Giá hiện tại.
  - **Ràng buộc**: UNIQUE (`store_id`, `service_id`).

### 6. StoreServicePriceHistory (Lịch sử giá dịch vụ)

- **Mô tả**: Lưu lịch sử giá của dịch vụ tại cửa hàng.
- **Cột**:
  - `price_history_id` (INT, PK, AUTO_INCREMENT): Mã lịch sử giá.
  - `store_service_id` (INT, FK, NOT NULL): Mã dịch vụ tại cửa hàng.
  - `price` (DECIMAL(10,2), NOT NULL): Giá.
  - `effective_date` (DATETIME, NOT NULL): Ngày giá có hiệu lực.
  - `created_at` (DATETIME, DEFAULT CURRENT_TIMESTAMP): Thời gian tạo.

### 7. Employee (Nhân viên)

- **Mô tả**: Lưu thông tin nhân viên.
- **Cột**:
  - `employee_id` (INT, PK, AUTO_INCREMENT): Mã nhân viên.
  - `employee_code` (VARCHAR(20), UNIQUE, NOT NULL): Mã định danh nhân viên.
  - `store_id` (INT, FK, NOT NULL): Mã cửa hàng.
  - `first_name` (VARCHAR(255), NOT NULL): Tên.
  - `last_name` (VARCHAR(255), NOT NULL): Họ.
  - `avatar_url` (VARCHAR(512)): URL ảnh đại diện.
  - `email` (VARCHAR(255), UNIQUE, NOT NULL): Email.
  - `phone_number` (VARCHAR(20)): Số điện thoại.
  - `gender` (ENUM: 'male', 'female', 'other'): Giới tính.
  - `date_of_birth` (DATE): Ngày sinh.
  - `specialization` (VARCHAR(255)): Chuyên môn.
  - `is_active` (BOOLEAN, DEFAULT TRUE): Trạng thái hoạt động.
  - `created_at` (DATETIME, DEFAULT CURRENT_TIMESTAMP): Thời gian tạo.
  - `updated_at` (DATETIME, ON UPDATE CURRENT_TIMESTAMP): Thời gian cập nhật.

### 8. SalaryRules (Quy tắc lương)

- **Mô tả**: Lưu các quy tắc tính lương.
- **Cột**:
  - `rule_id` (INT, PK, AUTO_INCREMENT): Mã quy tắc.
  - `description` (VARCHAR(255), NOT NULL): Mô tả.
  - `base_salary` (DECIMAL(15,2)): Lương cơ bản (NULL nếu không áp dụng).
  - `bonus_per_appointment` (DECIMAL(10,2)): Thưởng mỗi lịch hẹn (NULL nếu không áp dụng).
  - `bonus_percentage` (DECIMAL(5,2)): % doanh thu (NULL nếu không áp dụng).
  - `effective_date` (DATETIME, NOT NULL): Ngày áp dụng.
  - `is_active` (BOOLEAN, DEFAULT TRUE): Trạng thái hoạt động.

### 9. WorkingTimeSlot (Khung giờ làm việc)

- **Mô tả**: Lưu khung giờ làm việc của nhân viên.
- **Cột**:
  - `time_slot_id` (INT, PK, AUTO_INCREMENT): Mã khung giờ.
  - `store_id` (INT, FK, NOT NULL): Mã cửa hàng.
  - `employee_id` (INT, FK, NOT NULL): Mã nhân viên.
  - `start_time` (DATETIME, NOT NULL): Thời gian bắt đầu.
  - `end_time` (DATETIME, NOT NULL): Thời gian kết thúc.
  - `is_available` (BOOLEAN, DEFAULT TRUE): Trạng thái khả dụng.

### 10. Appointment (Lịch hẹn)

- **Mô tả**: Lưu thông tin lịch hẹn của khách hàng.
- **Cột**:
  - `appointment_id` (INT, PK, AUTO_INCREMENT): Mã lịch hẹn.
  - `user_id` (INT, FK, NOT NULL): Mã khách hàng.
  - `time_slot_id` (INT, FK, UNIQUE, NOT NULL): Mã khung giờ.
  - `store_service_id` (INT, FK, NOT NULL): Mã dịch vụ tại cửa hàng.
  - `employee_id` (INT, FK, NOT NULL): Mã nhân viên.
  - `notes` (TEXT): Ghi chú.
  - `status` (ENUM: 'pending', 'confirmed', 'completed', 'canceled', DEFAULT 'pending'): Trạng thái.
  - `reminder_sent` (BOOLEAN, DEFAULT FALSE): Đã gửi nhắc nhở chưa.
  - `created_at` (DATETIME, DEFAULT CURRENT_TIMESTAMP): Thời gian tạo.

### 11. Invoice (Hóa đơn)

- **Mô tả**: Lưu thông tin hóa đơn.
- **Cột**:
  - `invoice_id` (INT, PK, AUTO_INCREMENT): Mã hóa đơn.
  - `user_id` (INT, FK, NOT NULL): Mã khách hàng.
  - `total_amount` (DECIMAL(10,2), NOT NULL): Tổng tiền.
  - `status` (ENUM: 'pending', 'paid', 'canceled', DEFAULT 'pending'): Trạng thái.
  - `created_at` (DATETIME, DEFAULT CURRENT_TIMESTAMP): Thời gian tạo.

### 12. InvoiceDetail (Chi tiết hóa đơn)

- **Mô tả**: Lưu chi tiết các mục trong hóa đơn.
- **Cột**:
  - `detail_id` (INT, PK, AUTO_INCREMENT): Mã chi tiết.
  - `invoice_id` (INT, FK, NOT NULL): Mã hóa đơn.
  - `appointment_id` (INT, FK): Mã lịch hẹn (NULL nếu không liên quan).
  - `employee_id` (INT, FK): Mã nhân viên (NULL nếu không liên quan).
  - `store_service_id` (INT, FK): Mã dịch vụ tại cửa hàng (NULL nếu không phải dịch vụ).
  - `description` (VARCHAR(255)): Mô tả.
  - `unit_price` (DECIMAL(10,2), NOT NULL): Đơn giá.
  - `quantity` (INT, NOT NULL, DEFAULT 1): Số lượng.

### 13. EmployeeSalaries (Lương nhân viên)

- **Mô tả**: Lưu thông tin lương của nhân viên.
- **Cột**:
  - `salary_id` (INT, PK, AUTO_INCREMENT): Mã lương.
  - `employee_id` (INT, FK, NOT NULL): Mã nhân viên.
  - `rule_id` (INT, FK, NOT NULL): Mã quy tắc lương.
  - `base_salary` (DECIMAL(15,2), NOT NULL): Lương cơ bản.
  - `bonus` (DECIMAL(15,2), DEFAULT 0): Thưởng.
  - `total_salary` (DECIMAL(15,2), NOT NULL): Tổng lương.
  - `salary_month` (YEAR(4)): Tháng lương.
  - `salary_period` (TINYINT): Kỳ lương (1-12).
  - `calculated_at` (DATETIME, DEFAULT CURRENT_TIMESTAMP): Thời gian tính.

### 14. Review (Đánh giá)

- **Mô tả**: Lưu đánh giá từ khách hàng về cửa hàng, nhân viên, hoặc dịch vụ.
- **Cột**:
  - `review_id` (INT, PK, AUTO_INCREMENT): Mã đánh giá.
  - `user_id` (INT, FK, NOT NULL): Mã khách hàng.
  - `target_type` (ENUM: 'store', 'employee', 'service', NOT NULL): Loại đối tượng được đánh giá.
  - `target_id` (INT, NOT NULL): Mã đối tượng được đánh giá.
  - `rating` (TINYINT, NOT NULL, CHECK 1-5): Điểm đánh giá.
  - `comment` (TEXT): Bình luận.
  - `created_at` (DATETIME, DEFAULT CURRENT_TIMESTAMP): Thời gian tạo.

## Các luồng chính (Workflow)

### 1. Luồng của Khách hàng (Customer)

Khách hàng là người sử dụng dịch vụ, đặt lịch hẹn, thanh toán, và đánh giá.

#### a. Đăng ký/Đăng nhập

- **Bước**:
  1. Khách hàng cung cấp thông tin (`first_name`, `last_name`, `email`, `phone_number`, `password`) để đăng ký tài khoản trong bảng `User`.
  2. Vai trò mặc định là `customer` (liên kết với `role_id` trong `Role`).
  3. Đăng nhập bằng `email` và `password`.

#### b. Tìm kiếm và đặt lịch hẹn

- **Bước**:
  1. Xem danh sách cửa hàng (`Store`) và dịch vụ (`Service`) qua `StoreService`.
  2. Kiểm tra khung giờ trống của nhân viên qua `WorkingTimeSlot` (WHERE `is_available` = TRUE).
  3. Chọn dịch vụ (`store_service_id`), nhân viên (`employee_id`), và khung giờ (`time_slot_id`).
  4. Tạo bản ghi trong `Appointment` với trạng thái `pending`.
  5. Hệ thống cập nhật `is_available` = FALSE trong `WorkingTimeSlot`.

#### c. Thanh toán hóa đơn

- **Bước**:
  1. Sau khi hoàn thành dịch vụ, hệ thống tạo hóa đơn trong `Invoice` với `status` = `pending`.
  2. Chi tiết hóa đơn được thêm vào `InvoiceDetail` (liên kết với `appointment_id` và `store_service_id`).
  3. Khách hàng thanh toán, cập nhật `status` = `paid`.

#### d. Đánh giá

- **Bước**:
  1. Sau khi hoàn thành dịch vụ, khách hàng thêm đánh giá vào `Review`.
  2. Chọn `target_type` (store/employee/service) và `target_id` tương ứng.
  3. Cập nhật `average_rating` trong `Store` nếu đánh giá cửa hàng.

### 2. Luồng của Nhân viên (Employee)

Nhân viên thực hiện dịch vụ và quản lý lịch làm việc của mình.

#### a. Đăng ký thông tin

- **Bước**:
  1. Admin hoặc manager thêm nhân viên vào `Employee` với thông tin như `employee_code`, `store_id`, `specialization`.
  2. Nhân viên có thể đăng nhập bằng `email` nếu được cấp tài khoản trong `User` (vai trò `employee`).

#### b. Quản lý khung giờ làm việc

- **Bước**:
  1. Nhân viên thêm khung giờ làm việc vào `WorkingTimeSlot` (chỉ định `start_time`, `end_time`).
  2. Khung giờ mặc định `is_available` = TRUE cho đến khi khách hàng đặt lịch.

#### c. Thực hiện dịch vụ

- **Bước**:
  1. Xem danh sách lịch hẹn trong `Appointment` (WHERE `employee_id` = ID của nhân viên).
  2. Cập nhật trạng thái lịch hẹn từ `pending` → `confirmed` → `completed` sau khi thực hiện.

#### d. Nhận lương

- **Bước**:
  1. Hệ thống tự động tính lương dựa trên `SalaryRules` và lịch hẹn hoàn thành (`Appointment` với `status` = `completed`).
  2. Kết quả được lưu vào `EmployeeSalaries`.

### 3. Luồng của Admin

Admin quản lý toàn bộ hệ thống, bao gồm dữ liệu và người dùng.

#### a. Quản lý người dùng

- **Bước**:
  1. Thêm/sửa/xóa thông tin trong `User` và `Role`.
  2. Gán vai trò cho người dùng (admin, manager, employee, customer).

#### b. Quản lý cửa hàng và dịch vụ

- **Bước**:
  1. Thêm/sửa/xóa cửa hàng trong `Store`.
  2. Thêm/sửa/xóa dịch vụ trong `Service`.
  3. Cập nhật giá dịch vụ trong `StoreService` và lưu lịch sử giá trong `StoreServicePriceHistory`.

#### c. Quản lý nhân viên

- **Bước**:
  1. Thêm/sửa/xóa nhân viên trong `Employee`.
  2. Thiết lập quy tắc lương trong `SalaryRules`.

#### d. Theo dõi và báo cáo

- **Bước**:
  1. Xem danh sách lịch hẹn (`Appointment`), hóa đơn (`Invoice`), và lương (`EmployeeSalaries`).
  2. Tổng hợp đánh giá từ `Review` để cải thiện dịch vụ.

## Lưu ý

- **Tính toàn vẹn dữ liệu**: Các khóa ngoại đảm bảo không có dữ liệu mồ côi.
- **Tối ưu hóa**: Các index đã được thêm để tăng tốc truy vấn (ví dụ: `idx_store_coords`, `idx_appointment_status`).
- **Bảo mật**: Mật khẩu trong `User` cần được mã hóa (bcrypt/Argon2).
