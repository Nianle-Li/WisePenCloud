package com.oriole.wisepen.resource.repository;

import com.oriole.wisepen.resource.domain.entity.ResourceCommentEntity;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class CustomResourceCommentRepository {

    private final MongoTemplate mongoTemplate;

    public CustomResourceCommentRepository(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * 时间排序游标分页查询顶级评论（含已软删除，后置内存过滤）
     * 命中索引 {resourceId: 1, createTime: -1}
     * 查询 size+1 条，由 Service 判断 hasMore
     */
    public List<ResourceCommentEntity> listByResourceIdOrderByTimeDesc(
            String resourceId, LocalDateTime cursorCreateTime, int size) {
        Criteria criteria = Criteria.where("resourceId").is(resourceId);
        if (cursorCreateTime != null) {
            criteria.and("createTime").lt(cursorCreateTime);
        }
        Query query = Query.query(criteria).limit(size + 1);
        query.with(Sort.by(Sort.Direction.DESC, "createTime"));
        return mongoTemplate.find(query, ResourceCommentEntity.class);
    }

    /**
     * 热度排序游标分页查询顶级评论（含已软删除，后置内存过滤）
     * 命中索引 {resourceId: 1, likeCount: -1, createTime: -1}
     * 查询 size+1 条，由 Service 判断 hasMore
     */
    public List<ResourceCommentEntity> listByResourceIdOrderByHotDesc(
            String resourceId, Integer cursorLikeCount, LocalDateTime cursorCreateTime, int size) {
        Criteria criteria = Criteria.where("resourceId").is(resourceId);
        if (cursorLikeCount != null && cursorCreateTime != null) {
            // likeCount < cursor 或 (likeCount = cursor 且 createTime < cursor)
            criteria.andOperator(new Criteria().orOperator(
                    Criteria.where("likeCount").lt(cursorLikeCount),
                    new Criteria().andOperator(
                            Criteria.where("likeCount").is(cursorLikeCount),
                            Criteria.where("createTime").lt(cursorCreateTime)
                    )
            ));
        }
        Query query = Query.query(criteria).limit(size + 1);
        query.with(Sort.by(Sort.Direction.DESC, "likeCount", "createTime"));
        return mongoTemplate.find(query, ResourceCommentEntity.class);
    }

    /** 原子 $inc 更新回复数 */
    public void updateReplyCount(String commentId, int delta) {
        Query query = Query.query(Criteria.where("_id").is(commentId));
        Update update = new Update().inc("replyCount", delta);
        mongoTemplate.updateFirst(query, update, ResourceCommentEntity.class);
    }

    /** 原子 $inc 更新评论点赞数 */
    public void updateLikeCount(String commentId, int delta) {
        Query query = Query.query(Criteria.where("_id").is(commentId));
        Update update = new Update().inc("likeCount", delta);
        mongoTemplate.updateFirst(query, update, ResourceCommentEntity.class);
    }
}
