package com.slambook.controller;

import com.slambook.dto.response.ApiResponse;
import com.slambook.security.CustomUserDetails;
import com.slambook.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileUploadController {

    private final FileStorageService fileStorageService;

    /**
     * Upload profile picture
     */
    @PostMapping("/profile-picture")
    public Mono<ResponseEntity<ApiResponse<Map<String, String>>>> uploadProfilePicture(
            @RequestPart("file") FilePart file,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("Upload profile picture for user: {}", userDetails.getUserId());

        return fileStorageService.uploadProfilePicture(file, userDetails.getUserId())
                .map(url -> {
                    Map<String, String> response = new HashMap<>();
                    response.put("url", url);
                    response.put("type", "profile_picture");
                    return ResponseEntity
                            .status(HttpStatus.CREATED)
                            .body(ApiResponse.success("Profile picture uploaded successfully", response));
                });
    }

    /**
     * Upload slam book attachment (image)
     */
    @PostMapping("/slambook/image")
    public Mono<ResponseEntity<ApiResponse<Map<String, String>>>> uploadSlambookImage(
            @RequestPart("file") FilePart file,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("Upload slam book image for user: {}", userDetails.getUserId());

        return fileStorageService.uploadSlamBookAttachment(file, userDetails.getUserId(), "IMAGE")
                .map(url -> {
                    Map<String, String> response = new HashMap<>();
                    response.put("url", url);
                    response.put("type", "image");
                    return ResponseEntity
                            .status(HttpStatus.CREATED)
                            .body(ApiResponse.success("Image uploaded successfully", response));
                });
    }

    /**
     * Upload slam book attachment (video)
     */
    @PostMapping("/slambook/video")
    public Mono<ResponseEntity<ApiResponse<Map<String, String>>>> uploadSlambookVideo(
            @RequestPart("file") FilePart file,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("Upload slam book video for user: {}", userDetails.getUserId());

        return fileStorageService.uploadSlamBookAttachment(file, userDetails.getUserId(), "VIDEO")
                .map(url -> {
                    Map<String, String> response = new HashMap<>();
                    response.put("url", url);
                    response.put("type", "video");
                    return ResponseEntity
                            .status(HttpStatus.CREATED)
                            .body(ApiResponse.success("Video uploaded successfully", response));
                });
    }

    /**
     * Upload slam book attachment (audio)
     */
    @PostMapping("/slambook/audio")
    public Mono<ResponseEntity<ApiResponse<Map<String, String>>>> uploadSlambookAudio(
            @RequestPart("file") FilePart file,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("Upload slam book audio for user: {}", userDetails.getUserId());

        return fileStorageService.uploadSlamBookAttachment(file, userDetails.getUserId(), "AUDIO")
                .map(url -> {
                    Map<String, String> response = new HashMap<>();
                    response.put("url", url);
                    response.put("type", "audio");
                    return ResponseEntity
                            .status(HttpStatus.CREATED)
                            .body(ApiResponse.success("Audio uploaded successfully", response));
                });
    }

    /**
     * Upload college logo (Admin only)
     */
    @PostMapping("/college/logo")
    @PreAuthorize("hasAnyAuthority('COLLEGE_ADMIN', 'SUPER_ADMIN')")
    public Mono<ResponseEntity<ApiResponse<Map<String, String>>>> uploadCollegeLogo(
            @RequestPart("file") FilePart file,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("Upload college logo for college: {}", userDetails.getCollegeId());

        return fileStorageService.uploadCollegeLogo(file, userDetails.getCollegeId())
                .map(url -> {
                    Map<String, String> response = new HashMap<>();
                    response.put("url", url);
                    response.put("type", "college_logo");
                    return ResponseEntity
                            .status(HttpStatus.CREATED)
                            .body(ApiResponse.success("College logo uploaded successfully", response));
                });
    }

    /**
     * Delete file
     */
    @DeleteMapping
    public Mono<ResponseEntity<ApiResponse<Void>>> deleteFile(
            @RequestParam String fileUrl,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("Delete file: {} by user: {}", fileUrl, userDetails.getUserId());

        return fileStorageService.deleteFile(fileUrl)
                .then(Mono.just(ResponseEntity.ok(ApiResponse.<Void>success("File deleted successfully", null))));
    }
}