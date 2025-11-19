package com.slambook.controller;

import com.slambook.dto.request.TemplateCreateRequest;
import com.slambook.dto.response.ApiResponse;
import com.slambook.dto.response.TemplateResponse;
import com.slambook.model.Template;
import com.slambook.security.CustomUserDetails;
import com.slambook.service.TemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/templates")
@RequiredArgsConstructor
public class TemplateController {

    private final TemplateService templateService;

    @GetMapping("/system")
    public Mono<ResponseEntity<ApiResponse<List<TemplateResponse>>>> getSystemTemplates() {
        log.info("Get system templates");
        return templateService.getSystemTemplates()
                .collectList()
                .map(templates -> ResponseEntity.ok(ApiResponse.success(templates)));
    }

    @GetMapping("/college")
    public Mono<ResponseEntity<ApiResponse<List<TemplateResponse>>>> getCollegeTemplates(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("Get college templates for: {}", userDetails.getCollegeId());
        return templateService.getCollegeTemplates(userDetails)
                .collectList()
                .map(templates -> ResponseEntity.ok(ApiResponse.success(templates)));
    }

    @GetMapping("/available")
    public Mono<ResponseEntity<ApiResponse<List<TemplateResponse>>>> getAllAvailableTemplates(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("Get all available templates for user: {}", userDetails.getUserId());
        return templateService.getAllAvailableTemplates(userDetails)
                .collectList()
                .map(templates -> ResponseEntity.ok(ApiResponse.success(templates)));
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<ApiResponse<TemplateResponse>>> getTemplateById(
            @PathVariable String id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("Get template by id: {}", id);
        return templateService.getTemplateById(id, userDetails)
                .map(template -> ResponseEntity.ok(ApiResponse.success(template)));
    }

    @GetMapping("/default")
    public Mono<ResponseEntity<ApiResponse<TemplateResponse>>> getDefaultTemplate(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("Get default template for college: {}", userDetails.getCollegeId());
        return templateService.getDefaultTemplate(userDetails)
                .map(template -> ResponseEntity.ok(ApiResponse.success(template)));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('COLLEGE_ADMIN', 'SUPER_ADMIN')")
    public Mono<ResponseEntity<ApiResponse<TemplateResponse>>> createTemplate(
            @Valid @RequestBody TemplateCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("Create template: {} by user: {}", request.getName(), userDetails.getUserId());
        return templateService.createTemplate(request, userDetails)
                .map(template -> ResponseEntity
                        .status(HttpStatus.CREATED)
                        .body(ApiResponse.success("Template created successfully", template)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('COLLEGE_ADMIN', 'SUPER_ADMIN')")
    public Mono<ResponseEntity<ApiResponse<TemplateResponse>>> updateTemplate(
            @PathVariable String id,
            @Valid @RequestBody TemplateCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("Update template: {} by user: {}", id, userDetails.getUserId());
        return templateService.updateTemplate(id, request, userDetails)
                .map(template -> ResponseEntity.ok(ApiResponse.success("Template updated successfully", template)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('COLLEGE_ADMIN', 'SUPER_ADMIN')")
    public Mono<ResponseEntity<ApiResponse<Void>>> deleteTemplate(
            @PathVariable String id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("Delete template: {} by user: {}", id, userDetails.getUserId());
        return templateService.deleteTemplate(id, userDetails)
                .then(Mono.just(ResponseEntity.ok(ApiResponse.<Void>success("Template deleted successfully", null))));
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasAnyAuthority('COLLEGE_ADMIN', 'SUPER_ADMIN')")
    public Mono<ResponseEntity<ApiResponse<TemplateResponse>>> activateTemplate(
            @PathVariable String id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("Activate template: {}", id);
        return templateService.activateTemplate(id, userDetails)
                .map(template -> ResponseEntity.ok(ApiResponse.success("Template activated", template)));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyAuthority('COLLEGE_ADMIN', 'SUPER_ADMIN')")
    public Mono<ResponseEntity<ApiResponse<TemplateResponse>>> deactivateTemplate(
            @PathVariable String id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("Deactivate template: {}", id);
        return templateService.deactivateTemplate(id, userDetails)
                .map(template -> ResponseEntity.ok(ApiResponse.success("Template deactivated", template)));
    }

    @PatchMapping("/{id}/set-default")
    @PreAuthorize("hasAnyAuthority('COLLEGE_ADMIN', 'SUPER_ADMIN')")
    public Mono<ResponseEntity<ApiResponse<TemplateResponse>>> setAsDefault(
            @PathVariable String id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("Set template as default: {}", id);
        return templateService.setAsDefault(id, userDetails)
                .map(template -> ResponseEntity.ok(ApiResponse.success("Template set as default", template)));
    }

    @PostMapping("/{id}/clone")
    public Mono<ResponseEntity<ApiResponse<TemplateResponse>>> cloneTemplate(
            @PathVariable String id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("Clone template: {} by user: {}", id, userDetails.getUserId());
        return templateService.cloneTemplate(id, userDetails)
                .map(template -> ResponseEntity
                        .status(HttpStatus.CREATED)
                        .body(ApiResponse.success("Template cloned successfully", template)));
    }

    @GetMapping("/{id}/questions")
    public Mono<ResponseEntity<ApiResponse<List<Template.Question>>>> getTemplateQuestions(
            @PathVariable String id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("Get template questions: {}", id);
        return templateService.getTemplateQuestions(id, userDetails)
                .map(questions -> ResponseEntity.ok(ApiResponse.success(questions)));
    }
}