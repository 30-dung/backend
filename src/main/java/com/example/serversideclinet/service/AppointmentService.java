package com.example.serversideclinet.service;

import com.example.serversideclinet.dto.AppointmentRequest;
import com.example.serversideclinet.model.*;
import com.example.serversideclinet.model.StoreService;
import com.example.serversideclinet.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private WorkingTimeSlotRepository workingTimeSlotRepository;

    @Autowired
    private StoreServiceRepository storeServiceRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private InvoiceDetailRepository invoiceDetailRepository;

    @Autowired
    private JavaMailSender emailSender;

    @Transactional
    public Appointment createAppointment(AppointmentRequest request, String userEmail) {
        if (request.getTimeSlotId() == null) {
            throw new AppointmentException("Time slot ID không được để trống.");
        }

        if (request.getStoreServiceId() == null) {
            throw new AppointmentException("StoreService ID không được để trống.");
        }

        // Lấy user
        User user = userService.findByEmail(userEmail)
                .orElseThrow(() -> new AppointmentException("Không tìm thấy người dùng."));

        // Lấy time slot
        WorkingTimeSlot timeSlot = workingTimeSlotRepository.findById(request.getTimeSlotId())
                .orElseThrow(() -> new AppointmentException("Không tìm thấy khung giờ làm việc."));

        if (!timeSlot.getIsAvailable()) {
            throw new AppointmentException("Khung giờ không có sẵn.");
        }

        // Lấy employee và store service
        Employee employee = timeSlot.getEmployee();
        StoreService storeService = storeServiceRepository.findById(request.getStoreServiceId())
                .orElseThrow(() -> new AppointmentException("Không tìm thấy dịch vụ."));

        // Tính thời gian bắt đầu và kết thúc dịch vụ
        Short serviceDuration = storeService.getService().getDurationMinutes();
        LocalDateTime startTime = request.getStartTime() != null ?
                request.getStartTime() :
                timeSlot.getStartTime();
        LocalDateTime endTime = startTime.plusMinutes(serviceDuration);

        // Kiểm tra thời gian hợp lệ
        if (startTime.isBefore(timeSlot.getStartTime()) || endTime.isAfter(timeSlot.getEndTime())) {
            throw new AppointmentException("Thời gian đặt lịch nằm ngoài khung giờ làm việc.");
        }

        // Kiểm tra xem thời gian này đã được đặt chưa
        boolean isTimeAvailable = checkTimeAvailability(employee, startTime, endTime);
        if (!isTimeAvailable) {
            throw new AppointmentException("Thời gian này đã được đặt, vui lòng chọn thời gian khác.");
        }

        // Tạo appointment
        Appointment appointment = new Appointment();
        appointment.setUser(user);
        appointment.setStoreService(storeService);
        appointment.setEmployee(employee);
        appointment.setWorkingSlot(timeSlot);
        appointment.setStartTime(startTime);
        appointment.setEndTime(endTime);
        appointment.setStatus(Appointment.Status.PENDING);
        appointment.setNotes(request.getNotes());

        // Lưu appointment trước
        appointment = appointmentRepository.save(appointment);

        // Thêm appointment vào working slot và cập nhật trạng thái
        // Không lưu trực tiếp qua timeSlot để tránh vòng lặp
        timeSlot.getAppointments().add(appointment);
        updateTimeSlotAvailability(timeSlot);
        workingTimeSlotRepository.save(timeSlot);

        // Tạo hóa đơn nếu chưa có
        Invoice invoice = invoiceRepository
                .findFirstByUserAndStatus(user, InvoiceStatus.PENDING)
                .orElseGet(() -> {
                    Invoice newInvoice = new Invoice();
                    newInvoice.setUser(user);
                    newInvoice.setStatus(InvoiceStatus.PENDING);
                    newInvoice.setCreatedAt(LocalDateTime.now());
                    newInvoice.setTotalAmount(BigDecimal.ZERO); // Khởi tạo với 0
                    return invoiceRepository.save(newInvoice);
                });

        // Tạo và lưu chi tiết hóa đơn
        InvoiceDetail invoiceDetail = new InvoiceDetail();
        invoiceDetail.setInvoice(invoice);
        invoiceDetail.setAppointment(appointment);
        invoiceDetail.setEmployee(employee);
        invoiceDetail.setStoreService(storeService);
        invoiceDetail.setUnitPrice(storeService.getPrice());
        invoiceDetail.setQuantity(1);
        invoiceDetail.setDescription(storeService.getService().getServiceName());
        invoiceDetailRepository.save(invoiceDetail);

        // Cập nhật tổng tiền hóa đơn
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (InvoiceDetail detail : invoiceDetailRepository.findByInvoiceId(invoice.getInvoiceId())) {
            totalAmount = totalAmount.add(detail.getUnitPrice().multiply(new BigDecimal(detail.getQuantity())));
        }
        invoice.setTotalAmount(totalAmount);
        invoiceRepository.save(invoice);

        // Gửi email thông báo cho nhân viên
        sendAppointmentNotificationToEmployee(appointment);

        return appointment;
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
            // Nếu cuộc hẹn đã bị hủy, bỏ qua
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
        // Tính tổng thời gian của các cuộc hẹn (không bị hủy)
        long totalAppointmentMinutes = 0;
        long slotDurationMinutes = ChronoUnit.MINUTES.between(timeSlot.getStartTime(), timeSlot.getEndTime());

        for (Appointment appointment : timeSlot.getAppointments()) {
            if (appointment.getStatus() != Appointment.Status.CANCELED) {
                totalAppointmentMinutes += ChronoUnit.MINUTES.between(
                        appointment.getStartTime(), appointment.getEndTime());
            }
        }

        // Nếu thời gian còn lại ít hơn 30 phút, đánh dấu slot là không có sẵn
        boolean hasTimeAvailable = (slotDurationMinutes - totalAppointmentMinutes) >= 30;
        timeSlot.setIsAvailable(hasTimeAvailable);
    }

    /**
     * Gửi email thông báo đến nhân viên khi có cuộc hẹn mới
     */
    private void sendAppointmentNotificationToEmployee(Appointment appointment) {
        try {
            Employee employee = appointment.getEmployee();
            User customer = appointment.getUser();
            StoreService service = appointment.getStoreService();

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(employee.getEmail());
            message.setSubject("Thông báo: Có lịch hẹn mới");

            String emailContent = String.format(
                    "Xin chào %s,\n\n" +
                            "Bạn có một cuộc hẹn mới:\n" +
                            "- Khách hàng: %s\n" +
                            "- Dịch vụ: %s\n" +
                            "- Thời gian bắt đầu: %s\n" +
                            "- Thời gian kết thúc: %s\n" +
                            "- Ghi chú: %s\n\n" +
                            "Vui lòng chuẩn bị trước để phục vụ khách hàng tốt nhất.\n\n" +
                            "Trân trọng,\n" +
                            "Hệ thống quản lý",
                    employee.getFullName(),
                    customer.getFullName(),
                    service.getService().getServiceName(),
                    appointment.getStartTime().toString(),
                    appointment.getEndTime().toString(),
                    appointment.getNotes() != null ? appointment.getNotes() : "Không có"
            );

            message.setText(emailContent);
            emailSender.send(message);
        } catch (Exception e) {
            // Log lỗi nhưng không dừng quy trình đặt lịch
            System.err.println("Không thể gửi email thông báo: " + e.getMessage());
        }
    }
    @Transactional
    public void handleSuccessfulPayment(int invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn."));

        if (invoice.getStatus() == InvoiceStatus.PAID) {
            return; // Tránh xử lý lại nếu đã thanh toán
        }

        invoice.setStatus(InvoiceStatus.PAID);
        invoiceRepository.save(invoice);

        // Cập nhật trạng thái tất cả appointment liên quan
        List<InvoiceDetail> details = invoiceDetailRepository.findByInvoiceId(invoiceId);
        for (InvoiceDetail detail : details) {
            Appointment appointment = detail.getAppointment();
            if (appointment != null && appointment.getStatus() == Appointment.Status.CONFIRMED) {
                appointment.setStatus(Appointment.Status.COMPLETED);
                appointmentRepository.save(appointment);

                // Cộng điểm thưởng cho khách hàng
                User user = appointment.getUser();
                if (user != null) {
                    int currentPoints = user.getLoyaltyPoints() != null ? user.getLoyaltyPoints() : 0;
                    user.setLoyaltyPoints(currentPoints + 1);
                    userService.saveUser(user);
                }
            }
        }
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