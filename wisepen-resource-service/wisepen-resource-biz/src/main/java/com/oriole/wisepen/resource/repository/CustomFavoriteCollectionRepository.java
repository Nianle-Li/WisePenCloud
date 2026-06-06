package com.oriole.wisepen.resource.repository;

import com.oriole.wisepen.resource.domain.entity.FavoriteCollectionEntity;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * 收藏集合自定义 Repository，封装原子操作
 */
@Repository
public class CustomFavoriteCollectionRepository {

    private final MongoTemplate mongoTemplate;

    public CustomFavoriteCollectionRepository(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * 原子获取或创建默认收藏集合，保证并发安全的懒初始化。
     * @return 默认收藏集合的 collectionId
     */
    public String findOrCreateDefaultCollection(String userId) {
        Query query = Query.query(Criteria.where("userId").is(userId).and("isDefault").is(true));
        Update update = new Update()
                .setOnInsert("userId", userId)
                .setOnInsert("collectionName", "我的收藏")
                .setOnInsert("isDefault", true)
                .setOnInsert("createTime", LocalDateTime.now());
        FindAndModifyOptions options = FindAndModifyOptions.options().upsert(true).returnNew(true);
        FavoriteCollectionEntity collection = mongoTemplate.findAndModify(query, update, options, FavoriteCollectionEntity.class);
        return collection.getCollectionId();
    }
}
