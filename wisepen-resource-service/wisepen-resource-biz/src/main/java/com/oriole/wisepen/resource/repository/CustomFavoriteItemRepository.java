package com.oriole.wisepen.resource.repository;

import lombok.Data;
import org.bson.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 收藏条目自定义 Repository，封装聚合操作（避免 N+1）
 */
@Repository
public class CustomFavoriteItemRepository {

    private final MongoTemplate mongoTemplate;

    public CustomFavoriteItemRepository(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * 批量统计各收藏夹的条目数，一次 $group 聚合返回所有结果，避免 N+1 查询。
     *
     * @param collectionIds 待统计的收藏集合 ID 列表
     * @return Map&lt;collectionId, itemCount&gt;
     */
    public Map<String, Long> countItemsByCollectionIds(List<String> collectionIds) {
        if (collectionIds.isEmpty()) {
            return new HashMap<>();
        }
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("collectionId").in(collectionIds)),
                Aggregation.group("collectionId").count().as("count")
        );
        AggregationResults<CollectionCountResult> results =
                mongoTemplate.aggregate(agg, "wisepen_favorite_items", CollectionCountResult.class);

        Map<String, Long> countMap = new HashMap<>();
        for (CollectionCountResult r : results) {
            countMap.put(r.getId(), r.getCount());
        }
        return countMap;
    }

    /**
     * "按内容"视图分页：将同一资源的多条收藏记录聚合为一条，去重后分页。
     * 使用 $facet 在一次聚合中同时获取 total 和 data，避免两次查询。
     *
     * @return Spring Data PageImpl（data 中每条含 resourceId、collectionIds、firstFavoritedAt）
     */
    public Page<FavoriteContentAggResult> pageByUserId(String userId, int page, int size) {
        int skip = (page - 1) * size;

        // $facet 中 data 和 total 子管道
        Document dataSubPipeline = new Document("$facet", new Document()
                .append("data", Arrays.asList(
                        new Document("$skip", skip),
                        new Document("$limit", size)
                ))
                .append("total", List.of(
                        new Document("$count", "count")
                ))
        );

        AggregationOperation matchOp = Aggregation.match(Criteria.where("userId").is(userId));
        AggregationOperation groupOp = Aggregation.group("resourceId")
                .push("collectionId").as("collectionIds")
                .min("createTime").as("firstFavoritedAt");
        AggregationOperation sortOp = Aggregation.sort(
                org.springframework.data.domain.Sort.by(
                        org.springframework.data.domain.Sort.Direction.DESC, "firstFavoritedAt"));
        AggregationOperation facetOp = context -> dataSubPipeline;

        Aggregation agg = Aggregation.newAggregation(matchOp, groupOp, sortOp, facetOp);
        AggregationResults<Document> rawResults =
                mongoTemplate.aggregate(agg, "wisepen_favorite_items", Document.class);

        Document facetDoc = rawResults.getUniqueMappedResult();
        if (facetDoc == null) {
            return new PageImpl<>(List.of(), org.springframework.data.domain.PageRequest.of(page - 1, size), 0);
        }

        List<Document> dataDocs = facetDoc.getList("data", Document.class);
        List<Document> totalDocs = facetDoc.getList("total", Document.class);
        long total = totalDocs.isEmpty() ? 0L : ((Number) totalDocs.get(0).get("count")).longValue();

        List<FavoriteContentAggResult> data = dataDocs.stream()
                .map(doc -> {
                    FavoriteContentAggResult r = new FavoriteContentAggResult();
                    r.setResourceId(doc.getString("_id"));
                    r.setCollectionIds(doc.getList("collectionIds", String.class));
                    // MongoDB 聚合原始 Document 中日期类型为 java.util.Date，需手动转换
                    Date date = doc.getDate("firstFavoritedAt");
                    r.setFirstFavoritedAt(date != null
                            ? date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                            : null);
                    return r;
                })
                .toList();

        return new PageImpl<>(data, org.springframework.data.domain.PageRequest.of(page - 1, size), total);
    }

    /** $group 聚合结果映射（内部使用） */
    @Data
    private static class CollectionCountResult {
        private String id;    // $group by collectionId，映射到 _id
        private Long count;
    }

    /** "按内容"聚合结果（内部使用，作为 Service 层的数据载体） */
    @Data
    public static class FavoriteContentAggResult {
        private String resourceId;
        private List<String> collectionIds;
        private LocalDateTime firstFavoritedAt;
    }
}
