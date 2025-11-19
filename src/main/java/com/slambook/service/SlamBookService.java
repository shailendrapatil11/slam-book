package com.slambook.service;

import com.slambook.dto.request.ReactionRequest;
import com.slambook.dto.request.SlamBookEntryCreateRequest;
import com.slambook.dto.request.SlamBookEntryUpdateRequest;
import com.slambook.dto.response.ReactionResponse;
import com.slambook.dto.response.SlamBookEntryResponse;
import com.slambook.dto.response.UserBasicInfo;
import com.slambook.exception.BadRequestException;
import com.slambook.exception.ForbiddenException;
import com.slambook.exception.NotFoundException;
import com.slambook.model.SlamBookEntry;
import com.slambook.repository.SlamBookEntryRepository;
import com.slambook.repository.UserRepository;
import com.slambook.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SlamBookService {

    private final SlamBookEntryRepository slamBookEntryRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final NotificationService notificationService;
    private final FileStorageService fileStorageService;

    public Mono<SlamBookEntryResponse> createEntry(CustomUserDetails userDetails, SlamBookEntryCreateRequest request) {
        // Check if user already wrote for this person
        return slamBookEntryRepository.existsByWrittenForAndWrittenBy(request.getWrittenFor(), userDetails.getUserId())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new BadRequestException("You have already written in this person's slam book"));
                    }

                    // Verify writtenFor user exists and is in the same college
                    return userRepository.findById(request.getWrittenFor())
                            .switchIfEmpty(Mono.error(new NotFoundException("User not found")))
                            .flatMap(targetUser -> {
                                if (!targetUser.getCollegeId().equals(userDetails.getCollegeId())) {
                                    return Mono.error(new ForbiddenException("Cannot write for user from different college"));
                                }

                                // Check if target user allows anonymous entries
                                if (Boolean.TRUE.equals(request.getIsAnonymous()) &&
                                        targetUser.getSlamBookSettings() != null &&
                                        !Boolean.TRUE.equals(targetUser.getSlamBookSettings().getAllowAnonymous())) {
                                    return Mono.error(new BadRequestException("This user does not allow anonymous entries"));
                                }

                                SlamBookEntry entry = SlamBookEntry.builder()
                                        .collegeId(userDetails.getCollegeId())
                                        .writtenFor(request.getWrittenFor())
                                        .writtenBy(userDetails.getUserId())
                                        .isAnonymous(request.getIsAnonymous() != null ? request.getIsAnonymous() : false)
                                        .responses(request.getResponses())
                                        .ratings(request.getRatings())
                                        .attachments(new ArrayList<>()) // Initialize empty list
                                        .reactions(new ArrayList<>())
                                        .visibility(request.getVisibility() != null ? request.getVisibility() : SlamBookEntry.Visibility.PUBLIC)
                                        .isReported(false)
                                        .createdAt(LocalDateTime.now())
                                        .updatedAt(LocalDateTime.now())
                                        .build();

                                return slamBookEntryRepository.save(entry);
                            });
                })
                .flatMap(entry -> {
                    // Send notification to the user
                    return notificationService.sendNewEntryNotification(entry)
                            .thenReturn(entry);
                })
                .flatMap(this::enrichEntryWithUserDetails);
    }

    /**
     * Add attachment to existing entry
     */
    public Mono<SlamBookEntryResponse> addAttachment(
            String entryId,
            CustomUserDetails userDetails,
            String attachmentUrl,
            SlamBookEntry.AttachmentType type,
            String filename,
            Long size) {

        return slamBookEntryRepository.findById(entryId)
                .switchIfEmpty(Mono.error(new NotFoundException("Entry not found")))
                .flatMap(entry -> {
                    // Only the writer can add attachments
                    if (!entry.getWrittenBy().equals(userDetails.getUserId())) {
                        return Mono.error(new ForbiddenException("You can only add attachments to your own entries"));
                    }

                    if (entry.getAttachments() == null) {
                        entry.setAttachments(new ArrayList<>());
                    }

                    // Create attachment object
                    SlamBookEntry.Attachment attachment = SlamBookEntry.Attachment.builder()
                            .id(java.util.UUID.randomUUID().toString())
                            .type(type)
                            .url(attachmentUrl)
                            .filename(filename)
                            .size(size)
                            .build();

                    entry.getAttachments().add(attachment);
                    entry.setUpdatedAt(LocalDateTime.now());

                    return slamBookEntryRepository.save(entry);
                })
                .flatMap(this::enrichEntryWithUserDetails)
                .doOnSuccess(response -> log.info("Attachment added to entry: {}", entryId));
    }

    /**
     * Remove attachment from entry
     */
    public Mono<SlamBookEntryResponse> removeAttachment(String entryId, String attachmentId, CustomUserDetails userDetails) {
        return slamBookEntryRepository.findById(entryId)
                .switchIfEmpty(Mono.error(new NotFoundException("Entry not found")))
                .flatMap(entry -> {
                    // Only the writer can remove attachments
                    if (!entry.getWrittenBy().equals(userDetails.getUserId())) {
                        return Mono.error(new ForbiddenException("You can only remove attachments from your own entries"));
                    }

                    if (entry.getAttachments() == null || entry.getAttachments().isEmpty()) {
                        return Mono.error(new NotFoundException("No attachments found"));
                    }

                    // Find and remove attachment
                    SlamBookEntry.Attachment attachmentToRemove = entry.getAttachments().stream()
                            .filter(a -> a.getId().equals(attachmentId))
                            .findFirst()
                            .orElseThrow(() -> new NotFoundException("Attachment not found"));

                    // Delete file from storage
                    return fileStorageService.deleteFile(attachmentToRemove.getUrl())
                            .then(Mono.defer(() -> {
                                entry.getAttachments().removeIf(a -> a.getId().equals(attachmentId));
                                entry.setUpdatedAt(LocalDateTime.now());
                                return slamBookEntryRepository.save(entry);
                            }));
                })
                .flatMap(this::enrichEntryWithUserDetails)
                .doOnSuccess(response -> log.info("Attachment removed from entry: {}", entryId));
    }

    public Mono<SlamBookEntryResponse> updateEntry(String entryId, CustomUserDetails userDetails, SlamBookEntryUpdateRequest request) {
        return slamBookEntryRepository.findById(entryId)
                .switchIfEmpty(Mono.error(new NotFoundException("Entry not found")))
                .flatMap(entry -> {
                    // Only the writer can update
                    if (!entry.getWrittenBy().equals(userDetails.getUserId())) {
                        return Mono.error(new ForbiddenException("You can only update your own entries"));
                    }

                    if (request.getResponses() != null) entry.setResponses(request.getResponses());
                    if (request.getRatings() != null) entry.setRatings(request.getRatings());
                    if (request.getVisibility() != null) entry.setVisibility(request.getVisibility());

                    entry.setUpdatedAt(LocalDateTime.now());

                    return slamBookEntryRepository.save(entry);
                })
                .flatMap(this::enrichEntryWithUserDetails);
    }

    public Mono<Void> deleteEntry(String entryId, CustomUserDetails userDetails) {
        return slamBookEntryRepository.findById(entryId)
                .switchIfEmpty(Mono.error(new NotFoundException("Entry not found")))
                .flatMap(entry -> {
                    // Only the writer or the person it's written for can delete
                    if (!entry.getWrittenBy().equals(userDetails.getUserId()) &&
                            !entry.getWrittenFor().equals(userDetails.getUserId())) {
                        return Mono.error(new ForbiddenException("You cannot delete this entry"));
                    }

                    // Delete all attachments from storage
                    if (entry.getAttachments() != null && !entry.getAttachments().isEmpty()) {
                        return Flux.fromIterable(entry.getAttachments())
                                .flatMap(attachment -> fileStorageService.deleteFile(attachment.getUrl()))
                                .then(slamBookEntryRepository.delete(entry));
                    }

                    return slamBookEntryRepository.delete(entry);
                });
    }

    public Flux<SlamBookEntryResponse> getEntriesForMe(CustomUserDetails userDetails) {
        return slamBookEntryRepository.findByWrittenFor(userDetails.getUserId())
                .flatMap(this::enrichEntryWithUserDetails);
    }

    public Flux<SlamBookEntryResponse> getEntriesByMe(CustomUserDetails userDetails) {
        return slamBookEntryRepository.findByWrittenBy(userDetails.getUserId())
                .flatMap(this::enrichEntryWithUserDetails);
    }

    public Mono<SlamBookEntryResponse> getEntryById(String entryId, CustomUserDetails userDetails) {
        return slamBookEntryRepository.findById(entryId)
                .switchIfEmpty(Mono.error(new NotFoundException("Entry not found")))
                .flatMap(entry -> {
                    // Check access rights
                    boolean canAccess = entry.getWrittenFor().equals(userDetails.getUserId()) ||
                            entry.getWrittenBy().equals(userDetails.getUserId()) ||
                            entry.getVisibility() == SlamBookEntry.Visibility.PUBLIC ||
                            "COLLEGE_ADMIN".equals(userDetails.getRole()) ||
                            "SUPER_ADMIN".equals(userDetails.getRole());

                    if (!canAccess) {
                        return Mono.error(new ForbiddenException("You don't have access to this entry"));
                    }

                    return Mono.just(entry);
                })
                .flatMap(this::enrichEntryWithUserDetails);
    }

    public Mono<SlamBookEntryResponse> addReaction(String entryId, CustomUserDetails userDetails, ReactionRequest request) {
        return slamBookEntryRepository.findById(entryId)
                .switchIfEmpty(Mono.error(new NotFoundException("Entry not found")))
                .flatMap(entry -> {
                    if (entry.getReactions() == null) {
                        entry.setReactions(new ArrayList<>());
                    }

                    // Remove existing reaction from this user
                    entry.getReactions().removeIf(r -> r.getUserId().equals(userDetails.getUserId()));

                    // Add new reaction
                    SlamBookEntry.Reaction reaction = SlamBookEntry.Reaction.builder()
                            .userId(userDetails.getUserId())
                            .type(request.getType())
                            .createdAt(LocalDateTime.now())
                            .build();

                    entry.getReactions().add(reaction);
                    entry.setUpdatedAt(LocalDateTime.now());

                    return slamBookEntryRepository.save(entry);
                })
                .flatMap(entry -> {
                    // Send notification
                    return notificationService.sendReactionNotification(entry, userDetails.getUserId())
                            .thenReturn(entry);
                })
                .flatMap(this::enrichEntryWithUserDetails);
    }

    public Mono<SlamBookEntryResponse> removeReaction(String entryId, CustomUserDetails userDetails) {
        return slamBookEntryRepository.findById(entryId)
                .switchIfEmpty(Mono.error(new NotFoundException("Entry not found")))
                .flatMap(entry -> {
                    if (entry.getReactions() != null) {
                        entry.getReactions().removeIf(r -> r.getUserId().equals(userDetails.getUserId()));
                        entry.setUpdatedAt(LocalDateTime.now());
                        return slamBookEntryRepository.save(entry);
                    }
                    return Mono.just(entry);
                })
                .flatMap(this::enrichEntryWithUserDetails);
    }

    public Mono<SlamBookEntryResponse> reportEntry(String entryId, CustomUserDetails userDetails, String reason) {
        return slamBookEntryRepository.findById(entryId)
                .switchIfEmpty(Mono.error(new NotFoundException("Entry not found")))
                .flatMap(entry -> {
                    entry.setIsReported(true);
                    entry.setReportReason(reason);
                    entry.setReportedBy(userDetails.getUserId());
                    entry.setReportedAt(LocalDateTime.now());

                    return slamBookEntryRepository.save(entry);
                })
                .flatMap(this::enrichEntryWithUserDetails);
    }

    public Flux<SlamBookEntryResponse> getReportedEntries(CustomUserDetails userDetails) {
        return slamBookEntryRepository.findByCollegeIdAndIsReported(userDetails.getCollegeId(), true)
                .flatMap(this::enrichEntryWithUserDetails);
    }

    private Mono<SlamBookEntryResponse> enrichEntryWithUserDetails(SlamBookEntry entry) {
        Mono<UserBasicInfo> writtenForUserMono = userService.getUserBasicInfo(entry.getWrittenFor());

        Mono<UserBasicInfo> writtenByUserMono = entry.getIsAnonymous()
                ? Mono.just(UserBasicInfo.builder().id("anonymous").firstName("Anonymous").build())
                : userService.getUserBasicInfo(entry.getWrittenBy());

        return Mono.zip(writtenForUserMono, writtenByUserMono)
                .map(tuple -> {
                    UserBasicInfo writtenForUser = tuple.getT1();
                    UserBasicInfo writtenByUser = tuple.getT2();

                    List<ReactionResponse> reactionResponses = new ArrayList<>();
                    if (entry.getReactions() != null) {
                        reactionResponses = entry.getReactions().stream()
                                .map(r -> ReactionResponse.builder()
                                        .userId(r.getUserId())
                                        .type(r.getType())
                                        .createdAt(r.getCreatedAt())
                                        .build())
                                .collect(Collectors.toList());
                    }

                    return SlamBookEntryResponse.builder()
                            .id(entry.getId())
                            .writtenFor(entry.getWrittenFor())
                            .writtenForUser(writtenForUser)
                            .writtenBy(entry.getWrittenBy())
                            .writtenByUser(writtenByUser)
                            .isAnonymous(entry.getIsAnonymous())
                            .responses(entry.getResponses())
                            .ratings(entry.getRatings())
                            .attachments(entry.getAttachments())
                            .reactions(reactionResponses)
                            .visibility(entry.getVisibility())
                            .createdAt(entry.getCreatedAt())
                            .updatedAt(entry.getUpdatedAt())
                            .build();
                });
    }
}