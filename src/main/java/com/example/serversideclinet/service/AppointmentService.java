package com.example.serversideclinet.service;

import com.example.serversideclinet.dto.AppointmentRequest;
import com.example.serversideclinet.model.*;
import com.example.serversideclinet.repository.*;
import com.example.serversideclinet.util.SlugGenerator;
import org.springframework.transaction.annotation.Transactional;

import com.example.serversideclinet.model.Appointment.Status;
import org.springframework.data.jpa.domain.Specification;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AppointmentService {

    private static final Logger logger = LoggerFactory.getLogger(AppointmentService.class);

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StoreServiceRepository storeServiceRepository;

    @Autowired
    private WorkingTimeSlotRepository workingTimeSlotRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private InvoiceDetailRepository invoiceDetailRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    // Cập nhật phương thức tạo lịch để gửi email thông báo cho nhân viên
    @Transactional
    public List<Appointment> createMultipleAppointments(List<AppointmentRequest> requests, String userEmail) {
        logger.info("Starting createMultipleAppointments for user: {}", userEmail);
        logger.debug("Number of appointment requests: {}", requests.size());

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppointmentException("User not found with email: " + userEmail));
        logger.debug("User found: {}", user.getEmail());

        if (requests == null || requests.isEmpty()) {
            throw new AppointmentException("No appointment requests provided");
        }

        List<Appointment> createdAppointments = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (int i = 0; i < requests.size(); i++) {
            AppointmentRequest request = requests.get(i);
            logger.debug("Validating request {}: timeSlotId={}, storeServiceId={}, startTime={}, endTime={}",
                    i, request.getTimeSlotId(), request.getStoreServiceId(), request.getStartTime(), request.getEndTime());

            WorkingTimeSlot workingSlot = workingTimeSlotRepository.findById(request.getTimeSlotId())
                    .orElseThrow(() -> new AppointmentException("Working time slot not found: " + request.getTimeSlotId()));
            com.example.serversideclinet.model.StoreService storeService = storeServiceRepository.findById(request.getStoreServiceId())
                    .orElseThrow(() -> new AppointmentException("Store service not found: " + request.getStoreServiceId()));

            LocalDateTime startTime = request.getStartTime();
            LocalDateTime endTime = request.getEndTime();
            if (startTime.isBefore(LocalDateTime.now())) {
                logger.error("Request {}: Cannot book a time slot in the past. StartTime: {}", i, startTime);
                throw new AppointmentException("Cannot book a time slot in the past for request " + i);
            }

            Employee employee = workingSlot.getEmployee();
            List<Appointment> overlappingAppointments = appointmentRepository.findByEmployeeAndTimeOverlap(
                    employee, startTime, endTime);
            if (!overlappingAppointments.isEmpty()) {
                logger.error("Request {}: Time slot is already booked. Employee: {}, StartTime: {}, EndTime: {}",
                        i, employee.getEmail(), startTime, endTime);
                throw new AppointmentException("Time slot is already booked for request " + i + " at " + startTime + " - " + endTime);
            }

            Appointment appointment = new Appointment();
            appointment.setUser(user);
            appointment.setWorkingSlot(workingSlot);
            appointment.setEmployee(employee);
            appointment.setStoreService(storeService);
            appointment.setStartTime(startTime);
            appointment.setEndTime(endTime);
            appointment.setNotes(request.getNotes());
            appointment.setStatus(Appointment.Status.PENDING);
            appointment.setCreatedAt(LocalDateTime.now());
            appointment.setSlug(generateUniqueSlug());

            createdAppointments.add(appointment);
            totalAmount = totalAmount.add(storeService.getPrice());
        }

        logger.info("All requests validated successfully. Saving {} appointments.", createdAppointments.size());
        for (Appointment appointment : createdAppointments) {
            appointmentRepository.save(appointment);
        }

        logger.debug("Creating invoice for total amount: {}", totalAmount);
        Invoice invoice = new Invoice();
        invoice.setUser(user);
        invoice.setTotalAmount(totalAmount);
        invoice.setStatus(Invoice.InvoiceStatus.PENDING);
        invoice.setCreatedAt(LocalDateTime.now());
        invoice.setAppointments(createdAppointments);
        Invoice savedInvoice = invoiceRepository.save(invoice);

        for (Appointment appointment : createdAppointments) {
            appointment.setInvoice(savedInvoice);
            appointmentRepository.save(appointment);

            InvoiceDetail invoiceDetail = new InvoiceDetail();
            invoiceDetail.setInvoice(savedInvoice);
            invoiceDetail.setAppointment(appointment);
            invoiceDetail.setUnitPrice(appointment.getStoreService().getPrice());
            if (appointment.getEmployee() != null) {
                invoiceDetail.setEmployeeId(appointment.getEmployee().getEmployeeId());
            }
            if (appointment.getStoreService() != null) {
                invoiceDetail.setStoreServiceId(appointment.getStoreService().getStoreServiceId());
            }
            invoiceDetailRepository.save(invoiceDetail);

            // Gửi email thông báo cho nhân viên về cuộc hẹn mới
            sendNewAppointmentNotificationToEmployee(appointment);
        }

        logger.info("Successfully created {} appointments for user: {} - Status: PENDING (waiting for confirmation)",
                createdAppointments.size(), userEmail);

        return createdAppointments;
    }

    // Method để gửi email thông báo cho nhân viên khi có cuộc hẹn mới
    private void sendNewAppointmentNotificationToEmployee(Appointment appointment) {
        try {
            String employeeEmail = appointment.getEmployee().getEmail();
            if (employeeEmail == null || !employeeEmail.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                logger.error("Invalid employee email: {}", employeeEmail);
                return;
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            String startTimeFormatted = appointment.getStartTime().format(formatter);
            String endTimeFormatted = appointment.getEndTime().format(formatter);
            String timeRange = startTimeFormatted + " - " + endTimeFormatted;

            String customerPhone = appointment.getUser().getPhoneNumber();
            String customerName = appointment.getUser().getFullName(); // Lấy tên khách hàng

            emailService.sendNewAppointmentNotificationToEmployee(
                    employeeEmail,
                    appointment.getEmployee().getFullName(),
                    customerName, // Truyền tên khách hàng
                    timeRange,
                    appointment.getStoreService().getService().getServiceName(),
                    customerPhone);

            logger.info("New appointment notification email sent successfully to employee: {}", employeeEmail);
        } catch (Exception e) {
            logger.error("Failed to send new appointment notification email to employee: {}", e.getMessage());
        }
    }

    private String generateUniqueSlug() {
        String slug;
        do {
            slug = SlugGenerator.generateSlug();
        } while (appointmentRepository.existsBySlug(slug));
        return slug;
    }

    private void sendAppointmentConfirmationToCustomer(Appointment appointment) {
        try {
            String customerEmail = appointment.getUser().getEmail();
            if (customerEmail == null || !customerEmail.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                logger.error("Invalid customer email: {}", customerEmail);
                return;
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            String startTimeFormatted = appointment.getStartTime().format(formatter);
            String endTimeFormatted = appointment.getEndTime().format(formatter);
            String timeRange = startTimeFormatted + " - " + endTimeFormatted;

            emailService.sendAppointmentConfirmation(
                    customerEmail,
                    appointment.getUser().getFullName(),
                    appointment.getEmployee().getFullName(),
                    timeRange,
                    appointment.getStoreService().getService().getServiceName());
            logger.info("Confirmation email sent successfully to customer: {}", customerEmail);
        } catch (Exception e) {
            logger.error("Failed to send confirmation email to customer: {}", e.getMessage());
        }
    }

    // NEW: Method to send rejection notification to customer
    private void sendAppointmentRejectionToCustomer(Appointment appointment, String reason) {
        try {
            String customerEmail = appointment.getUser().getEmail();
            if (customerEmail == null || !customerEmail.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                logger.error("Invalid customer email for rejection: {}", customerEmail);
                return;
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            String startTimeFormatted = appointment.getStartTime().format(formatter);
            String serviceName = appointment.getStoreService().getService().getServiceName();
            String employeeName = appointment.getEmployee().getFullName();

            emailService.sendAppointmentRejection(
                    customerEmail,
                    appointment.getUser().getFullName(),
                    employeeName, // Nhân viên từ chối
                    startTimeFormatted,
                    serviceName,
                    reason);

            logger.info("Rejection email sent successfully to customer: {}", customerEmail);
        } catch (Exception e) {
            logger.error("Failed to send rejection email to customer: {}", e.getMessage());
        }
    }

    // NEW: Method to send reassignment notification to customer
    private void sendAppointmentReassignmentNotificationToCustomer(Appointment appointment, Employee oldEmployee, String newEmployeeName) {
        try {
            String customerEmail = appointment.getUser().getEmail();
            if (customerEmail == null || !customerEmail.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                logger.error("Invalid customer email for reassignment: {}", customerEmail);
                return;
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            String startTimeFormatted = appointment.getStartTime().format(formatter);
            String serviceName = appointment.getStoreService().getService().getServiceName();

            emailService.sendAppointmentReassignment(
                    customerEmail,
                    appointment.getUser().getFullName(),
                    oldEmployee.getFullName(), // Nhân viên cũ
                    newEmployeeName, // Nhân viên mới
                    startTimeFormatted,
                    serviceName
            );

            logger.info("Reassignment email sent successfully to customer: {}", customerEmail);
        } catch (Exception e) {
            logger.error("Failed to send reassignment email to customer: {}", e.getMessage());
        }
    }

    // NEW: Method to send new appointment notification to the reassign employee
    private void sendReassignedAppointmentNotificationToEmployee(Appointment appointment, String oldEmployeeName) {
        try {
            String newEmployeeEmail = appointment.getEmployee().getEmail();
            if (newEmployeeEmail == null || !newEmployeeEmail.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                logger.error("Invalid new employee email for reassignment notification: {}", newEmployeeEmail);
                return;
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            String startTimeFormatted = appointment.getStartTime().format(formatter);
            String endTimeFormatted = appointment.getEndTime().format(formatter);
            String timeRange = startTimeFormatted + " - " + endTimeFormatted;

            String customerPhone = appointment.getUser().getPhoneNumber();
            String customerName = appointment.getUser().getFullName();

            emailService.sendReassignedAppointmentNotificationToEmployee(
                    newEmployeeEmail,
                    appointment.getEmployee().getFullName(), // Tên nhân viên mới
                    customerName,
                    timeRange,
                    appointment.getStoreService().getService().getServiceName(),
                    customerPhone,
                    oldEmployeeName // Tên nhân viên cũ để thông báo
            );

            logger.info("Reassigned appointment notification email sent successfully to new employee: {}", newEmployeeEmail);
        } catch (Exception e) {
            logger.error("Failed to send reassignment email to new employee: {}", e.getMessage());
        }
    }

    @Transactional
    public void handleSuccessfulPayment(int invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn."));

        if (invoice.getStatus() == Invoice.InvoiceStatus.PAID) {
            return;
        }

        invoice.setStatus(Invoice.InvoiceStatus.PAID);
        invoiceRepository.save(invoice);

        List<InvoiceDetail> details = invoiceDetailRepository.findByInvoiceInvoiceId(invoiceId);
        for (InvoiceDetail detail : details) {
            Appointment appointment = detail.getAppointment();
            // Chỉ hoàn thành nếu đang ở trạng thái CONFIRMED
            if (appointment != null && appointment.getStatus() == Appointment.Status.CONFIRMED) {
                appointment.setStatus(Appointment.Status.COMPLETED);
                appointmentRepository.save(appointment);

                User user = appointment.getUser();
                if (user != null) {
                    int currentPoints = user.getLoyaltyPoints() != null ? user.getLoyaltyPoints() : 0;
                    user.setLoyaltyPoints(currentPoints + 1);
                    userService.saveUser(user);
                }
            }
        }
    }

    public Appointment getAppointmentById(Integer id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new AppointmentException("Appointment with ID " + id + " not found"));
    }

    public Appointment getAppointmentBySlug(String slug) {
        return appointmentRepository.findBySlug(slug)
                .orElseThrow(() -> new AppointmentException("Appointment with slug " + slug + " not found"));
    }

    @Transactional
    public Appointment updateAppointment(Integer id, AppointmentRequest request, String userEmail) {
        Appointment appointment = getAppointmentById(id);
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppointmentException("User not found with email: " + userEmail));

        if (!appointment.getUser().getEmail().equals(userEmail) && !user.getRoles().stream().anyMatch(r -> r.getRoleName().equals("EMPLOYEE"))) {
            throw new AppointmentException("Unauthorized to update this appointment");
        }

        WorkingTimeSlot workingSlot = workingTimeSlotRepository.findById(request.getTimeSlotId())
                .orElseThrow(() -> new AppointmentException("Working time slot not found: " + request.getTimeSlotId()));
        com.example.serversideclinet.model.StoreService storeService = storeServiceRepository.findById(request.getStoreServiceId())
                .orElseThrow(() -> new AppointmentException("Store service not found: " + request.getStoreServiceId()));

        LocalDateTime startTime = request.getStartTime();
        LocalDateTime endTime = request.getEndTime();
        if (startTime.isBefore(LocalDateTime.now())) {
            throw new AppointmentException("Cannot update to a time slot in the past");
        }

        List<Appointment> overlappingAppointments = appointmentRepository.findByEmployeeAndTimeOverlap(
                appointment.getEmployee(), startTime, endTime);
        if (!overlappingAppointments.isEmpty() && overlappingAppointments.stream().noneMatch(a -> a.getAppointmentId().equals(id))) {
            throw new AppointmentException("Time slot is already booked");
        }

        appointment.setWorkingSlot(workingSlot);
        appointment.setStoreService(storeService);
        appointment.setStartTime(startTime);
        appointment.setEndTime(endTime);
        appointment.setNotes(request.getNotes());

        return appointmentRepository.save(appointment);
    }

    @Transactional
    public void deleteAppointment(Integer id, String userEmail) {
        Appointment appointment = getAppointmentById(id);
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppointmentException("User not found with email: " + userEmail));

        if (!user.getRoles().stream().anyMatch(r -> r.getRoleName().equals("ADMIN"))) {
            throw new AppointmentException("Only ADMIN can delete appointments physically.");
        }
        appointmentRepository.delete(appointment);
    }

    public List<Appointment> getAppointmentsByUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppointmentException("User not found with email: " + email));
        return appointmentRepository.findByUserOrderByCreatedAtDesc(user);
    }

    // Phương thức getAppointmentsByEmployee (cho ROLE_EMPLOYEE), giờ cũng sử dụng Specification
    @Transactional(readOnly = true)
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"invoice", "storeService.store", "storeService.service", "employee", "user"})
    public List<Appointment> getAppointmentsByEmployee(String employeeEmail, Status status, LocalDateTime startDate, LocalDateTime endDate) {
        Employee employee = employeeRepository.findByEmail(employeeEmail)
                .orElseThrow(() -> new AppointmentException("Employee not found with email: " + employeeEmail));

        Specification<Appointment> spec = Specification.where(null); // Bắt đầu với Specification rỗng

        // Lọc theo nhân viên (bắt buộc cho endpoint này)
        spec = spec.and((root, query, cb) -> cb.equal(root.join("employee").get("employeeId"), employee.getEmployeeId()));

        // ONLY CHANGE START
        // Lọc theo trạng thái
        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        } else {
            // If status is ALL or null, exclude REJECTED by default for employee view
            spec = spec.and((root, query, cb) -> cb.notEqual(root.get("status"), Appointment.Status.REJECTED));
            // Removed default exclusion of CANCELED appointments. Now CANCELED appointments will be shown if 'ALL' is selected.
        }
        // ONLY CHANGE END

        // Lọc theo khoảng thời gian
        if (startDate != null && endDate != null) {
            spec = spec.and((root, query, cb) -> cb.between(root.get("startTime"), startDate, endDate));
        } else if (startDate != null) { // Chỉ có startDate
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("startTime"), startDate));
        } else if (endDate != null) { // Chỉ có endDate
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("startTime"), endDate));
        }

        logger.info("Building Specification for getAppointmentsByEmployee: {}", spec.toString()); // Debug log
        return appointmentRepository.findAll(spec); // Sử dụng findAll(Specification)
    }


    @Transactional
    public Appointment confirmAppointment(Integer id) {
        Appointment appointment = getAppointmentById(id);
        if (appointment.getStatus() != Appointment.Status.PENDING) {
            throw new AppointmentException("Only pending appointments can be confirmed");
        }

        appointment.setStatus(Appointment.Status.CONFIRMED);
        Appointment savedAppointment = appointmentRepository.save(appointment);

        logger.info("Sending confirmation email for appointment ID: {} to customer: {}",
                id, appointment.getUser().getEmail());
        sendAppointmentConfirmationToCustomer(savedAppointment);

        return savedAppointment;
    }

    @Transactional
    public Appointment completeAppoinment (Integer id) {
        Appointment appointment = getAppointmentById(id);
        if (appointment.getStatus() != Appointment.Status.CONFIRMED) {
            throw new AppointmentException("Only confimed appointments can be completed");
        }
        appointment.setStatus(Appointment.Status.COMPLETED);
        appointment.setCompletedAt(LocalDateTime.now());
        Appointment savedAppointment = appointmentRepository.save(appointment);
        return savedAppointment;
    }

    // NEW: Reject Appointment Method
    @Transactional
    public Appointment rejectAppointment(Integer id, String userEmail, String reason) {
        Appointment appointment = getAppointmentById(id);
        Employee employee = employeeRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppointmentException("Employee not found with email: " + userEmail));

        // Chỉ nhân viên được gán lịch đó mới có quyền từ chối
        if (!appointment.getEmployee().getEmployeeId().equals(employee.getEmployeeId())) {
            throw new AppointmentException("Unauthorized to reject this appointment");
        }

        if (appointment.getStatus() != Appointment.Status.PENDING) {
            throw new AppointmentException("Only pending appointments can be rejected");
        }

        appointment.setStatus(Appointment.Status.REJECTED);
        Appointment savedAppointment = appointmentRepository.save(appointment);

        // Gửi email thông báo từ chối cho khách hàng
        sendAppointmentRejectionToCustomer(savedAppointment, reason);

        return savedAppointment;
    }

    // UPDATED: Cancel Appointment Method (for CUSTOMER/EMPLOYEE to cancel)
    @Transactional
    public Appointment cancelAppointment(Integer id, String userEmail) {
        Appointment appointment = getAppointmentById(id);

        User userAsCustomer = userRepository.findByEmail(userEmail).orElse(null);
        Employee userAsEmployee = employeeRepository.findByEmail(userEmail).orElse(null);

        boolean isCustomerOfAppointment = (userAsCustomer != null && appointment.getUser().getEmail().equals(userEmail));
        boolean isEmployeeOfAppointment = (userAsEmployee != null && appointment.getEmployee().getEmail().equals(userEmail));
        boolean isAdmin = !(isCustomerOfAppointment || isEmployeeOfAppointment); // Rất đơn giản hóa, thực tế nên dùng SecurityContext

        if (!isCustomerOfAppointment && !isEmployeeOfAppointment && !isAdmin) {
            throw new AppointmentException("Unauthorized to cancel this appointment");
        }

        if (appointment.getStatus() == Status.COMPLETED ||
                appointment.getStatus() == Status.CANCELED ||
                appointment.getStatus() == Status.REJECTED) {
            throw new AppointmentException("Appointment in status " + appointment.getStatus() + " cannot be canceled.");
        }

        appointment.setStatus(Status.CANCELED);
        Appointment savedAppointment = appointmentRepository.save(appointment);

        String canceledBy = "Unknown";
        if (isCustomerOfAppointment) {
            canceledBy = appointment.getUser().getFullName() + " (Khách hàng)";
        } else if (isEmployeeOfAppointment) {
            canceledBy = appointment.getEmployee().getFullName() + " (Nhân viên)";
        } else {
            canceledBy = "Admin (" + userEmail + ")";
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            String timeRange = appointment.getStartTime().format(formatter) + " - " + appointment.getEndTime().format(formatter);
            emailService.sendAppointmentCancellationToCustomer(
                    appointment.getUser().getEmail(),
                    appointment.getUser().getFullName(),
                    appointment.getEmployee().getFullName(), // Nhân viên được gán lịch
                    timeRange,
                    appointment.getStoreService().getService().getServiceName(),
                    canceledBy
            );
            logger.info("Cancellation email sent successfully to customer: {}", appointment.getUser().getEmail());
        } catch (Exception e) {
            logger.error("Failed to send cancellation email to customer: {}", e.getMessage(), e);
        }

        return savedAppointment;
    }

    // NEW: Reassign Appointment Method (for ADMIN)
    @Transactional
    public Appointment reassignAppointment(Integer appointmentId, Integer newEmployeeId) {
        Appointment appointment = getAppointmentById(appointmentId);
        Employee oldEmployee = appointment.getEmployee();

        if (appointment.getStatus() != Appointment.Status.REJECTED) {
            throw new AppointmentException("Only rejected appointments can be reassigned.");
        }

        Employee newEmployee = employeeRepository.findById(newEmployeeId)
                .orElseThrow(() -> new AppointmentException("New employee not found with ID: " + newEmployeeId));

        if (!oldEmployee.getStore().getStoreId().equals(newEmployee.getStore().getStoreId())) {
            throw new AppointmentException("New employee must be from the same store.");
        }

        List<Appointment> overlappingAppointments = appointmentRepository.findByEmployeeAndTimeOverlap(
                newEmployee, appointment.getStartTime(), appointment.getEndTime());
        if (!overlappingAppointments.isEmpty()) {
            throw new AppointmentException("New employee's schedule overlaps with existing appointments.");
        }

        appointment.setEmployee(newEmployee);
        appointment.setStatus(Appointment.Status.PENDING); // Chuyển về PENDING để nhân viên mới xác nhận
        Appointment savedAppointment = appointmentRepository.save(appointment);

        sendAppointmentReassignmentNotificationToCustomer(savedAppointment, oldEmployee, newEmployee.getFullName());
        sendReassignedAppointmentNotificationToEmployee(savedAppointment, oldEmployee.getFullName());

        return savedAppointment;
    }

    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    public class AppointmentException extends RuntimeException {
        public AppointmentException(String message) {
            super(message);
        }
    }

    // Phương thức filterAppointments cho ADMIN, sử dụng Spring Data JPA Specifications
    @Transactional(readOnly = true) // Đảm bảo chỉ đọc dữ liệu
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"invoice", "storeService.store", "storeService.service", "employee", "user"})
    public List<Appointment> filterAppointments(
            Status status,
            String employeeEmail,
            LocalDateTime startDate,
            LocalDateTime endDate) {

        Specification<Appointment> spec = Specification.where(null); // Bắt đầu với Specification rỗng

        // Lọc theo trạng thái
        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }

        // Lọc theo email nhân viên
        // Chỉ thêm điều kiện nếu employeeEmail không null và không phải "ALL" (để cho phép lọc tất cả)
        if (employeeEmail != null && !employeeEmail.equalsIgnoreCase("ALL")) {
            spec = spec.and((root, query, cb) -> cb.equal(root.join("employee").get("email"), employeeEmail));
        }

        // Lọc theo khoảng thời gian startTime
        // Sử dụng `between` nếu cả startDate và endDate đều có
        if (startDate != null && endDate != null) {
            spec = spec.and((root, query, cb) -> cb.between(root.get("startTime"), startDate, endDate));
        } else if (startDate != null) { // Chỉ có startDate
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("startTime"), startDate));
        } else if (endDate != null) { // Chỉ có endDate
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("startTime"), endDate));
        }

        logger.info("Building Specification for filterAppointments: {}", spec.toString()); // Debug log

        // Thực hiện truy vấn với Specification
        List<Appointment> result = appointmentRepository.findAll(spec);
        logger.info("filterAppointments result size: {}", result.size()); // Debug log
        return result;
    }
}