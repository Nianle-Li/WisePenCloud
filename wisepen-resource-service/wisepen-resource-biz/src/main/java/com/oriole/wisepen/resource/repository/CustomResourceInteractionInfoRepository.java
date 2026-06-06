package com.oriole.wisepen.resource.repository;

import com.oriole.wisepen.resource.domain.entity.ResourceInteractionInfoEntity;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 资源互动信息自定义 Repository，封装原子更新操作
 */
@Repository
public class CustomResourceInteractionInfoRepository {

    private final MongoTemplate mongoTemplate;

    public CustomResourceInteractionInfoRepository(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /** 原子累加阅读量，upsert 兼容历史遗留资源（文档不存在时自动创建） */
    public void incrementReadCount(String resourceId, int delta) {
        incrementCountField(resourceId, "readCount", delta);
    }

    /** 原子累加点赞数，upsert 兼容历史遗留资源 */
    public void incrementLikeCount(String resourceId, int delta) {
        incrementCountField(resourceId, "likeCount", delta);
    }

    /** 原子累加收藏数，upsert 兼容历史遗留资源 */
    public void incrementFavoriteCount(String resourceId, int delta) {
        incrementCountField(resourceId, "favoriteCount", delta);
    }

    /**
     * 批量递减收藏数（updateMulti，非 upsert），只更新已存在文档。
     * 调用方须确保传入的 resourceId 在其他收藏集合中已无引用。
     */
    public void decrementFavoriteCountForResources(List<String> resourceIds) {
        Query query = Query.query(Criteria.where("_id").in(resourceIds));
        Update update = new Update().inc("favoriteCount", -1);
        mongoTemplate.updateMulti(query, update, ResourceInteractionInfoEntity.class);
    }

    /**
     * 原子累加评分统计（upsert），因需同时操作两个字段，不复用单字段模板，单独实现。
     *
     * @param scoreCountDelta 首次评分传 1，覆盖评分传 0
     * @param scoreTotalDelta 分数增量
     */
    public void updateScoreStats(String resourceId, int scoreCountDelta, int scoreTotalDelta) {
        Query query = Query.query(Criteria.where("_id").is(resourceId));
        Update update = new Update()
                .inc("scoreCount", scoreCountDelta)
                .inc("scoreTotal", scoreTotalDelta)
                .setOnInsert("resourceId", resourceId);
        mongoTemplate.upsert(query, update, ResourceInteractionInfoEntity.class);
    }

    /** 单字段原子 $inc + upsert 公共模板，供各计数方法复用 */
    private void incrementCountField(String resourceId, String field, int delta) {
        Query query = Query.query(Criteria.where("_id").is(resourceId));
        Update update = new Update()
                .inc(field, delta)
                .setOnInsert("resourceId", resourceId);
        mongoTemplate.upsert(query, update, ResourceInteractionInfoEntity.class);
    }
}
