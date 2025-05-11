package com.example.serversideclinet.service;

import com.example.serversideclinet.model.EmployeeSalaries;
import com.example.serversideclinet.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class EmployeeSalariesService {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeSalariesService.class);

    @Autowired
    private EmployeeSalariesRepository employeeSalariesRepository;

    @Autowired
    private SalaryRuleRepository salaryRuleRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private InvoiceDetailRepository invoiceDetailRepository;

    @Autowired
    private EntityManager entityManager;

    private static final String SALARY_QUERY = """
        SELECT 
            e.employee_id,
            sr.rule_id,
            COALESCE(sr.base_salary, 0) AS base_salary,
            COALESCE(sr.bonus_per_appointment, 0) AS bonus_per_appointment,
            COALESCE(sr.bonus_percentage, 0) AS bonus_percentage,
            COUNT(a.appointment_id) AS completed_appointments,
            COALESCE(SUM(id.unit_price * id.quantity), 0) AS total_revenue
        FROM 
            employees e
            JOIN salary_rules sr ON sr.is_active = TRUE
            LEFT JOIN appointment a ON a.employee_id = e.employee_id 
                AND a.status = 'COMPLETED'
                AND YEAR(a.created_at) = :year
                AND MONTH(a.created_at) = :month
            LEFT JOIN invoice_detail id ON id.appointment_id = a.appointment_id
        WHERE 
            e.is_active = TRUE
            AND sr.effective_date <= CURRENT_TIMESTAMP
        GROUP BY 
            e.employee_id, sr.rule_id, sr.base_salary, sr.bonus_per_appointment, sr.bonus_percentage
        """;

    @Scheduled(cron = "0 0 0 1 * ?") // Run at 00:00 on the 1st of every month
    @Transactional
    public void calculateMonthlySalaries() {
        logger.info("Starting scheduled salary calculation for all employees");
        LocalDate now = LocalDate.now();
        calculateSalariesForMonth(now.getYear(), now.getMonthValue());
    }

    @Transactional
    @SuppressWarnings("unchecked")
    public void calculateSalariesForMonth(int year, int month) {
        logger.info("Calculating salaries for year: {}, month: {}", year, month);

        try {
            // Validate input
            if (year < 2000 || year > 2100 || month < 1 || month > 12) {
                logger.error("Invalid year: {} or month: {}", year, month);
                throw new IllegalArgumentException("Year must be between 2000-2100 and month between 1-12");
            }

            // Check for existing calculations to prevent duplicates
            List<EmployeeSalaries> existingSalaries = employeeSalariesRepository
                    .findBySalaryMonthAndSalaryPeriod(year, month);
            if (!existingSalaries.isEmpty()) {
                logger.warn("Salaries already calculated for year: {}, month: {}", year, month);
                throw new IllegalStateException("Salaries already calculated for this period");
            }

            // Execute native query
            Query query = entityManager.createNativeQuery(SALARY_QUERY);
            query.setParameter("year", year);
            query.setParameter("month", month);
            List<Object[]> results = query.getResultList();

            if (results.isEmpty()) {
                logger.warn("No active employees or salary rules found for year: {}, month: {}", year, month);
                return;
            }

            for (Object[] row : results) {
                Integer employeeId = ((Number) row[0]).intValue();
                Integer ruleId = ((Number) row[1]).intValue();
                BigDecimal baseSalary = new BigDecimal(row[2].toString());
                BigDecimal bonusPerAppointment = new BigDecimal(row[3].toString());
                BigDecimal bonusPercentage = new BigDecimal(row[4].toString());
                long completedAppointments = ((Number) row[5]).longValue();
                BigDecimal totalRevenue = new BigDecimal(row[6].toString());

                // Calculate bonuses
                BigDecimal appointmentBonus = bonusPerAppointment.multiply(BigDecimal.valueOf(completedAppointments));
                BigDecimal revenueBonus = totalRevenue.multiply(
                        bonusPercentage.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                );
                BigDecimal totalBonus = appointmentBonus.add(revenueBonus);
                BigDecimal totalSalary = baseSalary.add(totalBonus);

                // Save to EmployeeSalaries
                EmployeeSalaries salary = new EmployeeSalaries();
                salary.setEmployeeId(employeeId);
                salary.setRuleId(ruleId);
                salary.setBaseSalary(baseSalary);
                salary.setBonus(totalBonus);
                salary.setTotalSalary(totalSalary);
                salary.setSalaryMonth(year);
                salary.setSalaryPeriod(month);
                salary.setCalculatedAt(LocalDateTime.now());

                employeeSalariesRepository.save(salary);
                logger.info("Salary calculated for employeeId: {}, totalSalary: {}", employeeId, totalSalary);
            }

            logger.info("Salary calculation completed for {} employees", results.size());
        } catch (Exception e) {
            logger.error("Error during salary calculation for year: {}, month: {}. Cause: {}", year, month, e.getMessage(), e);
            throw new RuntimeException("Failed to calculate salaries: " + e.getMessage(), e);
        }
    }
}