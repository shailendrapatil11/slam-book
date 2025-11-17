package com.slambook.controller;

import com.slambook.dto.request.CollegeCreateRequest;
import com.slambook.dto.request.CollegeUpdateRequest;
import com.slambook.dto.response.ApiResponse;
import com.slambook.dto.response.CollegeResponse;
import com.slambook.service.CollegeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('SUPER_ADMIN')")
public class SuperAdminController {

    private final CollegeService collegeService;

    @PostMapping("/colleges")
    public Mono<ResponseEntity<ApiResponse<CollegeResponse>>> createCollege(
            @Valid @RequestBody CollegeCreateRequest request) {
        log.info("Creating college: {}", request.getName());
        return collegeService.createCollege(request)
                .map(college -> ResponseEntity
                        .status(HttpStatus.CREATED)
                        .body(ApiResponse.success("College created successfully", college)));
    }

    @GetMapping("/colleges")
    public Mono<ResponseEntity<ApiResponse<List<CollegeResponse>>>> getAllColleges() {
        log.info("Fetching all colleges");
        return collegeService.getAllColleges()
                .collectList()
                .map(colleges -> ResponseEntity.ok(ApiResponse.success(colleges)));
    }

    @GetMapping("/colleges/{id}")
    public Mono<ResponseEntity<ApiResponse<CollegeResponse>>> getCollegeById(@PathVariable String id) {
        log.info("Fetching college by id: {}", id);
        return collegeService.getCollegeById(id)
                .map(college -> ResponseEntity.ok(ApiResponse.success(college)));
    }

    @GetMapping("/colleges/code/{code}")
    public Mono<ResponseEntity<ApiResponse<CollegeResponse>>> getCollegeByCode(@PathVariable String code) {
        log.info("Fetching college by code: {}", code);
        return collegeService.getCollegeByCode(code)
                .map(college -> ResponseEntity.ok(ApiResponse.success(college)));
    }

    @PutMapping("/colleges/{id}")
    public Mono<ResponseEntity<ApiResponse<CollegeResponse>>> updateCollege(
            @PathVariable String id,
            @Valid @RequestBody CollegeUpdateRequest request) {
        log.info("Updating college: {}", id);
        return collegeService.updateCollege(id, request)
                .map(college -> ResponseEntity.ok(ApiResponse.success("College updated successfully", college)));
    }

    @DeleteMapping("/colleges/{id}")
    public Mono<ResponseEntity<ApiResponse<Void>>> deleteCollege(@PathVariable String id) {
        log.info("Deleting college: {}", id);
        return collegeService.deleteCollege(id)
                .then(Mono.just(ResponseEntity.ok(ApiResponse.<Void>success("College deleted successfully", null))));
    }

    @GetMapping("/analytics")
    public Mono<ResponseEntity<ApiResponse<String>>> getSystemAnalytics() {
        log.info("Fetching system analytics");
        // TODO: Implement system analytics
        return Mono.just(ResponseEntity.ok(ApiResponse.success("Analytics data", "TODO")));
    }
}