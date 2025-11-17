package com.slambook.repository;

import com.slambook.model.Template;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface TemplateRepository extends ReactiveMongoRepository<Template, String> {
    Flux<Template> findByCollegeId(String collegeId);
    Flux<Template> findByCollegeIdIsNull();  // System templates
    Flux<Template> findByCollegeIdAndIsActive(String collegeId, Boolean isActive);
    Mono<Template> findByCollegeIdAndIsDefault(String collegeId, Boolean isDefault);
    Flux<Template> findByCollegeIdIsNullAndIsActive(Boolean isActive);
}
