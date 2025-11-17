package com.slambook.repository;

import com.slambook.model.Notification;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface NotificationRepository extends ReactiveMongoRepository<Notification, String> {
    Flux<Notification> findByUserId(String userId);
    Flux<Notification> findByUserIdAndIsRead(String userId, Boolean isRead);
    Flux<Notification> findByCollegeId(String collegeId);

    Mono<Long> countByUserIdAndIsRead(String userId, Boolean isRead);
    Mono<Void> deleteByUserIdAndIsRead(String userId, Boolean isRead);
}
