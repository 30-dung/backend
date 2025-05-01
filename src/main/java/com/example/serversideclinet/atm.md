# 🧪 Danh sách Thẻ Test – Kiểm thử hệ thống thanh toán

> **Lưu ý:** Các thẻ dưới đây chỉ dùng để test, **không có giá trị giao dịch thực tế**. Sử dụng trong môi trường sandbox của các cổng thanh toán như VNPAY, NAPAS, v.v.

---

## 1. 🏦 Thẻ ATM nội địa (NCB)

| STT | Ngân hàng | Số thẻ | Tên chủ thẻ | Ngày phát hành | OTP | Trạng thái |
|-----|-----------|----------------------|----------------|----------------|--------|----------------------|
| 1 | NCB | 9704198526191432198 | NGUYEN VAN A | 07/15 | 123456 | ✅ Thành công |
| 2 | NCB | 9704195798459170488 | NGUYEN VAN A | 07/15 | 123456 | ❌ Không đủ số dư |
| 3 | NCB | 9704192181368742 | NGUYEN VAN A | 07/15 | 123456 | ❌ Thẻ chưa kích hoạt |
| 4 | NCB | 9704193370791314 | NGUYEN VAN A | 07/15 | 123456 | ❌ Thẻ bị khóa |
| 5 | NCB | 9704194841945513 | NGUYEN VAN A | 07/15 | 123456 | ❌ Thẻ hết hạn |

---

## 2. 🌐 Thẻ Quốc tế

### VISA

| STT | Số thẻ | CVC | Tên chủ thẻ | Hết hạn | 3DS | Trạng thái |
|-----|--------------------|-----|----------------|---------|------|-------------|
| 6 | 4456530000001005 | 123 | NGUYEN VAN A | 12/26 | ❌ Không có | ✅ Thành công |
| 7 | 4456530000001096 | 123 | NGUYEN VAN A | 12/26 | ✅ Có | ✅ Thành công |

### MasterCard

| STT | Số thẻ | CVC | Tên chủ thẻ | Hết hạn | 3DS | Trạng thái |
|-----|--------------------|-----|----------------|---------|------|-------------|
| 8 | 5200000000001005 | 123 | NGUYEN VAN A | 12/26 | ❌ Không có | ✅ Thành công |
| 9 | 5200000000001096 | 123 | NGUYEN VAN A | 12/26 | ✅ Có | ✅ Thành công |

### JCB

| STT | Số thẻ | CVC | Tên chủ thẻ | Hết hạn | 3DS | Trạng thái |
|-----|--------------------|-----|----------------|---------|------|-------------|
| 10 | 3337000000000008 | 123 | NGUYEN VAN A | 12/26 | ❌ Không có | ✅ Thành công |
| 11 | 3337000000200004 | 123 | NGUYEN VAN A | 12/24 | ✅ Có | ✅ Thành công |

> **Thông tin thêm cho thẻ quốc tế:**
> - Email: `test@gmail.com`
> - Địa chỉ: `22 Lang Ha, Ha Noi`

---

## 3. 🏧 Thẻ ATM nội địa khác

### Nhóm Bank qua NAPAS

| STT | Số thẻ | Tên chủ thẻ | Ngày phát hành | OTP | Trạng thái |
|-----|----------------------|----------------|----------------|------|------------|
| 12 | 9704000000000018<br>9704020000000016 | NGUYEN VAN A | 03/07 | otp | ✅ Thành công |

### EXIMBANK

| STT | Số thẻ | Tên chủ thẻ | Ngày hết hạn | Trạng thái |
|-----|----------------------|----------------|----------------|------------|
| 13 | 9704310005819191 | NGUYEN VAN A | 10/26 | ✅ Thành công |

---

## ⚠️ Ghi chú

- Tất cả thẻ trong danh sách này chỉ dùng để test hệ thống.
- Không được sử dụng trong môi trường thực tế.
- Thẻ có thể không hoạt động nếu hệ thống test không hỗ trợ loại hoặc trạng thái tương ứng.

