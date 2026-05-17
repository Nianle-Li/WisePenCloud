package com.oriole.wisepen.resource.repository;

import com.oriole.wisepen.resource.domain.entity.ResourceInteractInfoEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ResourceInteractInfoRepository extends MongoRepository<ResourceInteractInfoEntity, String> {
    List<ResourceInteractInfoEntity> findByResourceIdIn(List<String> resourceIds);

    void deleteAllByResourceIdIn(List<String> resourceIds);
}
