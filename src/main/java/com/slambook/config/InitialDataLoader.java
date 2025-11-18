package com.slambook.config;

import com.slambook.model.User;
import com.slambook.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class InitialDataLoader implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        createSuperAdminIfNotExists().subscribe();
    }

    private Mono<User> createSuperAdminIfNotExists() {
        String email = "admin@slambook.com";

        return userRepository.findByEmail(email)
                .switchIfEmpty(createSuperAdmin())
                .doOnSuccess(user -> {
                    if (user.getId() != null) {
                        log.info("✅ Super Admin exists: {}", email);
                    }
                });
    }

    private Mono<User> createSuperAdmin() {
        log.info("🔧 Creating Super Admin...");

        User superAdmin = User.builder()
                .email("admin@slambook.com")
                .password(passwordEncoder.encode("Admin@123"))
                .role(User.UserRole.SUPER_ADMIN)
                .profile(User.UserProfile.builder()
                        .firstName("Super")
                        .lastName("Admin")
                        .build())
                .emailVerified(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isActive(true)
                .build();

        return userRepository.save(superAdmin)
                .doOnSuccess(user -> log.info("✅ Super Admin created successfully!"));
    }
}