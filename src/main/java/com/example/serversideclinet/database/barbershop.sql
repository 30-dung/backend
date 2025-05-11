-- Tạo bảng Role (Vai trò)
CREATE TABLE `Role`
(
    `role_id`     INT PRIMARY KEY AUTO_INCREMENT,
    `role_name`   VARCHAR(50) UNIQUE NOT NULL,
    `description` TEXT
);

-- Tạo bảng User (Người dùng)
CREATE TABLE `User`
(
    `user_id`         INT PRIMARY KEY AUTO_INCREMENT,
    `first_name`      VARCHAR(50)         NOT NULL,
    `last_name`       VARCHAR(50)         NOT NULL,
    `email`           VARCHAR(255) UNIQUE NOT NULL,
    `phone_number`    VARCHAR(20)         NOT NULL,
    `password`        VARCHAR(255)        NOT NULL,
    `membership_type` ENUM ('regular', 'pro', 'vip') DEFAULT 'regular',
    `role_id`         INT                 NOT NULL,
    `loyalty_points`  INT      DEFAULT 0,
    `created_at`      DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`role_id`) REFERENCES `Role` (`role_id`)
);

-- Tạo bảng Store (Cửa hàng)
CREATE TABLE `Store`
(
    `store_id`       INT PRIMARY KEY AUTO_INCREMENT,
    `store_name`     VARCHAR(255) NOT NULL,
    `phone_number`   VARCHAR(20)  NOT NULL,
    `city_province ` VARCHAR(255) NOT NULL,
    `district `      VARCHAR(255) NOT NULL,
    `opening_time`   TIME         NOT NULL,
    `closing_time`   TIME         NOT NULL,
    `description`    TEXT,
    `average_rating` DECIMAL(3, 2) DEFAULT 0,
    `created_at`     DATETIME      DEFAULT CURRENT_TIMESTAMP
);

-- Tạo bảng Service (Dịch vụ)
CREATE TABLE `Service`
(
    `service_id`       INT PRIMARY KEY AUTO_INCREMENT,
    `service_name`     VARCHAR(255) NOT NULL,
    `description`      TEXT,
    `duration_minutes` SMALLINT     NOT NULL
);

-- Tạo bảng StoreService (Dịch vụ tại cửa hàng)
CREATE TABLE `StoreService`
(
    `store_service_id` INT PRIMARY KEY AUTO_INCREMENT,
    `store_id`         INT            NOT NULL,
    `service_id`       INT            NOT NULL,
    `price`            DECIMAL(10, 2) NOT NULL,
    UNIQUE (`store_id`, `service_id`),
    FOREIGN KEY (`store_id`) REFERENCES `Store` (`store_id`),
    FOREIGN KEY (`service_id`) REFERENCES `Service` (`service_id`)
);

-- Tạo bảng StoreServicePriceHistory (Lịch sử giá dịch vụ)
CREATE TABLE `StoreServicePriceHistory`
(
    `price_history_id` INT PRIMARY KEY AUTO_INCREMENT,
    `store_service_id` INT            NOT NULL,
    `price`            DECIMAL(10, 2) NOT NULL,
    `effective_date`   DATETIME       NOT NULL,
    `created_at`       DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`store_service_id`) REFERENCES `StoreService` (`store_service_id`)
);

-- Tạo bảng Employee (Nhân viên)
CREATE TABLE `Employee`
(
    `employee_id`    INT PRIMARY KEY AUTO_INCREMENT,
    `employee_code`  VARCHAR(20) UNIQUE  NOT NULL,
    `store_id`       INT                 NOT NULL,
    `first_name`     VARCHAR(255)        NOT NULL,
    `last_name`      VARCHAR(255)        NOT NULL,
    `avatar_url`     VARCHAR(512),
    `email`          VARCHAR(255) UNIQUE NOT NULL,
    `phone_number`   VARCHAR(20),
    `gender`         ENUM('male', 'female', 'other'),
    `date_of_birth`  DATE,
    `specialization` VARCHAR(255),
    `is_active`      BOOLEAN  DEFAULT TRUE,
    `created_at`     DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at`     DATETIME ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`store_id`) REFERENCES `Store` (`store_id`)
);

-- Tạo bảng SalaryRules (Quy tắc lương)
CREATE TABLE `SalaryRules`
(
    `rule_id`               INT PRIMARY KEY AUTO_INCREMENT,
    `description`           VARCHAR(255) NOT NULL,
    `base_salary`           DECIMAL(15, 2),
    `bonus_per_appointment` DECIMAL(10, 2),
    `bonus_percentage`      DECIMAL(5, 2),
    `effective_date`        DATETIME     NOT NULL,
    `is_active`             BOOLEAN DEFAULT TRUE
);

-- Tạo bảng WorkingTimeSlot (Khung giờ làm việc)
CREATE TABLE `WorkingTimeSlot`
(
    `time_slot_id` INT PRIMARY KEY AUTO_INCREMENT,
    `store_id`     INT      NOT NULL,
    `employee_id`  INT      NOT NULL,
    `start_time`   DATETIME NOT NULL,
    `end_time`     DATETIME NOT NULL,
    `is_available` BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (`store_id`) REFERENCES `Store` (`store_id`),
    FOREIGN KEY (`employee_id`) REFERENCES `Employee` (`employee_id`)
);

-- Tạo bảng Appointment (Lịch hẹn)
CREATE TABLE `Appointment`
(
    `appointment_id`   INT PRIMARY KEY AUTO_INCREMENT,
    `user_id`          INT        NOT NULL,
    `time_slot_id`     INT UNIQUE NOT NULL,
    `store_service_id` INT        NOT NULL,
    `employee_id`      INT        NOT NULL,
    `notes`            TEXT,
    `status`           ENUM ('pending', 'confirmed', 'completed', 'canceled') DEFAULT 'pending' CHECK (`status` IN ('pending', 'confirmed', 'completed', 'canceled')),
    `reminder_sent`    BOOLEAN  DEFAULT FALSE,
    `created_at`       DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`user_id`) REFERENCES `User` (`user_id`),
    FOREIGN KEY (`time_slot_id`) REFERENCES `WorkingTimeSlot` (`time_slot_id`),
    FOREIGN KEY (`store_service_id`) REFERENCES `StoreService` (`store_service_id`),
    FOREIGN KEY (`employee_id`) REFERENCES `Employee` (`employee_id`)
);

-- Tạo bảng Invoice (Hóa đơn)
CREATE TABLE `Invoice`
(
    `invoice_id`   INT PRIMARY KEY AUTO_INCREMENT,
    `user_id`      INT            NOT NULL,
    `total_amount` DECIMAL(10, 2) NOT NULL,
    `status`       ENUM ('pending', 'paid', 'canceled') DEFAULT 'pending' CHECK (`status` IN ('pending', 'paid', 'canceled')),
    `created_at`   DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`user_id`) REFERENCES `User` (`user_id`)
);

-- Tạo bảng InvoiceDetail (Chi tiết hóa đơn)
CREATE TABLE `InvoiceDetail`
(
    `detail_id`        INT PRIMARY KEY AUTO_INCREMENT,
    `invoice_id`       INT            NOT NULL,
    `appointment_id`   INT,
    `employee_id`      INT,
    `store_service_id` INT,
    `description`      VARCHAR(255),
    `unit_price`       DECIMAL(10, 2) NOT NULL,
    `quantity`         INT            NOT NULL DEFAULT 1,
    FOREIGN KEY (`invoice_id`) REFERENCES `Invoice` (`invoice_id`),
    FOREIGN KEY (`appointment_id`) REFERENCES `Appointment` (`appointment_id`),
    FOREIGN KEY (`employee_id`) REFERENCES `Employee` (`employee_id`),
    FOREIGN KEY (`store_service_id`) REFERENCES `StoreService` (`store_service_id`)
);

-- Tạo bảng EmployeeSalaries (Lương nhân viên)
CREATE TABLE `EmployeeSalaries`
(
    `salary_id`     INT PRIMARY KEY AUTO_INCREMENT,
    `employee_id`   INT            NOT NULL,
    `rule_id`       INT            NOT NULL,
    `base_salary`   DECIMAL(15, 2) NOT NULL,
    `bonus`         DECIMAL(15, 2) DEFAULT 0,
    `total_salary`  DECIMAL(15, 2) NOT NULL,
    `salary_month` YEAR(4),
    `salary_period` TINYINT,
    `calculated_at` DATETIME       DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`employee_id`) REFERENCES `Employee` (`employee_id`),
    FOREIGN KEY (`rule_id`) REFERENCES `SalaryRules` (`rule_id`)
);

-- Tạo bảng Review (Đánh giá)
CREATE TABLE `Review`
(
    `review_id`   INT PRIMARY KEY AUTO_INCREMENT,
    `user_id`     INT     NOT NULL,
    `target_type` ENUM ('store', 'employee', 'service') NOT NULL,
    `target_id`   INT     NOT NULL,
    `rating`      TINYINT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    `comment`     TEXT,
    `created_at`  DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`user_id`) REFERENCES `User` (`user_id`) ON DELETE CASCADE,
    INDEX         `idx_review_target` (`target_type`, `target_id`)
);

-- Thêm index để tối ưu hóa (không bao gồm UserAddress)
CREATE INDEX `idx_store_coords` ON `Store` (`latitude`, `longitude`);
CREATE INDEX `idx_time_slots` ON `WorkingTimeSlot` (`start_time`, `end_time`);
CREATE INDEX `idx_appointment_status` ON `Appointment` (`status`);
CREATE INDEX `idx_employee_store` ON `Employee` (`store_id`);
CREATE INDEX `idx_working_employee` ON `WorkingTimeSlot` (`employee_id`, `is_available`);
CREATE INDEX `idx_store_service_store` ON `StoreService` (`store_id`);
CREATE INDEX `idx_store_service_service` ON `StoreService` (`service_id`);
CREATE INDEX `idx_appointment_user` ON `Appointment` (`user_id`);

-- Thêm dữ liệu mẫu vào bảng Role
INSERT INTO `Role` (`role_name`, `description`)
VALUES ('admin', 'Quản trị viên hệ thống'),
       ('manager', 'Quản lý cửa hàng'),
       ('employee', 'Nhân viên'),
       ('customer', 'Khách hàng');

-- Thêm dữ liệu mẫu vào bảng User
INSERT INTO `User` (`first_name`, `last_name`, `email`, `phone_number`, `password`, `membership_type`, `role_id`,
                    `loyalty_points`)
VALUES ('Nguyen', 'Van A', 'nguyenvana@gmail.com', '0901234567', '$2a$10$hashedpassword', 'regular', 4, 100),
       ('Tran', 'Thi B', 'tranthib@gmail.com', '0912345678', '$2a$10$hashedpassword', 'pro', 4, 500),
       ('Le', 'Admin', 'admin@xai.com', '0923456789', '$2a$10$hashedpassword', 'vip', 1, 0);

-- Thêm dữ liệu mẫu vào bảng Store
INSERT INTO `Store` (`store_name`, `phone_number`, `city_province `, `city_province`, `district`, `closing_time`,
                     `description`, `average_rating`)
VALUES ('Cửa hàng Hà Nội', '0241234567', 'HO CHI MINH', 'Go Vap', '08:00:00', '20:00:00', 'Cửa hàng chính tại Hà Nội',
        4.5),
       ('Cửa hàng Sài Gòn', '0281234567', 'HA NOI', 'CHUONG MY', '09:00:00', '21:00:00', 'Cửa hàng tại TP.HCM', 4.2);

-- Thêm dữ liệu mẫu vào bảng Service
INSERT INTO `Service` (`service_name`, `description`, `duration_minutes`)
VALUES ('Cắt tóc nam', 'Cắt tóc cơ bản cho nam', 30),
       ('Nhuộm tóc', 'Nhuộm tóc thời trang', 90),
       ('Massage thư giãn', 'Massage toàn thân', 60);

-- Thêm dữ liệu mẫu vào bảng StoreService
INSERT INTO `StoreService` (`store_id`, `service_id`, `price`)
VALUES (1, 1, 100000.00),
       (1, 2, 500000.00),
       (2, 3, 300000.00);

-- Thêm dữ liệu mẫu vào bảng StoreServicePriceHistory
INSERT INTO `StoreServicePriceHistory` (`store_service_id`, `price`, `effective_date`)
VALUES (1, 120000.00, '2025-04-01 00:00:00'),
       (2, 550000.00, '2025-04-05 00:00:00');

-- Thêm dữ liệu mẫu vào bảng Employee
INSERT INTO `Employee` (`employee_code`, `store_id`, `first_name`, `last_name`, `email`, `phone_number`, `gender`,
                        `date_of_birth`, `specialization`)
VALUES ('NV001', 1, 'Pham', 'Van C', 'phamvanc@gmail.com', '0934567890', 'male', '1995-05-10', 'Cắt tóc'),
       ('NV002', 2, 'Hoang', 'Thi D', 'hoangthid@gmail.com', '0945678901', 'female', '1998-08-15', 'Massage');

-- Thêm dữ liệu mẫu vào bảng SalaryRules
INSERT INTO `SalaryRules` (`description`, `base_salary`, `bonus_per_appointment`, `bonus_percentage`, `effective_date`)
VALUES ('Lương cơ bản + thưởng', 5000000.00, 20000.00, NULL, '2025-01-01 00:00:00'),
       ('Thưởng theo doanh thu', NULL, NULL, 5.00, '2025-04-01 00:00:00');

-- Thêm dữ liệu mẫu vào bảng WorkingTimeSlot
INSERT INTO `WorkingTimeSlot` (`store_id`, `employee_id`, `start_time`, `end_time`, `is_available`)
VALUES (1, 1, '2025-04-07 09:00:00', '2025-04-07 09:30:00', TRUE),
       (2, 2, '2025-04-07 10:00:00', '2025-04-07 11:00:00', TRUE);

-- Thêm dữ liệu mẫu vào bảng Appointment
INSERT INTO `Appointment` (`user_id`, `time_slot_id`, `store_service_id`, `employee_id`, `notes`, `status`)
VALUES (1, 1, 1, 1, 'Cắt tóc ngắn', 'confirmed'),
       (2, 2, 3, 2, 'Massage lưng', 'pending');

-- Thêm dữ liệu mẫu vào bảng Invoice
INSERT INTO `Invoice` (`user_id`, `total_amount`, `status`)
VALUES (1, 100000.00, 'paid'),
       (2, 300000.00, 'pending');

-- Thêm dữ liệu mẫu vào bảng InvoiceDetail
INSERT INTO `InvoiceDetail` (`invoice_id`, `appointment_id`, `employee_id`, `store_service_id`, `description`,
                             `unit_price`, `quantity`)
VALUES (1, 1, 1, 1, 'Cắt tóc nam', 100000.00, 1),
       (2, 2, 2, 3, 'Massage thư giãn', 300000.00, 1);

-- Thêm dữ liệu mẫu vào bảng Review
INSERT INTO `Review` (`user_id`, `target_type`, `target_id`, `rating`, `comment`)
VALUES (1, 'store', 1, 5, 'Dịch vụ tuyệt vời!'),