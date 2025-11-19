package com.slambook.config;

import com.slambook.model.Template;
import com.slambook.model.User;
import com.slambook.repository.TemplateRepository;
import com.slambook.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class InitialDataLoader implements ApplicationRunner {

    private final UserRepository userRepository;
    private final TemplateRepository templateRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        createSuperAdminIfNotExists()
                .then(createDefaultSystemTemplate())
                .subscribe();
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

    private Mono<Template> createDefaultSystemTemplate() {
        log.info("🔧 Checking default system template...");

        return templateRepository.findByCollegeIdIsNull()
                .filter(Template::getIsDefault)
                .filter(Template::getIsActive)
                .next()
                .switchIfEmpty(createSystemTemplate())
                .doOnSuccess(template -> {
                    if (template.getId() != null) {
                        log.info("✅ Default system template exists: {}", template.getName());
                    }
                });
    }

    private Mono<Template> createSystemTemplate() {
        log.info("🔧 Creating default system template...");

        List<Template.Question> questions = List.of(
                Template.Question.builder()
                        .id(UUID.randomUUID().toString())
                        .text("What nickname would you give me?")
                        .type(Template.QuestionType.TEXT)
                        .required(true)
                        .maxLength(50)
                        .placeholder("Enter a cool nickname...")
                        .order(1)
                        .build(),

                Template.Question.builder()
                        .id(UUID.randomUUID().toString())
                        .text("What was your first impression of me?")
                        .type(Template.QuestionType.TEXTAREA)
                        .required(true)
                        .maxLength(500)
                        .placeholder("Share your honest first impression...")
                        .order(2)
                        .build(),

                Template.Question.builder()
                        .id(UUID.randomUUID().toString())
                        .text("What's your favorite memory with me?")
                        .type(Template.QuestionType.TEXTAREA)
                        .required(false)
                        .maxLength(1000)
                        .placeholder("Describe a memorable moment we shared...")
                                        .order(3)
                                        .build(),

                                Template.Question.builder()
                                        .id(UUID.randomUUID().toString())
                                        .text("How would you describe my personality?")
                                        .type(Template.QuestionType.CHOICE)
                                        .required(true)
                                        .options(List.of(
                                                "Friendly & Outgoing",
                                                "Quiet & Thoughtful",
                                                "Funny & Energetic",
                                                "Smart & Analytical",
                                                "Creative & Artistic",
                                                "Kind & Caring"
                                        ))
                                        .order(4)
                                        .build(),

                                Template.Question.builder()
                                        .id(UUID.randomUUID().toString())
                                        .text("Rate our friendship (1-10)")
                                        .type(Template.QuestionType.RATING)
                                        .required(true)
                                        .minValue(1)
                                        .maxValue(10)
                                        .order(5)
                                        .build(),

                                Template.Question.builder()
                                        .id(UUID.randomUUID().toString())
                                        .text("What do you like most about me?")
                                        .type(Template.QuestionType.TEXTAREA)
                                        .required(false)
                                        .maxLength(500)
                                        .placeholder("What makes me special to you?")
                                        .order(6)
                                        .build(),

                                Template.Question.builder()
                                        .id(UUID.randomUUID().toString())
                                        .text("What advice would you give me?")
                                        .type(Template.QuestionType.TEXTAREA)
                                        .required(false)
                                        .maxLength(500)
                                        .placeholder("Share some wisdom or advice...")
                                        .order(7)
                                        .build(),

                                Template.Question.builder()
                                        .id(UUID.randomUUID().toString())
                                        .text("What do you wish for my future?")
                                        .type(Template.QuestionType.TEXTAREA)
                                        .required(false)
                                        .maxLength(500)
                                        .placeholder("Your wishes and hopes for me...")
                                        .order(8)
                                        .build(),

                                Template.Question.builder()
                                        .id(UUID.randomUUID().toString())
                                        .text("If you could say one thing to me, what would it be?")
                                        .type(Template.QuestionType.TEXTAREA)
                                        .required(false)
                                        .maxLength(1000)
                                        .placeholder("Your final message to me...")
                                        .order(9)
                                        .build()
                        );

        Template template = Template.builder()
                .collegeId(null) // System template (global)
                .name("Classic Slam Book")
                .description("The traditional slam book template with heartfelt questions about friendship and memories")
                .questions(questions)
                .isDefault(true)
                .isActive(true)
                .createdBy("SYSTEM")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return templateRepository.save(template)
                .doOnSuccess(t -> log.info("✅ Default system template created: {}", t.getName()));
    }
}