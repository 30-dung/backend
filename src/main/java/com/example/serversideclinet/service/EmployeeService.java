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
        employee.setDateOfBirth(dto.getDateOfBirth());
        employee.setSpecialization(dto.getSpecialization());
        employee.setStore(store);
        employee.setRoles(new HashSet<>(roles));
        employee.setAvatarUrl(dto.getAvatarUrl());

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

        if (appointment.getStatus() != Appointment.Status.PENDING) {
            throw new RuntimeException("Chỉ có thể thay đổi cuộc hẹn ở trạng thái PENDING");
        }

        String timeRange = appointment.getStartTime() + " - " + appointment.getEndTime();
        String customerName = appointment.getUser().getFullName();
        String email = appointment.getUser().getEmail();
        String employeeName = employee.getFullName();
        String serviceName = appointment.getStoreService().getService().getServiceName();

        switch (action.toUpperCase()) {
            case "CONFIRM":
                appointment.setStatus(Appointment.Status.CONFIRMED);
                appointmentRepository.save(appointment);
                emailService.sendAppointmentConfirmation(email, customerName, employeeName, timeRange, serviceName);
                break;
            case "CANCEL":
                appointment.setStatus(Appointment.Status.CANCELED);
                appointmentRepository.save(appointment);
                emailService.sendAppointmentCancellation(email, customerName, employeeName, timeRange, serviceName);
                break;
            default:
                throw new IllegalArgumentException("Hành động không hợp lệ: chỉ chấp nhận CONFIRM hoặc CANCEL");
        }

        return new AppointmentStatusResponseDTO(
                appointment.getAppointmentId(),
                appointment.getStatus().name(),
                appointment.getStartTime().toString(),
                appointment.getEndTime().toString(),
                employeeName,
                customerName,
                serviceName,
                appointment.getNotes()
        );
    }




}
