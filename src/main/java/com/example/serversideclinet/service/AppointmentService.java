package com.example.serversideclinet.service;

import com.example.serversideclinet.dto.AppointmentRequest;
import com.example.serversideclinet.model.*;
import com.example.serversideclinet.repository.*;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

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


    @Transactional
    public Appointment createAppointment(AppointmentRequest request, String userEmail) {
        // Fetch user by email
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + userEmail));

        // Fetch working time slot
        WorkingTimeSlot workingSlot = workingTimeSlotRepository.findById(request.getTimeSlotId())
                .orElseThrow(() -> new IllegalArgumentException("Working time slot not found: " + request.getTimeSlotId()));

        // Fetch store service
        com.example.serversideclinet.model.StoreService storeService = storeServiceRepository.findById(request.getStoreServiceId())
                .orElseThrow(() -> new IllegalArgumentException("Store service not found: " + request.getStoreServiceId()));

        // Validate time slot availability
        LocalDateTime startTime = request.getStartTime();
        LocalDateTime endTime = request.getEndTime();
        if (startTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Cannot book a time slot in the past");
        }

        // Check for overlapping appointments
        Employee employee = workingSlot.getEmployee();
        List<Appointment> overlappingAppointments = appointmentRepository.findByEmployeeAndStartTimeBetween(
                employee, startTime, endTime);
        if (!overlappingAppointments.isEmpty()) {
            throw new IllegalArgumentException("Time slot is already booked");
        }

        // Create appointment
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
        appointment.setReminderSent(false);

        // Save appointment
        Appointment savedAppointment = appointmentRepository.save(appointment);

        // Create invoice
        Invoice invoice = new Invoice();
        invoice.setUser(user);
        invoice.setTotalAmount(storeService.getPrice());
        invoice.setStatus(Invoice.InvoiceStatus.PENDING);
        invoice.setCreatedAt(LocalDateTime.now());
        invoice.getAppointments().add(savedAppointment);
        Invoice savedInvoice = invoiceRepository.save(invoice);

        // Thiết lập mối quan hệ ngược
        savedAppointment.setInvoice(savedInvoice);
        appointmentRepository.save(savedAppointment);

        // Tạo InvoiceDetail
        InvoiceDetail invoiceDetail = new InvoiceDetail();
        invoiceDetail.setInvoice(savedInvoice);
        invoiceDetail.setAppointment(savedAppointment);
        invoiceDetail.setUnitPrice(storeService.getPrice());
        // Gán giá trị từ Appointment nếu cần
        if (savedAppointment.getEmployee() != null) {
            invoiceDetail.setEmployeeId(savedAppointment.getEmployee().getEmployeeId());
        }
        if (savedAppointment.getStoreService() != null) {
            invoiceDetail.setStoreServiceId(savedAppointment.getStoreService().getStoreServiceId());
        }
        invoiceDetailRepository.save(invoiceDetail);

        // Send email notification to employee
        sendAppointmentNotificationToEmployee(savedAppointment);

        return savedAppointment;
    }

    private void sendAppointmentNotificationToEmployee(Appointment appointment) {
        try {
            String employeeEmail = appointment.getEmployee().getEmail();
            if (employeeEmail == null || !employeeEmail.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                logger.error("Invalid employee email: {}", employeeEmail);
                return;
            }
            emailService.sendAppointmentConfirmation(employeeEmail,
                    appointment.getUser().getFullName(),
                    appointment.getEmployee().getFullName(),
                    appointment.getStartTime() + " - " + appointment.getEndTime(),
                    appointment.getStoreService().getService().getServiceName());
            logger.info("Email notification sent successfully to: {}", employeeEmail);
        } catch (Exception e) {
            logger.error("Failed to send email notification: {}", e.getMessage());
        }
    }

    /**
     * Kiểm tra xem thời gian có sẵn cho nhân viên hay không
     */
    private boolean checkTimeAvailability(Employee employee, LocalDateTime startTime, LocalDateTime endTime) {
        // Tìm tất cả các cuộc hẹn của nhân viên trong ngày
        LocalDateTime dayStart = startTime.toLocalDate().atStartOfDay();
        LocalDateTime dayEnd = dayStart.plusDays(1);

        List<Appointment> employeeAppointments = appointmentRepository.findByEmployeeAndStartTimeBetween(
                employee, dayStart, dayEnd);

        // Kiểm tra xem có bị trùng thời gian không
        for (Appointment existingAppointment : employeeAppointments) {
            if (existingAppointment.getStatus() == Appointment.Status.CANCELED) {
                continue;
            }

            LocalDateTime existingStart = existingAppointment.getStartTime();
            LocalDateTime existingEnd = existingAppointment.getEndTime();

            // Kiểm tra xem có overlap không
            boolean overlap = (startTime.isBefore(existingEnd) && endTime.isAfter(existingStart));

            if (overlap) {
                return false;
            }
        }

        return true;
    }

    /**
     * Cập nhật trạng thái khả dụng của time slot dựa trên các cuộc hẹn hiện có
     */
    private void updateTimeSlotAvailability(WorkingTimeSlot timeSlot) {
        long totalAppointmentMinutes = 0;
        long slotDurationMinutes = ChronoUnit.MINUTES.between(timeSlot.getStartTime(), timeSlot.getEndTime());

        for (Appointment appointment : timeSlot.getAppointments()) {
            if (appointment.getStatus() != Appointment.Status.CANCELED) {
                totalAppointmentMinutes += ChronoUnit.MINUTES.between(
                        appointment.getStartTime(), appointment.getEndTime());
            }
        }

        boolean hasTimeAvailable = (slotDurationMinutes - totalAppointmentMinutes) >= 30;
        timeSlot.setIsAvailable(hasTimeAvailable);
    }

    @Transactional
    public void handleSuccessfulPayment(int invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn."));

        if (invoice.getStatus() == Invoice.InvoiceStatus.PAID) {
            return; // Tránh xử lý lại nếu đã thanh toán
        }

        invoice.setStatus(Invoice.InvoiceStatus.PAID);
        invoiceRepository.save(invoice);

        List<InvoiceDetail> details = invoiceDetailRepository.findByInvoiceInvoiceId(invoiceId);
        for (InvoiceDetail detail : details) {
            Appointment appointment = detail.getAppointment();
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

    /**
     * Retrieve an appointment by its ID
     */
    public Appointment getAppointmentById(Integer id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new AppointmentException("Appointment with ID " + id + " not found"));
    }

    /**
     * Update an existing appointment
     */
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

        List<Appointment> overlappingAppointments = appointmentRepository.findByEmployeeAndStartTimeBetween(
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

    /**
     * Delete an appointment
     */
    @Transactional
    public void deleteAppointment(Integer id, String userEmail) {
        Appointment appointment = getAppointmentById(id);
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppointmentException("User not found with email: " + userEmail));

        if (!appointment.getUser().getEmail().equals(userEmail) && !user.getRoles().stream().anyMatch(r -> r.getRoleName().equals("EMPLOYEE"))) {
            throw new AppointmentException("Unauthorized to delete this appointment");
        }

        appointmentRepository.delete(appointment);
    }

    /**
     * Retrieve appointments by user email
     */
    public List<Appointment> getAppointmentsByUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppointmentException("User not found with email: " + email));
        return appointmentRepository.findByUser(user);
    }

    /**
     * Retrieve appointments by employee email
     */
    public List<Appointment> getAppointmentsByEmployee(String email) {
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new AppointmentException("Employee not found with email: " + email));
        return appointmentRepository.findByEmployeeAndStatus(employee, Appointment.Status.PENDING);
    }

    /**
     * Confirm an appointment
     */
    @Transactional
    public Appointment confirmAppointment(Integer id) {
        Appointment appointment = getAppointmentById(id);
        if (appointment.getStatus() != Appointment.Status.PENDING) {
            throw new AppointmentException("Only pending appointments can be confirmed");
        }
        appointment.setStatus(Appointment.Status.CONFIRMED);
        return appointmentRepository.save(appointment);
    }

    /**
     * Cancel an appointment
     */
    @Transactional
    public Appointment cancelAppointment(Integer id, String userEmail) {
        Appointment appointment = getAppointmentById(id);
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppointmentException("User not found with email: " + userEmail));

        if (!appointment.getUser().getEmail().equals(userEmail) && !user.getRoles().stream().anyMatch(r -> r.getRoleName().equals("EMPLOYEE"))) {
            throw new AppointmentException("Unauthorized to cancel this appointment");
        }

        if (appointment.getStatus() == Appointment.Status.COMPLETED) {
            throw new AppointmentException("Completed appointments cannot be canceled");
        }
        appointment.setStatus(Appointment.Status.CANCELED);
        return appointmentRepository.save(appointment);
    }

    public class AppointmentException extends RuntimeException {
        public AppointmentException(String message) {
            super(message);
        }
    }

    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }
}