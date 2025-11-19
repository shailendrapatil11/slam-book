package com.slambook.service;

import com.slambook.dto.request.TemplateCreateRequest;
import com.slambook.dto.request.TemplateQuestionRequest;
import com.slambook.dto.response.TemplateResponse;
import com.slambook.exception.BadRequestException;
import com.slambook.exception.ForbiddenException;
import com.slambook.exception.NotFoundException;
import com.slambook.model.Template;
import com.slambook.repository.TemplateRepository;
import com.slambook.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TemplateService {

    private final TemplateRepository templateRepository;
    private final UserService userService;

    /**
     * Get system templates (available to all colleges)
     */
    public Flux<TemplateResponse> getSystemTemplates() {
        return templateRepository.findByCollegeIdIsNullAndIsActive(true)
                .map(this::mapToTemplateResponse);
    }

    /**
     * Get college-specific templates
     */
    public Flux<TemplateResponse> getCollegeTemplates(CustomUserDetails userDetails) {
        return templateRepository.findByCollegeIdAndIsActive(userDetails.getCollegeId(), true)
                .map(this::mapToTemplateResponse);
    }

    /**
     * Get all available templates (system + college)
     */
    public Flux<TemplateResponse> getAllAvailableTemplates(CustomUserDetails userDetails) {
        Flux<Template> systemTemplates = templateRepository.findByCollegeIdIsNullAndIsActive(true);
        Flux<Template> collegeTemplates = templateRepository.findByCollegeIdAndIsActive(userDetails.getCollegeId(), true);

        return Flux.concat(systemTemplates, collegeTemplates)
                .map(this::mapToTemplateResponse);
    }

    /**
     * Get template by ID
     */
    public Mono<TemplateResponse> getTemplateById(String templateId, CustomUserDetails userDetails) {
        return templateRepository.findById(templateId)
                .switchIfEmpty(Mono.error(new NotFoundException("Template not found")))
                .flatMap(template -> {
                    // Check access: system templates or own college templates
                    if (template.getCollegeId() != null &&
                            !template.getCollegeId().equals(userDetails.getCollegeId()) &&
                            !"SUPER_ADMIN".equals(userDetails.getRole())) {
                        return Mono.error(new ForbiddenException("Cannot access template from another college"));
                    }
                    return Mono.just(template);
                })
                .flatMap(this::enrichTemplateWithCreator);
    }

    /**
     * Get default template for a college
     */
    public Mono<TemplateResponse> getDefaultTemplate(CustomUserDetails userDetails) {
        // First try to get college default
        return templateRepository.findByCollegeIdAndIsDefault(userDetails.getCollegeId(), true)
                .switchIfEmpty(
                        // If no college default, get system default
                        templateRepository.findByCollegeIdIsNull()
                                .filter(Template::getIsDefault)
                                .next()
                )
                .switchIfEmpty(
                        // If no default at all, get first active system template
                        templateRepository.findByCollegeIdIsNullAndIsActive(true).next()
                )
                .map(this::mapToTemplateResponse)
                .switchIfEmpty(Mono.error(new NotFoundException("No default template found")));
    }

    /**
     * Create custom template (College Admin only)
     */
    public Mono<TemplateResponse> createTemplate(TemplateCreateRequest request, CustomUserDetails userDetails) {
        // Only college admins and super admins can create templates
        if (!"COLLEGE_ADMIN".equals(userDetails.getRole()) && !"SUPER_ADMIN".equals(userDetails.getRole())) {
            return Mono.error(new ForbiddenException("Only admins can create templates"));
        }

        List<Template.Question> questions = request.getQuestions().stream()
                .map(this::mapToQuestion)
                .collect(Collectors.toList());

        Template template = Template.builder()
                .collegeId("SUPER_ADMIN".equals(userDetails.getRole()) ? null : userDetails.getCollegeId())
                .name(request.getName())
                .description(request.getDescription())
                .questions(questions)
                .isDefault(request.getIsDefault() != null ? request.getIsDefault() : false)
                .isActive(true)
                .createdBy(userDetails.getUserId())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // If setting as default, unset other defaults
        if (template.getIsDefault()) {
            return unsetOtherDefaults(userDetails.getCollegeId())
                    .then(templateRepository.save(template))
                    .flatMap(this::enrichTemplateWithCreator)
                    .doOnSuccess(t -> log.info("Template created: {} by user: {}", t.getName(), userDetails.getUserId()));
        }

        return templateRepository.save(template)
                .flatMap(this::enrichTemplateWithCreator)
                .doOnSuccess(t -> log.info("Template created: {} by user: {}", t.getName(), userDetails.getUserId()));
    }

    /**
     * Update template
     */
    public Mono<TemplateResponse> updateTemplate(String templateId, TemplateCreateRequest request, CustomUserDetails userDetails) {
        return templateRepository.findById(templateId)
                .switchIfEmpty(Mono.error(new NotFoundException("Template not found")))
                .flatMap(template -> {
                    // Check ownership
                    if (template.getCollegeId() != null &&
                            !template.getCollegeId().equals(userDetails.getCollegeId()) &&
                            !"SUPER_ADMIN".equals(userDetails.getRole())) {
                        return Mono.error(new ForbiddenException("Cannot update template from another college"));
                    }

                    // Update fields
                    template.setName(request.getName());
                    template.setDescription(request.getDescription());

                    List<Template.Question> questions = request.getQuestions().stream()
                            .map(this::mapToQuestion)
                            .collect(Collectors.toList());
                    template.setQuestions(questions);

                    template.setUpdatedAt(LocalDateTime.now());

                    return templateRepository.save(template);
                })
                .flatMap(this::enrichTemplateWithCreator);
    }

    /**
     * Delete template
     */
    public Mono<Void> deleteTemplate(String templateId, CustomUserDetails userDetails) {
        return templateRepository.findById(templateId)
                .switchIfEmpty(Mono.error(new NotFoundException("Template not found")))
                .flatMap(template -> {
                    // Check ownership
                    if (template.getCollegeId() != null &&
                            !template.getCollegeId().equals(userDetails.getCollegeId()) &&
                            !"SUPER_ADMIN".equals(userDetails.getRole())) {
                        return Mono.error(new ForbiddenException("Cannot delete template from another college"));
                    }

                    // Don't allow deleting default template
                    if (template.getIsDefault()) {
                        return Mono.error(new BadRequestException("Cannot delete default template. Set another template as default first."));
                    }

                    return templateRepository.delete(template);
                });
    }

    /**
     * Activate template
     */
    public Mono<TemplateResponse> activateTemplate(String templateId, CustomUserDetails userDetails) {
        return updateTemplateStatus(templateId, userDetails, true);
    }

    /**
     * Deactivate template
     */
    public Mono<TemplateResponse> deactivateTemplate(String templateId, CustomUserDetails userDetails) {
        return updateTemplateStatus(templateId, userDetails, false);
    }

    /**
     * Set template as default
     */
    public Mono<TemplateResponse> setAsDefault(String templateId, CustomUserDetails userDetails) {
        return templateRepository.findById(templateId)
                .switchIfEmpty(Mono.error(new NotFoundException("Template not found")))
                .flatMap(template -> {
                    // Check ownership
                    if (template.getCollegeId() != null &&
                            !template.getCollegeId().equals(userDetails.getCollegeId()) &&
                            !"SUPER_ADMIN".equals(userDetails.getRole())) {
                        return Mono.error(new ForbiddenException("Cannot modify template from another college"));
                    }

                    // Unset other defaults first
                    return unsetOtherDefaults(template.getCollegeId())
                            .then(Mono.defer(() -> {
                                template.setIsDefault(true);
                                template.setIsActive(true); // Auto-activate when setting as default
                                template.setUpdatedAt(LocalDateTime.now());
                                return templateRepository.save(template);
                            }));
                })
                .flatMap(this::enrichTemplateWithCreator);
    }

    /**
     * Clone template
     */
    public Mono<TemplateResponse> cloneTemplate(String templateId, CustomUserDetails userDetails) {
        return templateRepository.findById(templateId)
                .switchIfEmpty(Mono.error(new NotFoundException("Template not found")))
                .flatMap(sourceTemplate -> {
                    // Check if user can access this template
                    if (sourceTemplate.getCollegeId() != null &&
                            !sourceTemplate.getCollegeId().equals(userDetails.getCollegeId()) &&
                            !"SUPER_ADMIN".equals(userDetails.getRole())) {
                        return Mono.error(new ForbiddenException("Cannot clone template from another college"));
                    }

                    // Create clone
                    Template clonedTemplate = Template.builder()
                            .collegeId(userDetails.getCollegeId())
                            .name(sourceTemplate.getName() + " (Copy)")
                            .description(sourceTemplate.getDescription())
                            .questions(sourceTemplate.getQuestions()) // Deep copy
                            .isDefault(false)
                            .isActive(true)
                            .createdBy(userDetails.getUserId())
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();

                    return templateRepository.save(clonedTemplate);
                })
                .flatMap(this::enrichTemplateWithCreator);
    }

    /**
     * Get template questions
     */
    public Mono<List<Template.Question>> getTemplateQuestions(String templateId, CustomUserDetails userDetails) {
        return getTemplateById(templateId, userDetails)
                .map(TemplateResponse::getQuestions);
    }

    // Helper methods

    private Mono<TemplateResponse> updateTemplateStatus(String templateId, CustomUserDetails userDetails, boolean isActive) {
        return templateRepository.findById(templateId)
                .switchIfEmpty(Mono.error(new NotFoundException("Template not found")))
                .flatMap(template -> {
                    // Check ownership
                    if (template.getCollegeId() != null &&
                            !template.getCollegeId().equals(userDetails.getCollegeId()) &&
                            !"SUPER_ADMIN".equals(userDetails.getRole())) {
                        return Mono.error(new ForbiddenException("Cannot modify template from another college"));
                    }

                    template.setIsActive(isActive);
                    template.setUpdatedAt(LocalDateTime.now());

                    return templateRepository.save(template);
                })
                .flatMap(this::enrichTemplateWithCreator);
    }

    private Mono<Void> unsetOtherDefaults(String collegeId) {
        Flux<Template> templatesFlux;

        if (collegeId == null) {
            // For system templates
            templatesFlux = templateRepository.findByCollegeIdIsNull()
                    .filter(Template::getIsDefault);
        } else {
            // For college templates
            templatesFlux = templateRepository.findByCollegeId(collegeId)
                    .filter(Template::getIsDefault);
        }

        return templatesFlux
                .flatMap(template -> {
                    template.setIsDefault(false);
                    template.setUpdatedAt(LocalDateTime.now());
                    return templateRepository.save(template);
                })
                .then();
    }

    private Template.Question mapToQuestion(TemplateQuestionRequest request) {
        return Template.Question.builder()
                .id(UUID.randomUUID().toString())
                .text(request.getText())
                .type(request.getType())
                .required(request.getRequired() != null ? request.getRequired() : false)
                .options(request.getOptions())
                .maxLength(request.getMaxLength())
                .minValue(request.getMinValue())
                .maxValue(request.getMaxValue())
                .placeholder(request.getPlaceholder())
                .order(request.getOrder())
                .build();
    }

    private Mono<TemplateResponse> enrichTemplateWithCreator(Template template) {
        if (template.getCreatedBy() == null) {
            return Mono.just(mapToTemplateResponse(template));
        }

        return userService.getUserBasicInfo(template.getCreatedBy())
                .map(creator -> {
                    TemplateResponse response = mapToTemplateResponse(template);
                    response.setCreatedByName(creator.getFirstName() + " " + creator.getLastName());
                    return response;
                })
                .defaultIfEmpty(mapToTemplateResponse(template));
    }

    private TemplateResponse mapToTemplateResponse(Template template) {
        return TemplateResponse.builder()
                .id(template.getId())
                .collegeId(template.getCollegeId())
                .name(template.getName())
                .description(template.getDescription())
                .questions(template.getQuestions())
                .isDefault(template.getIsDefault())
                .isActive(template.getIsActive())
                .createdBy(template.getCreatedBy())
                .createdAt(template.getCreatedAt())
                .build();
    }
}