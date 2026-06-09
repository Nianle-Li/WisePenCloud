package com.oriole.wisepen.resource.repository;

import com.oriole.wisepen.resource.domain.entity.FavoriteCollectionEntity;
import com.oriole.wisepen.resource.domain.entity.FavoriteResourceRef;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** 收藏集合自定义 Repository，封装 resources 数组的原子操作 */
@Repository
public class CustomFavoriteCollectionRepository {

    private final MongoTemplate mongoTemplate;
    private final FavoriteCollectionRepository favoriteCollectionRepository;

    public CustomFavoriteCollectionRepository(MongoTemplate mongoTemplate,
                                              FavoriteCollectionRepository favoriteCollectionRepository) {
        this.mongoTemplate = mongoTemplate;
        this.favoriteCollectionRepository = favoriteCollectionRepository;
    }

    /** 获取或创建默认收藏集合；默认 collectionName 为空 */
    public FavoriteCollectionEntity findOrCreateDefaultCollection(String userId) {
        Optional<FavoriteCollectionEntity> existing =
                favoriteCollectionRepository.findFirstByUserIdAndIsDefaultTrue(userId);
        if (existing.isPresent()) {
            return existing.get();
        }
        FavoriteCollectionEntity newCollection = new FavoriteCollectionEntity(userId, "", null, true);
        return favoriteCollectionRepository.save(newCollection);
    }

    /** 向 resources 追加收藏引用（调用方已确保未存在） */
    public void addResource(String collectionId, FavoriteResourceRef ref) {
        Query query = Query.query(Criteria.where("_id").is(collectionId));
        Update update = new Update().push("resources").value(ref);
        mongoTemplate.updateFirst(query, update, FavoriteCollectionEntity.class);
    }

    /** 从 resources 中移除指定 resourceId */
    public void removeResource(String collectionId, String resourceId) {
        Query query = Query.query(Criteria.where("_id").is(collectionId));
        Update update = new Update().pull("resources", Query.query(Criteria.where("resourceId").is(resourceId)));
        mongoTemplate.updateFirst(query, update, FavoriteCollectionEntity.class);
    }

    /** 从指定用户的所有集合中移除 resourceId */
    public void removeResourceFromAllCollections(String userId, String resourceId) {
        Query query = Query.query(Criteria.where("userId").is(userId));
        Update update = new Update().pull("resources", Query.query(Criteria.where("resourceId").is(resourceId)));
        mongoTemplate.updateMulti(query, update, FavoriteCollectionEntity.class);
    }

    /** 从所有用户集合中批量移除 resourceIds（GC 用），一次 updateMulti */
    public void removeResourcesFromAllCollections(List<String> resourceIds) {
        if (resourceIds == null || resourceIds.isEmpty()) return;
        Update update = new Update().pull("resources", Query.query(Criteria.where("resourceId").in(resourceIds)));
        mongoTemplate.updateMulti(new Query(), update, FavoriteCollectionEntity.class);
    }
}
