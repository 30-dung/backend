package com.example.serversideclinet.service;

import com.example.serversideclinet.model.AppointmentTimeSlot;
import com.example.serversideclinet.model.WorkingTimeSlot;
import com.example.serversideclinet.repository.AppointmentTimeSlotRepository;
import com.example.serversideclinet.repository.WorkingTimeSlotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AppointmentTimeSlotService {

    @Autowired
    private AppointmentTimeSlotRepository appointmentTimeSlotRepository;

    @Autowired
    private WorkingTimeSlotRepository workingTimeSlotRepository;

    /**
     * Tìm các khung giờ hẹn có sẵn trong một khung giờ làm việc cụ thể
     */
    public List<AppointmentTimeSlot> findAvailableSlotsByWorkingTimeSlot(Integer workingTimeSlotId) {
        WorkingTimeSlot workingTimeSlot = workingTimeSlotRepository.findById(workingTimeSlotId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khung giờ làm việc."));

        return appointmentTimeSlotRepository.findByWorkingTimeSlotAndIsBookedFalse(workingTimeSlot);
    }

    /**
     * Tìm tất cả các khung giờ hẹn có sẵn trong khoảng thời gian
     */
    public List<AppointmentTimeSlot> findAvailableSlotsByTimeRange(LocalDateTime start, LocalDateTime end) {
        return appointmentTimeSlotRepository.findByStartTimeGreaterThanEqualAndEndTimeLessThanEqualAndIsBookedFalse(start, end);
    }

    /**
     * Tạo sẵn nhiều khung giờ hẹn cho một khung giờ làm việc
     * với độ dài mỗi slot là slotDurationMinutes phút
     */
    @Transactional
    public List<AppointmentTimeSlot> generateTimeSlotsForWorkingSlot(Integer workingTimeSlotId, int slotDurationMinutes) {
        WorkingTimeSlot workingTimeSlot = workingTimeSlotRepository.findById(workingTimeSlotId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khung giờ làm việc."));

        // Xóa các slot cũ chưa được đặt (nếu có)
        appointmentTimeSlotRepository.deleteByWorkingTimeSlotAndIsBookedFalse(workingTimeSlot);

        LocalDateTime current = workingTimeSlot.getStartTime();
        LocalDateTime end = workingTimeSlot.getEndTime();

        while (current.plusMinutes(slotDurationMinutes).isBefore(end) ||
                current.plusMinutes(slotDurationMinutes).isEqual(end)) {
            LocalDateTime slotEnd = current.plusMinutes(slotDurationMinutes);

            // Kiểm tra xem slot này có trùng với slot đã đặt không
            if (workingTimeSlot.checkAvailability(current, slotEnd)) {
                AppointmentTimeSlot slot = new AppointmentTimeSlot();
                slot.setWorkingTimeSlot(workingTimeSlot);
                slot.setStartTime(current);
                slot.setEndTime(slotEnd);
                slot.setIsBooked(false);
                appointmentTimeSlotRepository.save(slot);
            }

            current = slotEnd;
        }

        return appointmentTimeSlotRepository.findByWorkingTimeSlotAndIsBookedFalse(workingTimeSlot);
    }

    /**
     * Đánh dấu một khung giờ hẹn đã được đặt
     */
    @Transactional
    public AppointmentTimeSlot bookTimeSlot(Integer appointmentTimeSlotId) {
        AppointmentTimeSlot slot = appointmentTimeSlotRepository.findById(appointmentTimeSlotId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khung giờ hẹn."));

        if (slot.getIsBooked()) {
            throw new RuntimeException("Khung giờ này đã được đặt.");
        }

        slot.setIsBooked(true);
        return appointmentTimeSlotRepository.save(slot);
    }

    /**
     * Đánh dấu một khung giờ hẹn đã được giải phóng (khi hủy lịch)
     */
    @Transactional
    public AppointmentTimeSlot releaseTimeSlot(Integer appointmentTimeSlotId) {
        AppointmentTimeSlot slot = appointmentTimeSlotRepository.findById(appointmentTimeSlotId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khung giờ hẹn."));

        slot.setIsBooked(false);
        slot.setAppointment(null); // Ngắt liên kết với Appointment
        return appointmentTimeSlotRepository.save(slot);
    }
}