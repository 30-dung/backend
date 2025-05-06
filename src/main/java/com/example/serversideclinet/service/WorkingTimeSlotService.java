package com.example.serversideclinet.service;

import com.example.serversideclinet.dto.WorkingTimeSlotRequest;
import com.example.serversideclinet.model.Employee;
import com.example.serversideclinet.model.Store;
import com.example.serversideclinet.model.WorkingTimeSlot;
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

@Service
public class WorkingTimeSlotService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private WorkingTimeSlotRepository workingTimeSlotRepository;

    @Transactional
    public WorkingTimeSlot createSlot(WorkingTimeSlotRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        // Lấy thông tin nhân viên từ email
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        // Kiểm tra storeId có đúng với nơi làm việc không
        if (!employee.getStore().getStoreId().equals(request.getStoreId())) {
            throw new RuntimeException("Bạn không thể đăng ký thời gian làm việc cho cửa hàng khác nơi làm việc.");
        }

        // Lấy cửa hàng
        Store store = storeRepository.findById(request.getStoreId())
                .orElseThrow(() -> new RuntimeException("Store not found"));

        LocalDateTime start = request.getStartTime();
        LocalDateTime end = request.getEndTime();

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

        return workingTimeSlotRepository.save(slot);
    }
    public List<WorkingTimeSlot> getMyWorkingSlots() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        return workingTimeSlotRepository.findByEmployeeOrderByStartTimeAsc(employee);
    }

}
