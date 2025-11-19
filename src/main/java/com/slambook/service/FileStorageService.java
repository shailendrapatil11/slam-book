package com.slambook.service;

import com.slambook.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageService {

    @Value("${app.file.upload-dir:./uploads}")
    private String uploadDir;

    @Value("${app.file.max-file-size:5242880}") // 5MB default
    private long maxFileSize;

    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );

    private static final List<String> ALLOWED_VIDEO_TYPES = Arrays.asList(
            "video/mp4", "video/webm", "video/ogg"
    );

    private static final List<String> ALLOWED_AUDIO_TYPES = Arrays.asList(
            "audio/mpeg", "audio/mp3", "audio/wav", "audio/ogg"
    );

    /**
     * Upload profile picture
     */
    public Mono<String> uploadProfilePicture(FilePart filePart, String userId) {
        return uploadFile(filePart, "profiles", userId, ALLOWED_IMAGE_TYPES);
    }

    /**
     * Upload slam book attachment (image, video, audio)
     */
    public Mono<String> uploadSlamBookAttachment(FilePart filePart, String userId, String attachmentType) {
        List<String> allowedTypes;
        String folder;

        switch (attachmentType.toUpperCase()) {
            case "IMAGE":
                allowedTypes = ALLOWED_IMAGE_TYPES;
                folder = "slambook/images";
                break;
            case "VIDEO":
                allowedTypes = ALLOWED_VIDEO_TYPES;
                folder = "slambook/videos";
                break;
            case "AUDIO":
                allowedTypes = ALLOWED_AUDIO_TYPES;
                folder = "slambook/audio";
                break;
            default:
                return Mono.error(new BadRequestException("Invalid attachment type: " + attachmentType));
        }

        return uploadFile(filePart, folder, userId, allowedTypes);
    }

    /**
     * Upload college logo
     */
    public Mono<String> uploadCollegeLogo(FilePart filePart, String collegeId) {
        return uploadFile(filePart, "colleges/logos", collegeId, ALLOWED_IMAGE_TYPES);
    }

    /**
     * Generic file upload method
     */
    private Mono<String> uploadFile(FilePart filePart, String folder, String userId, List<String> allowedTypes) {
        // Validate file
        return validateFile(filePart, allowedTypes)
                .flatMap(valid -> {
                    try {
                        // Create directory if not exists
                        Path uploadPath = Paths.get(uploadDir, folder);
                        Files.createDirectories(uploadPath);

                        // Generate unique filename
                        String originalFilename = filePart.filename();
                        String extension = getFileExtension(originalFilename);
                        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                        String filename = String.format("%s_%s_%s%s",
                                userId,
                                timestamp,
                                UUID.randomUUID().toString().substring(0, 8),
                                extension);

                        Path filePath = uploadPath.resolve(filename);

                        log.info("Uploading file: {} to: {}", originalFilename, filePath);

                        // Save file
                        return DataBufferUtils.write(filePart.content(), filePath, StandardCopyOption.REPLACE_EXISTING)
                                .then(Mono.fromCallable(() -> {
                                    // Return relative URL
                                    String relativeUrl = String.format("/%s/%s/%s", uploadDir, folder, filename);
                                    log.info("File uploaded successfully: {}", relativeUrl);
                                    return relativeUrl;
                                }));

                    } catch (IOException e) {
                        log.error("Error creating upload directory", e);
                        return Mono.error(new RuntimeException("Failed to create upload directory", e));
                    }
                });
    }

    /**
     * Validate file size and type
     */
    private Mono<Boolean> validateFile(FilePart filePart, List<String> allowedTypes) {
        // Get content type
        String contentType = filePart.headers().getContentType() != null
                ? Objects.requireNonNull(filePart.headers().getContentType()).toString()
                : "";

        // Validate content type
        boolean isAllowedType = allowedTypes.stream()
                .anyMatch(type -> contentType.toLowerCase().contains(type.toLowerCase()));

        if (!isAllowedType) {
            return Mono.error(new BadRequestException(
                    "Invalid file type. Allowed types: " + String.join(", ", allowedTypes)
            ));
        }

        // Validate file size (check content length if available)
        return filePart.content()
                .reduce(0L, (acc, buffer) -> {
                    long size = acc + buffer.readableByteCount();
                    DataBufferUtils.release(buffer);
                    return size;
                })
                .flatMap(fileSize -> {
                    if (fileSize > maxFileSize) {
                        return Mono.error(new BadRequestException(
                                String.format("File size exceeds maximum limit of %d MB", maxFileSize / 1024 / 1024)
                        ));
                    }
                    return Mono.just(true);
                });
    }

    /**
     * Delete file
     */
    public Mono<Void> deleteFile(String fileUrl) {
        return Mono.fromRunnable(() -> {
            try {
                // Convert URL to file path
                String filePath = fileUrl.startsWith("/") ? fileUrl.substring(1) : fileUrl;
                Path path = Paths.get(filePath);

                if (Files.exists(path)) {
                    Files.delete(path);
                    log.info("File deleted: {}", fileUrl);
                } else {
                    log.warn("File not found for deletion: {}", fileUrl);
                }
            } catch (IOException e) {
                log.error("Error deleting file: {}", fileUrl, e);
            }
        });
    }

    /**
     * Get file extension
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }

        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }

        return filename.substring(lastDotIndex);
    }

    /**
     * Get file size in human-readable format
     */
    public String getReadableFileSize(long size) {
        if (size <= 0) return "0 B";

        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));

        return String.format("%.2f %s",
                size / Math.pow(1024, digitGroups),
                units[digitGroups]);
    }

    /**
     * Validate image dimensions (optional, for advanced validation)
     */
    public Mono<Boolean> validateImageDimensions(FilePart filePart, int minWidth, int minHeight, int maxWidth, int maxHeight) {
        // This would require additional image processing library like ImageIO
        // For now, return true (basic validation only)
        return Mono.just(true);
    }

    /**
     * Generate thumbnail for image (optional, for future implementation)
     */
    public Mono<String> generateThumbnail(String originalImageUrl, int width, int height) {
        // This would require image processing library
        // For now, return original URL
        return Mono.just(originalImageUrl);
    }
}