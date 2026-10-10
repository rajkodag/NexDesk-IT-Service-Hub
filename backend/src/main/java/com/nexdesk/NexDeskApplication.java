package com.nexdesk;

import com.nexdesk.entity.Technician;
import com.nexdesk.entity.User;
import com.nexdesk.repository.TechnicianRepository;
import com.nexdesk.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class NexDeskApplication {

    private static final Logger log = LoggerFactory.getLogger(NexDeskApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(NexDeskApplication.class, args);
        log.info("==================================================================");
        log.info("  NexDesk Enterprise IT Service Management Platform Started!     ");
        log.info("  Web UI:       http://localhost:8080/                            ");
        log.info("  Swagger Docs: http://localhost:8080/swagger-ui/index.html       ");
        log.info("==================================================================");
    }

    @Bean
    public CommandLineRunner initDatabase(UserRepository userRepository,
                                          TechnicianRepository technicianRepository,
                                          PasswordEncoder passwordEncoder) {
        return args -> {
            // Seed Admin user if missing or update with strong enterprise password
            if (!userRepository.existsByEmail("admin@nexdesk.com")) {
                User admin = new User(
                        "System Administrator",
                        "admin@nexdesk.com",
                        passwordEncoder.encode("Admin@2026!"),
                        "ADMIN"
                );
                userRepository.save(admin);
                log.info("Seeded default Administrator account: admin@nexdesk.com (Password: Admin@2026!)");
            }

            // Seed sample technicians if missing
            if (!technicianRepository.existsByEmail("alex@nexdesk.com")) {
                Technician alex = new Technician(
                        "Alex Technician",
                        "alex@nexdesk.com",
                        "Hardware & Infrastructure",
                        "AVAILABLE"
                );
                technicianRepository.save(alex);
                log.info("Seeded default Technician: Alex Technician");
            }

            if (!technicianRepository.existsByEmail("priya@nexdesk.com")) {
                Technician priya = new Technician(
                        "Priya Technician",
                        "priya@nexdesk.com",
                        "Network & Cloud Security",
                        "AVAILABLE"
                );
                technicianRepository.save(priya);
                log.info("Seeded default Technician: Priya Technician");
            }
        };
    }
}
