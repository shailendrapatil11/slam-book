package com.slambook.controller;

import com.slambook.dto.request.ReactionRequest;
import com.slambook.dto.request.ReportRequest;
import com.slambook.dto.request.SlamBookEntryCreateRequest;
import com.slambook.dto.request.SlamBookEntryUpdateRequest;
import com.slambook.dto.response.ApiResponse;
import com.slambook.dto.response.SlamBookEntryResponse;
import com.slambook.security.CustomUserDetails;
import com.slambook.service.SlamBookService;
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
@RequestMapping("/api/v1/slambook")
@RequiredArgsConstructor
public class SlamBookController {

    private final SlamBookService slamBookService;

    @PostMapping("/entries")
    public Mono<ResponseEntity<ApiResponse<SlamBookEntryResponse>>> createEntry(
            @Valid @RequestBody SlamBookEntryCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("Create slam book entry by user: {} for user: {}", userDetails.getUserId(), request.getWrittenFor());
        return slamBookService.createEntry(userDetails, request)
                .map(entry -> ResponseEntity
                        .status(HttpStatus.CREATED)
                        .body(ApiResponse.success("Entry created successfully", entry)));
    }

    @GetMapping("/entries/for-me")
    public Mono<ResponseEntity<ApiResponse<List<SlamBookEntryResponse>>>> getEntriesForMe(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("Get entries for user: {}", userDetails.getUserId());
        return slamBookService.getEntriesForMe(userDetails)
                .collectList()
                .map(entries -> ResponseEntity.ok(ApiResponse.success(entries)));
    }

    @GetMapping("/entries/by-me")
    public Mono<ResponseEntity<ApiResponse<List<SlamBookEntryResponse>>>> getEntriesByMe(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("Get entries by user: {}", userDetails.getUserId());
        return slamBookService.getEntriesByMe(userDetails)
                .collectList()
                .map(entries -> ResponseEntity.ok(ApiResponse.success(entries)));
    }

    @GetMapping("/entries/{id}")
    public Mono<ResponseEntity<ApiResponse<SlamBookEntryResponse>>> getEntryById(
            @PathVariable String id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("Get entry by id: {}", id);
        return slamBookService.getEntryById(id, userDetails)
                .map(entry -> ResponseEntity.ok(ApiResponse.success(entry)));
    }

    @PutMapping("/entries/{id}")
    public Mono<ResponseEntity<ApiResponse<SlamBookEntryResponse>>> updateEntry(
            @PathVariable String id,
            @Valid @RequestBody SlamBookEntryUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("Update entry: {} by user: {}", id, userDetails.getUserId());
        return slamBookService.updateEntry(id, userDetails, request)
                .map(entry -> ResponseEntity.ok(ApiResponse.success("Entry updated successfully", entry)));
    }

    @DeleteMapping("/entries/{id}")
    public Mono<ResponseEntity<ApiResponse<Void>>> deleteEntry(
            @PathVariable String id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("Delete entry: {} by user: {}", id, userDetails.getUserId());
        return slamBookService.deleteEntry(id, userDetails)
                .then(Mono.just(ResponseEntity.ok(ApiResponse.<Void>success("Entry deleted successfully", null))));
    }

    @PostMapping("/entries/{id}/reactions")
    public Mono<ResponseEntity<ApiResponse<SlamBookEntryResponse>>> addReaction(
            @PathVariable String id,
            @Valid @RequestBody ReactionRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("Add reaction to entry: {} by user: {}", id, userDetails.getUserId());
        return slamBookService.addReaction(id, userDetails, request)
                .map(entry -> ResponseEntity.ok(ApiResponse.success("Reaction added", entry)));
    }

    @DeleteMapping("/entries/{id}/reactions")
    public Mono<ResponseEntity<ApiResponse<SlamBookEntryResponse>>> removeReaction(
            @PathVariable String id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("Remove reaction from entry: {} by user: {}", id, userDetails.getUserId());
        return slamBookService.removeReaction(id, userDetails)
                .map(entry -> ResponseEntity.ok(ApiResponse.success("Reaction removed", entry)));
    }

    @PostMapping("/entries/{id}/report")
    public Mono<ResponseEntity<ApiResponse<SlamBookEntryResponse>>> reportEntry(
            @PathVariable String id,
            @Valid @RequestBody ReportRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("Report entry: {} by user: {}", id, userDetails.getUserId());
        return slamBookService.reportEntry(id, userDetails, request.getReason())
                .map(entry -> ResponseEntity.ok(ApiResponse.success("Entry reported successfully", entry)));
    }

    @GetMapping("/entries/reported")
    @PreAuthorize("hasAnyAuthority('COLLEGE_ADMIN', 'SUPER_ADMIN')")
    public Mono<ResponseEntity<ApiResponse<List<SlamBookEntryResponse>>>> getReportedEntries(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("Get reported entries for college: {}", userDetails.getCollegeId());
        return slamBookService.getReportedEntries(userDetails)
                .collectList()
                .map(entries -> ResponseEntity.ok(ApiResponse.success(entries)));
    }
}