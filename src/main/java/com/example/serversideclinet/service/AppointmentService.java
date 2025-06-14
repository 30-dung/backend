package com.example.serversideclinet.service;

import com.example.serversideclinet.dto.AppointmentRequest;
import com.example.serversideclinet.model.*;
import com.example.serversideclinet.repository.*;
import com.example.serversideclinet.util.SlugGenerator;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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

            // NEW: Gửi email thông báo cho nhân viên về cuộc hẹn mới
            sendNewAppointmentNotificationToEmployee(appointment);
        }

        logger.info("Successfully created {} appointments for user: {} - Status: PENDING (waiting for confirmation)",
                createdAppointments.size(), userEmail);

        return createdAppointments;
    }

    // NEW: Method để gửi email thông báo cho nhân viên khi có cuộc hẹn mới
    private void sendNewAppointmentNotificationToEmployee(Appointment appointment) {
        try {
            String employeeEmail = appointment.getEmployee().getEmail();
            if (employeeEmail == null || !employeeEmail.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                logger.error("Invalid employee email: {}", employeeEmail);
                return;
            }

            // Format thời gian đẹp hơn
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            String startTimeFormatted = appointment.getStartTime().format(formatter);
            String endTimeFormatted = appointment.getEndTime().format(formatter);
            String timeRange = startTimeFormatted + " - " + endTimeFormatted;

            // Lấy số điện thoại khách hàng (nếu có)
            String customerPhone = appointment.getUser().getPhoneNumber(); // Assuming User has phoneNumber field

            emailService.sendNewAppointmentNotificationToEmployee(
                    employeeEmail,
                    appointment.getEmployee().getFullName(),
                    appointment.getUser().getFullName(),
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

    // UPDATED: Method now sends confirmation email when appointment is confirmed
    private void sendAppointmentConfirmationToCustomer(Appointment appointment) {
        try {
            String customerEmail = appointment.getUser().getEmail();
            if (customerEmail == null || !customerEmail.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                logger.error("Invalid customer email: {}", customerEmail);
                return;
            }

            // Format thời gian đẹp hơn
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

        if (!appointment.getUser().getEmail().equals(userEmail) && !user.getRoles().stream().anyMatch(r -> r.getRoleName().equals("EMPLOYEE"))) {
            throw new AppointmentException("Unauthorized to delete this appointment");
        }

        appointmentRepository.delete(appointment);
    }

    public List<Appointment> getAppointmentsByUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppointmentException("User not found with email: " + email));
        return appointmentRepository.findByUserOrderByCreatedAtDesc(user);
    }


    public List<Appointment> getAppointmentsByEmployee(String email) {
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new AppointmentException("Employee not found with email: " + email));
        return appointmentRepository.findByEmployeeOrderByCreatedAtDesc(employee);
    }

    // UPDATED: Now sends confirmation email when confirming appointment
    @Transactional
    public Appointment confirmAppointment(Integer id) {
        Appointment appointment = getAppointmentById(id);
        if (appointment.getStatus() != Appointment.Status.PENDING) {
            throw new AppointmentException("Only pending appointments can be confirmed");
        }

        appointment.setStatus(Appointment.Status.CONFIRMED);
        Appointment savedAppointment = appointmentRepository.save(appointment);

        // Gửi email xác nhận cho khách hàng khi nhân viên xác nhận cuộc hẹn
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
        Appointment savedAppointment = appointmentRepository.save(appointment);
        return savedAppointment;
    }

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

    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    public class AppointmentException extends RuntimeException {
        public AppointmentException(String message) {
            super(message);
        }
    }
}