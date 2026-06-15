package com.oriole.wisepen.resource.repository;

import com.oriole.wisepen.resource.domain.entity.ResourceCommentReplyEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ResourceCommentReplyRepository extends MongoRepository<ResourceCommentReplyEntity, String> {

    /** 查询单条未软删除回复，用于父回复存在性校验与归属校验 */
    @Query("{ '_id': ?0, 'deletedAt': null }")
    Optional<ResourceCommentReplyEntity> findByIdAndDeletedAtIsNull(String id);

    /** 仅限资源清理任务使用，不在 API 查询路径中调用 */
    List<ResourceCommentReplyEntity> findByResourceId(String resourceId);
}
