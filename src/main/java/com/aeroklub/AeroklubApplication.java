package com.aeroklub;

import com.aeroklub.model.User;
import com.aeroklub.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class AeroklubApplication {
    public static void main(String[] args) { SpringApplication.run(AeroklubApplication.class, args); }

    @Bean
    CommandLineRunner seedAdmin(UserRepository users, PasswordEncoder encoder,
                                @Value("${app.admin.password}") String password) {
        return args -> {
            if (users.existsByUsername("admin")) return;
            User u = new User();
            u.setUsername("admin");
            u.setEmail("admin@aeroklub.local");
            u.setPassword(encoder.encode(password));
            u.setRole(User.Role.ADMIN);
            users.save(u);
        };
    }
}