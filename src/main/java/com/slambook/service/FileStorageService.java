package com.slambook.service;

import com.slambook.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.util.unit.DataSize;
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

    @Value("${app.file.max-file-size:5MB}")
    private DataSize maxFileSize;

    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );

    private static final List<String> ALLOWED_VIDEO_TYPES = Arrays.asList(
            "video/mp4", "video/webm", "video/ogg"
    );

    private static final List<String> ALLOWED_AUDIO_TYPES = Arrays.asList(
            "audio/mpeg", "audio/mp3", "audio/wav", "audio/ogg"
    );

    public Mono<String> uploadProfilePicture(FilePart filePart, String userId) {
        return uploadFile(filePart, "profiles", userId, ALLOWED_IMAGE_TYPES);
    }

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

    public Mono<String> uploadCollegeLogo(FilePart filePart, String collegeId) {
        return uploadFile(filePart, "colleges/logos", collegeId, ALLOWED_IMAGE_TYPES);
    }

    private Mono<String> uploadFile(FilePart filePart, String folder, String userId, List<String> allowedTypes) {
        // 1. Validate content type
        String contentType = filePart.headers().getContentType() != null
                ? Objects.requireNonNull(filePart.headers().getContentType()).toString()
                : "";
        boolean isAllowedType = allowedTypes.stream()
                .anyMatch(type -> contentType.toLowerCase().contains(type.toLowerCase()));
        if (!isAllowedType) {
            return Mono.error(new BadRequestException(
                    "Invalid file type. Allowed types: " + String.join(", ", allowedTypes)
            ));
        }

        Path tempFile;
        try {
            // 2. Save to a temporary file
            tempFile = Files.createTempFile("slambook-", "-" + filePart.filename());
        } catch (IOException e) {
            log.error("Failed to create temporary file", e);
            return Mono.error(new RuntimeException("Failed to create temporary file", e));
        }

        return filePart.transferTo(tempFile)
                .then(Mono.fromCallable(() -> {
                    // 3. Validate file size
                    long fileSize = Files.size(tempFile);
                    if (fileSize > maxFileSize.toBytes()) {
                        Files.delete(tempFile); // Clean up temp file
                        throw new BadRequestException(
                                String.format("File size exceeds maximum limit of %s", maxFileSize.toMegabytes() + "MB")
                        );
                    }

                    // 4. Move file to final destination
                    Path uploadPath = Paths.get(uploadDir, folder);
                    Files.createDirectories(uploadPath);

                    String originalFilename = filePart.filename();
                    String extension = getFileExtension(originalFilename);
                    String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                    String filename = String.format("%s_%s_%s%s",
                            userId,
                            timestamp,
                            UUID.randomUUID().toString().substring(0, 8),
                            extension);

                    Path finalPath = uploadPath.resolve(filename);
                    Files.move(tempFile, finalPath, StandardCopyOption.REPLACE_EXISTING);

                    log.info("File moved to final destination: {}", finalPath);

                    // 5. Return relative URL, ensuring forward slashes for web paths
                    String relativeUrl = "/" + Path.of(folder).resolve(filename).toString().replace('\\', '/');
                    log.info("File uploaded successfully. URL: {}", relativeUrl);
                    return relativeUrl;
                }));
    }

    public Mono<Void> deleteFile(String fileUrl) {
        return Mono.fromRunnable(() -> {
            if (fileUrl == null || fileUrl.isBlank()) {
                return;
            }
            try {
                // Convert URL to file a path by combining with the base upload directory
                String relativePath = fileUrl.startsWith("/") ? fileUrl.substring(1) : fileUrl;
                Path path = Paths.get(uploadDir).resolve(relativePath);

                if (Files.exists(path)) {
                    Files.delete(path);
                    log.info("File deleted: {}", path);
                } else {
                    log.warn("File not found for deletion: {}", path);
                }
            } catch (IOException e) {
                log.error("Error deleting file: {}", fileUrl, e);
            }
        });
    }

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
}