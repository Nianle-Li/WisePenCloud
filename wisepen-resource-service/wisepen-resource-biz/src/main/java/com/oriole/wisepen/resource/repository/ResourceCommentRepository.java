package com.oriole.wisepen.resource.repository;

import com.oriole.wisepen.resource.domain.entity.ResourceCommentEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ResourceCommentRepository extends MongoRepository<ResourceCommentEntity, String> {

    /** 查询单条未软删除评论，用于存在性校验与归属校验 */
    @Query("{ '_id': ?0, 'deletedAt': null }")
    Optional<ResourceCommentEntity> findByIdAndDeletedAtIsNull(String id);

    /** 仅限资源清理任务使用，不在 API 查询路径中调用 */
    List<ResourceCommentEntity> findByResourceId(String resourceId);
}
