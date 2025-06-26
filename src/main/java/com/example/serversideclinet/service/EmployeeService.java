// service/EmployeeService.java
package com.example.serversideclinet.service;

import com.example.serversideclinet.dto.AppointmentStatusResponseDTO;
import com.example.serversideclinet.dto.EmployeeRequestDTO;
import com.example.serversideclinet.dto.EmployeeProfileUpdateDTO; // Import
import com.example.serversideclinet.dto.PasswordUpdateDTO; // Import
import com.example.serversideclinet.dto.PendingAppointmentDTO;
import com.example.serversideclinet.model.Appointment;
import com.example.serversideclinet.model.Employee;
import com.example.serversideclinet.model.Role;
import com.example.serversideclinet.model.Store;
import com.example.serversideclinet.repository.AppointmentRepository;
import com.example.serversideclinet.repository.EmployeeRepository;
import com.example.serversideclinet.repository.RoleRepository;
import com.example.serversideclinet.repository.StoreRepository;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value; // Import Value
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files; // Import Files
import java.nio.file.Path; // Import Path
import java.nio.file.Paths; // Import Paths
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private AppointmentRepository appointmentRepository;
    @Autowired
    private StoreRepository storeRepository;
    @Autowired
    private EmailService emailService;

    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${file.upload-dir}") // Đảm bảo cấu hình thư mục upload
    private String uploadDir;

    // Helper method to delete old image file
    private void deleteOldImageFile(String imageUrl) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            String fileName = imageUrl.replace("/images/", ""); // Loại bỏ tiền tố
            Path filePath = Paths.get(uploadDir).resolve(fileName);
            try {
                if (Files.exists(filePath) && !Files.isDirectory(filePath)) {
                    Files.delete(filePath);
                    System.out.println("Deleted old employee avatar: " + filePath.toString());
                }
            } catch (IOException e) {
                System.err.println("Could not delete old employee avatar " + filePath.toString() + ": " + e.getMessage());
            }
        }
    }

    public Employee createEmployee(EmployeeRequestDTO dto) {
        if (employeeRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email đã tồn tại. Vui lòng sử dụng email khác.");
        }

        // Kiểm tra mã nhân viên
        if (employeeRepository.existsByEmployeeCode(dto.getEmployeeCode())) {
            throw new RuntimeException("Mã nhân viên đã tồn tại");
        }

        // Kiểm tra email
        if (employeeRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email đã được sử dụng");
        }

        // Kiểm tra số điện thoại
        if (employeeRepository.existsByPhoneNumber(dto.getPhoneNumber())) {
            throw new RuntimeException("Số điện thoại đã được sử dụng");
        }

        // Kiểm tra store
        Store store = storeRepository.findById(dto.getStoreId())
                .orElseThrow(() -> new RuntimeException("Store không tồn tại"));
        // Kiểm tra roles
        List<Role> roles = roleRepository.findAllById(dto.getRoleIds());
        if (roles.size() != dto.getRoleIds().size()) {
            throw new RuntimeException("Một hoặc nhiều role không tồn tại");
        }

        Employee employee = new Employee();
        employee.setEmployeeCode(dto.getEmployeeCode());
        employee.setFullName(dto.getFullName());
        employee.setEmail(dto.getEmail());
        employee.setPassword(passwordEncoder.encode(dto.getPassword()));
        employee.setPhoneNumber(dto.getPhoneNumber());
        employee.setGender(dto.getGender());
        employee.setDateOfBirth(dto.getDateOfBirth().atStartOfDay());
        employee.setSpecialization(dto.getSpecialization());
        employee.setStore(store);
        employee.setRoles(new HashSet<>(roles));
        employee.setAvatarUrl(dto.getAvatarUrl()); // Avatar URL sẽ được frontend upload và gửi qua

        // *** THÊM PHẦN NÀY: Set lương mặc định ***
        // Lương cơ bản mặc định: 10,000,000 VND
        employee.setBaseSalary(new BigDecimal("10000000.00"));
        // Tỷ lệ hoa hồng mặc định: 5% (0.05)
        employee.setCommissionRate(new BigDecimal("0.05"));
        // Loại lương: Lương cố định + hoa hồng
        employee.setSalaryType(Employee.SalaryType.MIXED);

        return employeeRepository.save(employee);
    }

    /**
     * Cập nhật thông tin lương cho nhân viên
     */
    public Employee updateEmployeeSalary(Integer employeeId, BigDecimal baseSalary,
                                         BigDecimal commissionRate, Employee.SalaryType salaryType) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));
        if (baseSalary != null) {
            employee.setBaseSalary(baseSalary);
        }

        if (commissionRate != null) {
            employee.setCommissionRate(commissionRate);
        }

        if (salaryType != null) {
            employee.setSalaryType(salaryType);
        }

        employee.setUpdatedAt(LocalDateTime.now());

        return employeeRepository.save(employee);
    }

    /**
     * Lấy tất cả nhân viên từ hệ thống
     *
     * @return Danh sách tất cả nhân viên
     */
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    /**
     * Tìm nhân viên theo ID
     *
     * @param id ID của nhân viên cần tìm
     * @return Optional chứa thông tin nhân viên nếu tìm thấy
     */
    public Optional<Employee> findEmployeeById(Integer id) {
        return employeeRepository.findById(id);
    }

    public List<PendingAppointmentDTO> getPendingAppointmentsForEmployee(Integer employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với ID: " + employeeId));
        List<Appointment> appointments = appointmentRepository.findByEmployeeAndStatus(employee, Appointment.Status.PENDING);

        return appointments.stream().map(appt -> new PendingAppointmentDTO(
                appt.getAppointmentId(),
                appt.getStartTime() + " - " + appt.getEndTime(),
                appt.getCreatedAt().toString(),
                appt.getStoreService().getService().getServiceName(),
                appt.getUser().getFullName(),
                appt.getUser().getPhoneNumber(),
                appt.getUser().getEmail()
        )).toList();
    }

    @Transactional
    public AppointmentStatusResponseDTO updateAppointmentStatus(Integer employeeId, Integer appointmentId, String action) throws MessagingException, IOException {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy cuộc hẹn"));
        if (!appointment.getEmployee().getEmployeeId().equals(employeeId)) {
            throw new RuntimeException("Bạn không có quyền thay đổi cuộc hẹn này");
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        // <-- Khai báo formatter
        String timeRange = appointment.getStartTime().format(formatter) + " - " + appointment.getEndTime().format(formatter);
        // <-- Sử dụng formatter
        String customerName = appointment.getUser().getFullName();
        String customerEmail = appointment.getUser().getEmail();
        // Lấy email khách hàng
        String employeeName = employee.getFullName();
        String serviceName = appointment.getStoreService().getService().getServiceName();
        switch (action.toUpperCase()) {
            case "CONFIRM":
                if (appointment.getStatus() != Appointment.Status.PENDING) {
                    throw new RuntimeException("Chỉ có thể xác nhận cuộc hẹn ở trạng thái PENDING");
                }
                appointment.setStatus(Appointment.Status.CONFIRMED);
                appointmentRepository.save(appointment);
                emailService.sendAppointmentConfirmation(customerEmail, customerName, employeeName, timeRange, serviceName);
                break;
            case "CANCEL": // Logic cho Employee hủy lịch
                // Cho phép hủy nếu đang PENDING hoặc CONFIRMED
                if (appointment.getStatus() != Appointment.Status.PENDING && appointment.getStatus() != Appointment.Status.CONFIRMED) {
                    throw new RuntimeException("Chỉ có thể hủy cuộc hẹn ở trạng thái PENDING hoặc CONFIRMED");
                }
                appointment.setStatus(Appointment.Status.CANCELED);
                appointmentRepository.save(appointment);
                // GỌI ĐÚNG PHƯƠNG THỨC VÀ TRUYỀN ĐỦ THAM SỐ
                emailService.sendAppointmentCancellationToCustomer(
                        customerEmail,
                        customerName,
                        employeeName,
                        timeRange,
                        serviceName,
                        employeeName + " (Nhân viên)" // Người hủy là nhân viên
                );
                break;
            case "COMPLETE":
                if (appointment.getStatus() != Appointment.Status.CONFIRMED) {
                    throw new RuntimeException("Chỉ có thể đánh dấu hoàn thành cuộc hẹn ở trạng thái CONFIRMED");
                }
                appointment.setStatus(Appointment.Status.COMPLETED);
                appointment.setCompletedAt(LocalDateTime.now());
                appointment.setSalaryCalculated(false);
                // Ensure salary can be calculated
                appointmentRepository.save(appointment);
                emailService.sendAppointmentCompletion(customerEmail, customerName, employeeName, timeRange, serviceName);
                break;
            default:
                throw new IllegalArgumentException("Hành động không hợp lệ: chỉ chấp nhận CONFIRM, CANCEL hoặc COMPLETE");
        }

        return new AppointmentStatusResponseDTO(
                appointment.getAppointmentId(),
                appointment.getStatus().name(),
                appointment.getEndTime().toString(),
                appointment.getStartTime().toString(),
                employeeName,
                customerName,
                serviceName,
                appointment.getNotes()
        );
    }


    public List<Employee> getEmployeesByStore(Integer storeId) {
        return employeeRepository.findByStoreStoreId(storeId);
    }

    /**
     * Cập nhật thông tin cá nhân của nhân viên.
     * Cho phép nhân viên sửa đổi fullName, email, phoneNumber, gender, dateOfBirth, specialization, avatarUrl.
     *
     * @param employeeId ID của nhân viên cần cập nhật.
     * @param updateDTO DTO chứa thông tin mới.
     * @return Đối tượng Employee sau khi cập nhật.
     */
    @Transactional
    public Employee updateEmployeeProfile(Integer employeeId, EmployeeProfileUpdateDTO updateDTO) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với ID: " + employeeId));
        // Kiểm tra email trùng lặp (nếu email được cập nhật và khác email hiện tại của nhân viên)
        if (!employee.getEmail().equals(updateDTO.getEmail()) && employeeRepository.existsByEmail(updateDTO.getEmail())) {
            throw new IllegalArgumentException("Email đã được sử dụng bởi người dùng khác.");
        }

        // Kiểm tra số điện thoại trùng lặp (nếu số điện thoại được cập nhật và khác số điện thoại hiện tại của nhân viên)
        if (!employee.getPhoneNumber().equals(updateDTO.getPhoneNumber()) && employeeRepository.existsByPhoneNumber(updateDTO.getPhoneNumber())) {
            throw new IllegalArgumentException("Số điện thoại đã được sử dụng bởi người dùng khác.");
        }

        // Xóa ảnh cũ nếu avatarUrl thay đổi hoặc bị xóa
        if (employee.getAvatarUrl() != null && !employee.getAvatarUrl().isEmpty() &&
                (updateDTO.getAvatarUrl() == null || updateDTO.getAvatarUrl().isEmpty() || // Nếu avatar mới là rỗng
                        !employee.getAvatarUrl().equals(updateDTO.getAvatarUrl()))) { // Hoặc avatar mới khác cũ
            deleteOldImageFile(employee.getAvatarUrl());
        }

        employee.setFullName(updateDTO.getFullName());
        employee.setEmail(updateDTO.getEmail());
        employee.setPhoneNumber(updateDTO.getPhoneNumber());
        employee.setGender(updateDTO.getGender());
        if (updateDTO.getDateOfBirth() != null) {
            employee.setDateOfBirth(updateDTO.getDateOfBirth().atStartOfDay());
        }
        employee.setSpecialization(updateDTO.getSpecialization());
        employee.setAvatarUrl(updateDTO.getAvatarUrl()); // Cập nhật URL ảnh mới (hoặc rỗng)
        employee.setUpdatedAt(LocalDateTime.now());

        return employeeRepository.save(employee);
    }

    /**
     * Cập nhật mật khẩu của nhân viên.
     * Yêu cầu nhập mật khẩu hiện tại, mật khẩu mới và xác nhận mật khẩu mới.
     *
     * @param employeeId ID của nhân viên.
     * @param passwordUpdateDTO DTO chứa mật khẩu hiện tại, mật khẩu mới và xác nhận mật khẩu mới.
     * @return Đối tượng Employee sau khi cập nhật mật khẩu.
     */
    @Transactional
    public Employee updateEmployeePassword(Integer employeeId, PasswordUpdateDTO passwordUpdateDTO) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với ID: " + employeeId));
        // 1. Kiểm tra mật khẩu hiện tại có đúng không
        if (!passwordEncoder.matches(passwordUpdateDTO.getCurrentPassword(), employee.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu hiện tại không chính xác.");
        }

        // 2. Kiểm tra mật khẩu mới và xác nhận mật khẩu mới có khớp nhau không
        if (!passwordUpdateDTO.getNewPassword().equals(passwordUpdateDTO.getConfirmNewPassword())) {
            throw new IllegalArgumentException("Mật khẩu mới và xác nhận mật khẩu mới không khớp.");
        }

        // 3. Mã hóa và lưu mật khẩu mới
        employee.setPassword(passwordEncoder.encode(passwordUpdateDTO.getNewPassword()));
        employee.setUpdatedAt(LocalDateTime.now());

        return employeeRepository.save(employee);
    }


    @Transactional
    public Employee adminUpdateEmployee(Integer employeeId, EmployeeRequestDTO updateDTO) { // Sử dụng EmployeeRequestDTO
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với ID: " + employeeId));
        // Kiểm tra email trùng lặp (nếu email được cập nhật và khác email hiện tại của nhân viên)
        if (!employee.getEmail().equals(updateDTO.getEmail()) && employeeRepository.existsByEmail(updateDTO.getEmail())) {
            throw new IllegalArgumentException("Email đã được sử dụng bởi người dùng khác.");
        }

        // Kiểm tra số điện thoại trùng lặp (nếu số điện thoại được cập nhật và khác số điện thoại hiện tại của nhân viên)
        if (!employee.getPhoneNumber().equals(updateDTO.getPhoneNumber()) && employeeRepository.existsByPhoneNumber(updateDTO.getPhoneNumber())) {
            throw new IllegalArgumentException("Số điện thoại đã được sử dụng bởi người dùng khác.");
        }

        // Xóa ảnh cũ nếu avatarUrl thay đổi hoặc bị xóa
        if (employee.getAvatarUrl() != null && !employee.getAvatarUrl().isEmpty() &&
                (updateDTO.getAvatarUrl() == null || updateDTO.getAvatarUrl().isEmpty() || // Nếu avatar mới là rỗng
                        !employee.getAvatarUrl().equals(updateDTO.getAvatarUrl()))) { // Hoặc avatar mới khác cũ
            deleteOldImageFile(employee.getAvatarUrl());
        }


        // Kiểm tra và cập nhật Store nếu có thay đổi
        if (updateDTO.getStoreId() != null && !employee.getStore().getStoreId().equals(updateDTO.getStoreId())) {
            Store newStore = storeRepository.findById(updateDTO.getStoreId())
                    .orElseThrow(() -> new RuntimeException("Store mới không tồn tại"));
            employee.setStore(newStore);
        }

        // Kiểm tra và cập nhật Roles nếu có thay đổi
        if (updateDTO.getRoleIds() != null) {
            Set<Integer> currentRoleIds = new HashSet<>();
            employee.getRoles().forEach(role -> currentRoleIds.add(role.getRoleId()));

            Set<Integer> newRoleIds = new HashSet<>(updateDTO.getRoleIds());

            if (!currentRoleIds.equals(newRoleIds)) {
                List<Role> newRoles = roleRepository.findAllById(updateDTO.getRoleIds());
                if (newRoles.size() != updateDTO.getRoleIds().size()) {
                    throw new RuntimeException("Một hoặc nhiều role không tồn tại");
                }
                employee.setRoles(new HashSet<>(newRoles));
            }
        }


        // Cập nhật các thông tin khác của nhân viên
        if (updateDTO.getEmployeeCode() != null) {
            // Kiểm tra employeeCode nếu thay đổi
            if (!employee.getEmployeeCode().equals(updateDTO.getEmployeeCode()) && employeeRepository.existsByEmployeeCode(updateDTO.getEmployeeCode())) {
                throw new RuntimeException("Mã nhân viên đã tồn tại");
            }
            employee.setEmployeeCode(updateDTO.getEmployeeCode());
        }
        if (updateDTO.getFullName() != null) {
            employee.setFullName(updateDTO.getFullName());
        }
        if (updateDTO.getEmail() != null) {
            employee.setEmail(updateDTO.getEmail());
        }
        if (updateDTO.getPhoneNumber() != null) {
            employee.setPhoneNumber(updateDTO.getPhoneNumber());
        }
        if (updateDTO.getGender() != null) {
            employee.setGender(updateDTO.getGender());
        }
        if (updateDTO.getDateOfBirth() != null) {
            employee.setDateOfBirth(updateDTO.getDateOfBirth().atStartOfDay());
        }
        if (updateDTO.getSpecialization() != null) {
            employee.setSpecialization(updateDTO.getSpecialization());
        }
        if (updateDTO.getAvatarUrl() != null) {
            employee.setAvatarUrl(updateDTO.getAvatarUrl());
        }
        // Chỉ cập nhật mật khẩu nếu nó được cung cấp trong DTO
        if (updateDTO.getPassword() != null && !updateDTO.getPassword().isEmpty()) {
            employee.setPassword(passwordEncoder.encode(updateDTO.getPassword()));
        }


        // Cập nhật thông tin lương
        if (updateDTO.getBaseSalary() != null) {
            employee.setBaseSalary(updateDTO.getBaseSalary());
        }
        if (updateDTO.getCommissionRate() != null) {
            employee.setCommissionRate(updateDTO.getCommissionRate());
        }
        if (updateDTO.getSalaryType() != null) {
            employee.setSalaryType(updateDTO.getSalaryType());
        }

        employee.setUpdatedAt(LocalDateTime.now());

        return employeeRepository.save(employee);
    }
}