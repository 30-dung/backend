package com.example.serversideclinet.database;

import com.example.serversideclinet.model.Role;
import com.example.serversideclinet.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InsertData {

    @Bean
    public CommandLineRunner loadData(RoleRepository roleRepository) {
        return args -> {
            if (roleRepository.count() == 0) {
                Role admin = new Role();
                admin.setRoleName("ROLE_ADMIN");
                admin.setDescription("ROLE_ADMIN");

                Role customer = new Role();
                customer.setRoleName("ROLE_CUSTOMER");
                customer.setDescription("ROLE_CUSTOMER");

                Role employee = new Role();
                employee.setRoleName("ROLE_EMPLOYEE");
                employee.setDescription("ROLE_EMPLOYEE");

                roleRepository.save(admin);
                roleRepository.save(customer);
                roleRepository.save(employee);

                System.out.println("✅ Đã insert 3 vai trò vào bảng Role");
            } else {
                System.out.println("ℹ️ Dữ liệu Role đã tồn tại, không insert lại.");
            }
        };
    }
}
