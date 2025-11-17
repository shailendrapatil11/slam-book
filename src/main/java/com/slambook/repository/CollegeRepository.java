package com.slambook.repository;

import com.slambook.model.College;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface CollegeRepository extends ReactiveMongoRepository<College, String> {
    Mono<College> findByCollegeCode(String collegeCode);
    Mono<Boolean> existsByCollegeCode(String collegeCode);
    Mono<Boolean> existsByEmail(String email);
    Flux<College> findByIsActive(Boolean isActive);
    Flux<College> findBySubscriptionStatus(College.SubscriptionStatus status);
}
