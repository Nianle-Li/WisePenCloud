package com.oriole.wisepen.resource.repository;

import com.oriole.wisepen.resource.domain.entity.ResourceUserInteractionRecordEntity;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 用户资源互动记录自定义 Repository，封装需要原子读写语义的操作。
 */
@Repository
public class CustomResourceUserInteractionRecordRepository {

    private final MongoTemplate mongoTemplate;

    public CustomResourceUserInteractionRecordRepository(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    private ResourceUserInteractionRecordEntity findAndSetField(String resourceId, String userId, String field, Object value) {
        Query query = Query.query(Criteria.where("resourceId").is(resourceId).and("userId").is(userId));

        Update update = new Update()
                .set(field, value)
                .setOnInsert("resourceId", resourceId)
                .setOnInsert("userId", userId);

        return mongoTemplate.findAndModify(
                query, update,
                FindAndModifyOptions.options().upsert(true).returnNew(false),
                ResourceUserInteractionRecordEntity.class);
    }

    /**
     * 原子写入阅读状态
     */
    public ResourceUserInteractionRecordEntity findAndSetRead(String resourceId, String userId, boolean read) {
        return findAndSetField(resourceId, userId, "read", read);
    }

    /**
     * 原子写入点赞状态
     */
    public ResourceUserInteractionRecordEntity findAndSetLiked(String resourceId, String userId, boolean liked) {
        return findAndSetField(resourceId, userId, "liked", liked);
    }

    /**
     * 原子写入评分
     */
    public ResourceUserInteractionRecordEntity findAndSetScore(String resourceId, String userId, int score) {
        return findAndSetField(resourceId, userId, "score", score);
    }

    /**
     * 原子地将 collectionId 加入收藏集合集，返回操作前集合是否为空（首次收藏标志）。
     * 并发安全：$addToSet + findAndModify(returnNew=false) 消除 check-then-act 竞态窗口。
     */
    public boolean addToFavoritedCollections(String resourceId, String userId, String collectionId) {
        Query query = Query.query(Criteria.where("resourceId").is(resourceId).and("userId").is(userId));
        Update update = new Update()
                .addToSet("favoritedCollectionIds", collectionId)
                .setOnInsert("resourceId", resourceId)
                .setOnInsert("userId", userId)
                .setOnInsert("read", false)
                .setOnInsert("liked", false);
        ResourceUserInteractionRecordEntity old = mongoTemplate.findAndModify(
                query, update,
                FindAndModifyOptions.options().upsert(true).returnNew(false),
                ResourceUserInteractionRecordEntity.class);
        // old == null 表示文档刚由 upsert 创建，必为首次收藏
        return old == null
                || old.getFavoritedCollectionIds() == null
                || old.getFavoritedCollectionIds().isEmpty();
    }

    /**
     * 原子地将 collectionId 从收藏集合集中移除，返回操作前集合是否仅含该 collectionId（最后一次收藏标志）。
     * 并发安全：$pull + findAndModify(returnNew=false) 消除 check-then-act 竞态窗口。
     */
    public boolean removeFromFavoritedCollections(String resourceId, String userId, String collectionId) {
        Query query = Query.query(Criteria.where("resourceId").is(resourceId).and("userId").is(userId));
        Update update = new Update().pull("favoritedCollectionIds", collectionId);
        ResourceUserInteractionRecordEntity old = mongoTemplate.findAndModify(
                query, update,
                FindAndModifyOptions.options().returnNew(false),
                ResourceUserInteractionRecordEntity.class);
        if (old == null || old.getFavoritedCollectionIds() == null) {
            return false;
        }
        return old.getFavoritedCollectionIds().size() == 1
                && old.getFavoritedCollectionIds().contains(collectionId);
    }

    /**
     * 批量将 collectionId 从一批资源的收藏集合集中移除（供 deleteCollection 级联同步使用）。
     */
    public void batchPullCollectionFromFavoritedSet(String userId, List<String> resourceIds, String collectionId) {
        if (resourceIds.isEmpty()) {
            return;
        }
        Query query = Query.query(Criteria.where("userId").is(userId).and("resourceId").in(resourceIds));
        Update update = new Update().pull("favoritedCollectionIds", collectionId);
        mongoTemplate.updateMulti(query, update, ResourceUserInteractionRecordEntity.class);
    }
}
