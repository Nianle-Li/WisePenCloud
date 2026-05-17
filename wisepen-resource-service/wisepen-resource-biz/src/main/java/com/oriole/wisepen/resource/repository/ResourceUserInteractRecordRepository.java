package com.oriole.wisepen.resource.repository;

import com.oriole.wisepen.resource.domain.entity.ResourceUserInteractRecordEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ResourceUserInteractRecordRepository extends MongoRepository<ResourceUserInteractRecordEntity, String> {
    Optional<ResourceUserInteractRecordEntity> findByUserIdAndResourceId(String userId, String resourceId);

    List<ResourceUserInteractRecordEntity> findByUserIdAndResourceIdIn(String userId, List<String> resourceIds);

    void deleteAllByResourceIdIn(List<String> resourceIds);
}
