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

@Service
public class WorkingTimeSlotService {
    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private WorkingTimeSlotRepository workingTimeSlotRepository;

    @Transactional
    public WorkingTimeSlot createSlot(WorkingTimeSlotRequest request){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        Store store = storeRepository.findById(request.getStoreId())
                .orElseThrow(() -> new RuntimeException("Store not found"));
        WorkingTimeSlot slot = new WorkingTimeSlot();
        slot.setEmployee(employee);
        slot.setStore(store);
        slot.setStartTime(request.getStartTime());
        slot.setEndTime(request.getEndTime());
        slot.setIsAvailable(true);

        return workingTimeSlotRepository.save(slot);
    }
}
