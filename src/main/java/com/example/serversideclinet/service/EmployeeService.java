package com.example.serversideclinet.service;

import com.example.serversideclinet.dto.AppointmentStatusResponseDTO;
import com.example.serversideclinet.dto.EmployeeRequestDTO;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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
        employee.setAvatarUrl(dto.getAvatarUrl());

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

        String timeRange = appointment.getStartTime() + " - " + appointment.getEndTime();
        String customerName = appointment.getUser().getFullName();
        String email = appointment.getUser().getEmail();
        String employeeName = employee.getFullName();
        String serviceName = appointment.getStoreService().getService().getServiceName();

        switch (action.toUpperCase()) {
            case "CONFIRM":
                if (appointment.getStatus() != Appointment.Status.PENDING) {
                    throw new RuntimeException("Chỉ có thể xác nhận cuộc hẹn ở trạng thái PENDING");
                }
                appointment.setStatus(Appointment.Status.CONFIRMED);
                appointmentRepository.save(appointment);
                emailService.sendAppointmentConfirmation(email, customerName, employeeName, timeRange, serviceName);
                break;
            case "CANCEL":
                if (appointment.getStatus() != Appointment.Status.PENDING) {
                    throw new RuntimeException("Chỉ có thể hủy cuộc hẹn ở trạng thái PENDING");
                }
                appointment.setStatus(Appointment.Status.CANCELED);
                appointmentRepository.save(appointment);
                emailService.sendAppointmentCancellation(email, customerName, employeeName, timeRange, serviceName);
                break;
            case "COMPLETE":
                if (appointment.getStatus() != Appointment.Status.CONFIRMED) {
                    throw new RuntimeException("Chỉ có thể đánh dấu hoàn thành cuộc hẹn ở trạng thái CONFIRMED");
                }
                appointment.setStatus(Appointment.Status.COMPLETED);
                appointment.setCompletedAt(LocalDateTime.now());
                appointment.setSalaryCalculated(false); // Ensure salary can be calculated
                appointmentRepository.save(appointment);
                emailService.sendAppointmentCompletion(email, customerName, employeeName, timeRange, serviceName);
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
}