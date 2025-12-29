package com.project.medinova.config;

import com.project.medinova.entity.User;
import com.project.medinova.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Tạo tài khoản admin nếu chưa tồn tại
        createAdminUser();
    }

    private void createAdminUser() {
        String adminEmail = "admin@gmail.com";
        String adminPassword = "admin";

        // Kiểm tra xem admin đã tồn tại chưa
        if (userRepository.existsByEmail(adminEmail)) {
            logger.info("Admin user already exists. Skipping creation.");
            return;
        }

        // Tạo tài khoản admin mới
        User admin = new User();
        admin.setEmail(adminEmail);
        admin.setPasswordHash(passwordEncoder.encode(adminPassword));
        admin.setFullName("System Administrator");
        admin.setRole("ADMIN");
        admin.setStatus("ACTIVE");

        userRepository.save(admin);
        logger.info("Admin user created successfully. Email: {}, Password: {}", adminEmail, adminPassword);
    }
}

