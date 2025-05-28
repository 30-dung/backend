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
import java.util.stream.Collectors;

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

        List<WorkingTimeSlot> slots = workingTimeSlotRepository
                .findByEmployeeAndStartTimeBetween(employee, startOfDay, endOfDay);
        logger.info("Fetched {} slots: {}", slots.size(), slots);

        LocalDateTime now = LocalDateTime.now();
        logger.info("Current time: {}", now);

        List<WorkingTimeSlot> dividedSlots = new ArrayList<>();
        for (WorkingTimeSlot slot : slots) {
            if (slot.getIsAvailable()) {
                LocalDateTime current = LocalDateTime.of(localDate, LocalTime.of(7, 0)); // Bắt đầu từ 07:00
                LocalDateTime end = LocalDateTime.of(localDate, LocalTime.of(22, 0));   // Kết thúc tại 22:00
                int intervalMinutes = 60; // 1-hour intervals

                while (current.plusMinutes(intervalMinutes).isBefore(end) || current.plusMinutes(intervalMinutes).isEqual(end)) {
                    WorkingTimeSlot newSlot = new WorkingTimeSlot();
                    newSlot.setEmployee(slot.getEmployee());
                    newSlot.setStore(slot.getStore());
                    newSlot.setStartTime(current);
                    newSlot.setEndTime(current.plusMinutes(intervalMinutes));
                    newSlot.setIsAvailable(true);
                    newSlot.setTimeSlotId(slot.getTimeSlotId());

                    // Kiểm tra xung đột lịch hẹn
                    List<Appointment> appointments = appointmentRepository
                            .findByEmployeeAndStartTimeBetween(employee, newSlot.getStartTime(), newSlot.getEndTime());
                    logger.info("Slot {} - Start: {}, Appointments found: {}", newSlot.getTimeSlotId(), newSlot.getStartTime(), appointments.size());
                    boolean noConflict = appointments.stream()
                            .noneMatch(apt -> apt.getStatus() != Appointment.Status.CANCELED);
                    logger.info("Slot {} - No conflicting appointments: {}", newSlot.getTimeSlotId(), noConflict);
                    if (noConflict) {
                        dividedSlots.add(newSlot);
                    }
                    current = current.plusMinutes(intervalMinutes);
                }
            }
        }
        logger.info("Divided into {} slots", dividedSlots.size());

        return dividedSlots;
    }

    @Transactional
    public WorkingTimeSlot createSlot(WorkingTimeSlotRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        if (!employee.getStore().getStoreId().equals(request.getStoreId())) {
            throw new RuntimeException("Bạn không thể đăng ký thời gian làm việc cho cửa hàng khác nơi làm việc.");
        }

        Store store = storeRepository.findById(request.getStoreId())
                .orElseThrow(() -> new RuntimeException("Store not found"));

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

        return workingTimeSlotRepository.findByEmployeeOrderByStartTimeAsc(employee);
    }
}