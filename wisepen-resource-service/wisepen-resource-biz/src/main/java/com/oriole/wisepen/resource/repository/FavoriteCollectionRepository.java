package com.oriole.wisepen.resource.repository;

import com.oriole.wisepen.resource.domain.entity.FavoriteCollectionEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface FavoriteCollectionRepository extends MongoRepository<FavoriteCollectionEntity, String> {
    /** 默认收藏集合置顶，其余按创建时间升序 */
    List<FavoriteCollectionEntity> findByUserIdOrderByIsDefaultDescCreateTimeAsc(String userId);
}
