package com.example.serversideclinet.service;

import com.example.serversideclinet.dto.WorkingTimeSlotRequest;
import com.example.serversideclinet.model.Appointment;
import com.example.serversideclinet.model.Employee;
import com.example.serversideclinet.model.Store;
import com.example.serversideclinet.model.WorkingTimeSlot;
import com.example.serversideclinet.repository.AppointmentRepository;
import com.example.serversideclinet.repository.EmployeeRepository;
import com.example.serversideclinet.repository.StoreRepository;
import com.example.serversideclinet.repository.WorkingTimeSlotRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class WorkingTimeSlotService {

    private static final Logger logger = LoggerFactory.getLogger(WorkingTimeSlotService.class);

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private WorkingTimeSlotRepository workingTimeSlotRepository;

    public List<WorkingTimeSlot> getAvailableSlots(Integer employeeId, String date) {
        LocalDate localDate = LocalDate.parse(date);
        LocalDateTime startOfDay = localDate.atStartOfDay();
        LocalDateTime endOfDay = localDate.atTime(23, 59, 59);

        logger.info("Fetching slots for employeeId: {}, date: {}, startOfDay: {}, endOfDay: {}", employeeId, date, startOfDay, endOfDay);

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        logger.info("Found employee: {}", employee.getEmployeeId());

        List<WorkingTimeSlot> workingSlots = workingTimeSlotRepository
                .findByEmployeeAndStartTimeBetween(employee, startOfDay, endOfDay);
        logger.info("Fetched {} working slots: {}", workingSlots.size(), workingSlots);

        LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);
        logger.info("Current time (rounded): {}", now);

        List<WorkingTimeSlot> resultSlots = new ArrayList<>();
        for (WorkingTimeSlot slot : workingSlots) {
            LocalDateTime start = slot.getStartTime();
            LocalDateTime end = slot.getEndTime();
            int intervalMinutes = 60;

            while (start.isBefore(end)) {
                WorkingTimeSlot newSlot = new WorkingTimeSlot();
                newSlot.setEmployee(slot.getEmployee());
                newSlot.setStore(slot.getStore());
                newSlot.setStartTime(start);
                LocalDateTime slotEnd = start.plusMinutes(intervalMinutes);
                newSlot.setEndTime(slotEnd);
                newSlot.setTimeSlotId(slot.getTimeSlotId());

                boolean isPast = slotEnd.isBefore(now) || slotEnd.isEqual(now);
                List<Appointment> appointments = appointmentRepository
                        .findByEmployeeAndTimeOverlap(employee, start, slotEnd.minusNanos(1));
                boolean isBooked = appointments.stream()
                        .anyMatch(apt -> apt.getStatus() != Appointment.Status.CANCELED);
                newSlot.setIsAvailable(!isPast && !isBooked);

                resultSlots.add(newSlot);
                start = start.plusMinutes(intervalMinutes);
            }
        }

        logger.info("Generated {} slots with availability status", resultSlots.size());
        return resultSlots;
    }

    @Transactional
    public WorkingTimeSlot createSlot(WorkingTimeSlotRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        // Sử dụng storeId của nhân viên, bỏ qua storeId từ request nếu có
        Store store = employee.getStore();
        if (store == null) {
            throw new RuntimeException("Nhân viên chưa được gán cửa hàng.");
        }

        LocalDateTime start = request.getStartTime();
        LocalDateTime end = request.getEndTime();

        if (!start.isBefore(end)) {
            throw new RuntimeException("Thời gian bắt đầu phải trước thời gian kết thúc.");
        }

        Duration duration = Duration.between(start, end);
        if (duration.toHours() > 12) {
            throw new RuntimeException("Không thể đăng ký ca làm dài hơn 12 tiếng.");
        }

        LocalTime opening = store.getOpeningTime();
        LocalTime closing = store.getClosingTime();

        if (start.toLocalTime().isBefore(opening) || end.toLocalTime().isAfter(closing)) {
            throw new RuntimeException("Ca làm phải nằm trong khung giờ mở cửa của cửa hàng.");
        }

        boolean isOverlapping = workingTimeSlotRepository
                .existsByEmployeeAndStartTimeLessThanAndEndTimeGreaterThan(
                        employee, end, start);
        if (isOverlapping) {
            throw new RuntimeException("Bạn đã có lịch làm việc trùng với thời gian này.");
        }

        WorkingTimeSlot slot = new WorkingTimeSlot();
        slot.setEmployee(employee);
        slot.setStore(store);
        slot.setStartTime(start);
        slot.setEndTime(end);
        slot.setIsAvailable(true);

        return workingTimeSlotRepository.save(slot);
    }

    public List<WorkingTimeSlot> getMyWorkingSlots() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        List<WorkingTimeSlot> slots = workingTimeSlotRepository.findByEmployeeOrderByStartTimeAsc(employee);
        return slots;
    }
}