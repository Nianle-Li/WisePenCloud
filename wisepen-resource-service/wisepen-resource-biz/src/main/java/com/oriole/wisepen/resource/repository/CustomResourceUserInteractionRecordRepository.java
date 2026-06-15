package com.oriole.wisepen.resource.repository;

import com.oriole.wisepen.resource.domain.entity.ResourceUserInteractionRecordEntity;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

@Repository
public class CustomResourceUserInteractionRecordRepository {

    private final MongoTemplate mongoTemplate;

    public CustomResourceUserInteractionRecordRepository(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    private ResourceUserInteractionRecordEntity findAndSetField(String resourceId, String userId, String field, Object value) {
        Query query = Query.query(Criteria.where("resourceId").is(resourceId).and("userId").is(userId));
        Update update = new Update().set(field, value);

        return mongoTemplate.findAndModify(
                query, update, FindAndModifyOptions.options().returnNew(false),
                ResourceUserInteractionRecordEntity.class);
    }

    /** 原子写入阅读状态 */
    public ResourceUserInteractionRecordEntity findAndSetRead(String resourceId, String userId, boolean read) {
        return findAndSetField(resourceId, userId, "read", read);
    }

    /** 原子写入点赞状态 */
    public ResourceUserInteractionRecordEntity findAndSetLiked(String resourceId, String userId, boolean liked) {
        return findAndSetField(resourceId, userId, "liked", liked);
    }

    /** 原子写入评分 */
    public ResourceUserInteractionRecordEntity findAndSetScore(String resourceId, String userId, int score) {
        return findAndSetField(resourceId, userId, "score", score);
    }

    /**
     * 查询用户互动记录，若不存在则返回 null（不自动创建）
     * 用于读取当前点赞状态，Service 层判断 targetId 是否在 likedCommentIds 中
     */
    public ResourceUserInteractionRecordEntity findInteractionRecord(String resourceId, String userId) {
        Query query = Query.query(Criteria.where("resourceId").is(resourceId).and("userId").is(userId));
        return mongoTemplate.findOne(query, ResourceUserInteractionRecordEntity.class);
    }

    /** $addToSet 幂等添加评论/回复 ID 到已点赞列表，同时确保记录存在（upsert） */
    public void addToLikedCommentIds(String resourceId, String userId, String targetId) {
        Query query = Query.query(Criteria.where("resourceId").is(resourceId).and("userId").is(userId));
        Update update = new Update().addToSet("likedCommentIds", targetId);
        mongoTemplate.upsert(query, update, ResourceUserInteractionRecordEntity.class);
    }

    /** $pull 从已点赞列表移除评论/回复 ID */
    public void pullFromLikedCommentIds(String resourceId, String userId, String targetId) {
        Query query = Query.query(Criteria.where("resourceId").is(resourceId).and("userId").is(userId));
        Update update = new Update().pull("likedCommentIds", targetId);
        mongoTemplate.updateFirst(query, update, ResourceUserInteractionRecordEntity.class);
    }
}
