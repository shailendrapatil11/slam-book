package com.slambook.repository;

import com.slambook.model.SlamBookEntry;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface SlamBookEntryRepository extends ReactiveMongoRepository<SlamBookEntry, String> {
    Flux<SlamBookEntry> findByCollegeId(String collegeId);
    Flux<SlamBookEntry> findByWrittenFor(String userId);
    Flux<SlamBookEntry> findByWrittenBy(String userId);
    Flux<SlamBookEntry> findByCollegeIdAndWrittenFor(String collegeId, String userId);

    Mono<Boolean> existsByWrittenForAndWrittenBy(String writtenFor, String writtenBy);
    Mono<Long> countByCollegeId(String collegeId);
    Mono<Long> countByWrittenFor(String userId);
    Mono<Long> countByWrittenBy(String userId);

    Flux<SlamBookEntry> findByCollegeIdAndIsReported(String collegeId, Boolean isReported);
}
