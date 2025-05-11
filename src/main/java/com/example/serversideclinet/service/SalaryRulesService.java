package com.example.serversideclinet.service;

import com.example.serversideclinet.model.SalaryRules;
import com.example.serversideclinet.repository.SalaryRulesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SalaryRulesService {
    @Autowired
    private SalaryRulesRepository salaryRulesRepository;

    // Lấy danh sách tất cả quy tắc lương
    public List<SalaryRules> findAll() {
        return salaryRulesRepository.findAll();
    }

    // Tìm quy tắc lương theo ID
    public SalaryRules findById(int id) {
        return salaryRulesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("SalaryRules not found with id: " + id));
    }

    // Thêm quy tắc lương mới
    public SalaryRules addSalaryRules(SalaryRules salaryRules) {
        return salaryRulesRepository.save(salaryRules);
    }

    // Cập nhật quy tắc lương
    public SalaryRules updateSalaryRules(int id, SalaryRules updatedRules) {
        SalaryRules existingRules = salaryRulesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("SalaryRules not found with id: " + id));

        // Cập nhật các thuộc tính
        existingRules.setDescription(updatedRules.getDescription());
        existingRules.setBaseSalary(updatedRules.getBaseSalary());
        existingRules.setBonusPerAppointment(updatedRules.getBonusPerAppointment());
        existingRules.setBonusPercentage(updatedRules.getBonusPercentage());
        existingRules.setEffectiveDate(updatedRules.getEffectiveDate());
        existingRules.setActive(updatedRules.getActive());

        return salaryRulesRepository.save(existingRules);
    }

    // Xóa quy tắc lương
    public void deleteSalaryRules(int id) {
        SalaryRules existingRules = salaryRulesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("SalaryRules not found with id: " + id));
        salaryRulesRepository.delete(existingRules);
    }
}