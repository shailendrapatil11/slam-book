package com.slambook.repository;

import com.slambook.model.User;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface UserRepository extends ReactiveMongoRepository<User, String> {
    Mono<User> findByEmailAndCollegeId(String email, String collegeId);
    Mono<User> findByEmail(String email);
    Mono<Boolean> existsByEmailAndCollegeId(String email, String collegeId);

    Flux<User> findByCollegeId(String collegeId);
    Flux<User> findByCollegeIdAndIsActive(String collegeId, Boolean isActive);
    Flux<User> findByCollegeIdAndRole(String collegeId, User.UserRole role);
    Flux<User> findByCollegeIdAndJoinRequest_Status(String collegeId, User.JoinRequestStatus status);

    Mono<Long> countByCollegeId(String collegeId);
    Mono<Long> countByCollegeIdAndIsActive(String collegeId, Boolean isActive);
    Mono<Long> countByCollegeIdAndJoinRequest_Status(String collegeId, User.JoinRequestStatus status);

    Mono<User> findByVerificationToken(String token);
}
