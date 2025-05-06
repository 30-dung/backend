package com.example.serversideclinet.service;

import com.example.serversideclinet.dto.WorkingTimeSlotRequest;
import com.example.serversideclinet.model.AppointmentTimeSlot;
import com.example.serversideclinet.model.Employee;
import com.example.serversideclinet.model.Store;
import com.example.serversideclinet.model.WorkingTimeSlot;
import com.example.serversideclinet.repository.AppointmentTimeSlotRepository;
import com.example.serversideclinet.repository.EmployeeRepository;
import com.example.serversideclinet.repository.StoreRepository;
import com.example.serversideclinet.repository.WorkingTimeSlotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class WorkingTimeSlotService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private WorkingTimeSlotRepository workingTimeSlotRepository;

    @Autowired
    private AppointmentTimeSlotRepository appointmentTimeSlotRepository;

    @Autowired
    private AppointmentTimeSlotService appointmentTimeSlotService;

    @Transactional
    public WorkingTimeSlot createSlot(WorkingTimeSlotRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        // Lấy thông tin nhân viên từ email
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên."));

        // Kiểm tra storeId có đúng với nơi làm việc không
        if (!employee.getStore().getStoreId().equals(request.getStoreId())) {
            throw new RuntimeException("Bạn không thể đăng ký thời gian làm việc cho cửa hàng khác nơi làm việc.");
        }

        // Lấy cửa hàng
        Store store = storeRepository.findById(request.getStoreId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy cửa hàng."));

        LocalDateTime start = request.getStartTime();
        LocalDateTime end = request.getEndTime();

        validateWorkingTimeSlot(start, end, store);

        // Kiểm tra trùng lịch đã đăng ký
        boolean isOverlapping = workingTimeSlotRepository
                .existsByEmployeeAndStartTimeLessThanAndEndTimeGreaterThan(
                        employee, end, start);
        if (isOverlapping) {
            throw new RuntimeException("Bạn đã có lịch làm việc trùng với thời gian này.");
        }

        // Lưu lịch làm việc
        WorkingTimeSlot slot = new WorkingTimeSlot();
        slot.setEmployee(employee);
        slot.setStore(store);
        slot.setStartTime(start);
        slot.setEndTime(end);
        slot.setIsActive(true);

        slot = workingTimeSlotRepository.save(slot);

        return slot;
    }

    private void validateWorkingTimeSlot(LocalDateTime start, LocalDateTime end, Store store) {
        if (!start.isBefore(end)) {
            throw new RuntimeException("Thời gian bắt đầu phải trước thời gian kết thúc.");
        }

        // Kiểm tra ca làm không quá 12 tiếng
        Duration duration = Duration.between(start, end);
        if (duration.toHours() > 12) {
            throw new RuntimeException("Không thể đăng ký ca làm dài hơn 12 tiếng.");
        }

        // Kiểm tra nằm trong khung giờ mở cửa của store
        LocalTime opening = store.getOpeningTime();
        LocalTime closing = store.getClosingTime();

        if (start.toLocalTime().isBefore(opening) || end.toLocalTime().isAfter(closing)) {
            throw new RuntimeException("Ca làm phải nằm trong khung giờ mở cửa của cửa hàng.");
        }
    }

    public List<WorkingTimeSlot> getMyWorkingSlots() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên."));

        return workingTimeSlotRepository.findByEmployeeOrderByStartTimeAsc(employee);
    }

    public List<WorkingTimeSlot> getAvailableWorkingSlots(Integer storeId, LocalDateTime fromDate, LocalDateTime toDate) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy cửa hàng."));

        return workingTimeSlotRepository
                .findByStoreAndIsActiveAndStartTimeGreaterThanEqualAndEndTimeLessThanEqual(
                        store, true, fromDate, toDate);
    }

    @Transactional
    public WorkingTimeSlot updateWorkingTimeSlotStatus(Integer timeSlotId, boolean isActive) {
        WorkingTimeSlot slot = workingTimeSlotRepository.findById(timeSlotId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khung giờ làm việc."));

        // Kiểm tra xem có thể vô hiệu hóa không
        if (!isActive) {
            boolean hasBookedAppointments = slot.getAppointmentTimeSlots().stream()
                    .anyMatch(AppointmentTimeSlot::getIsBooked);

            if (hasBookedAppointments) {
                throw new RuntimeException("Không thể vô hiệu hóa khung giờ làm việc vì đã có lịch hẹn.");
            }
        }

        slot.setIsActive(isActive);
        return workingTimeSlotRepository.save(slot);
    }

    @Transactional
    public void deleteWorkingTimeSlot(Integer timeSlotId) {
        WorkingTimeSlot slot = workingTimeSlotRepository.findById(timeSlotId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khung giờ làm việc."));

        // Kiểm tra xem có thể xóa không
        boolean hasBookedAppointments = slot.getAppointmentTimeSlots().stream()
                .anyMatch(AppointmentTimeSlot::getIsBooked);

        if (hasBookedAppointments) {
            throw new RuntimeException("Không thể xóa khung giờ làm việc vì đã có lịch hẹn.");
        }

        // Xóa tất cả AppointmentTimeSlot không được đặt
        appointmentTimeSlotRepository.deleteByWorkingTimeSlotAndIsBookedFalse(slot);

        // Xóa WorkingTimeSlot
        workingTimeSlotRepository.delete(slot);
    }
}