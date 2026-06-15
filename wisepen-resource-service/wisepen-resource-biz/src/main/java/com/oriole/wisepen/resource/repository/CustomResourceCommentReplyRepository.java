package com.oriole.wisepen.resource.repository;

import com.oriole.wisepen.resource.domain.entity.ResourceCommentReplyEntity;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class CustomResourceCommentReplyRepository {

    private final MongoTemplate mongoTemplate;

    public CustomResourceCommentReplyRepository(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * 时间排序游标分页查询回复列表（含已软删除，后置内存过滤）
     * 命中索引 {rootCommentId: 1, createTime: -1}
     * 查询 size+1 条，由 Service 判断 hasMore
     */
    public List<ResourceCommentReplyEntity> listByRootCommentIdOrderByTimeDesc(
            String rootCommentId, LocalDateTime cursorCreateTime, int size) {
        Criteria criteria = Criteria.where("rootCommentId").is(rootCommentId);
        if (cursorCreateTime != null) {
            criteria.and("createTime").lt(cursorCreateTime);
        }
        Query query = Query.query(criteria).limit(size + 1);
        query.with(Sort.by(Sort.Direction.DESC, "createTime"));
        return mongoTemplate.find(query, ResourceCommentReplyEntity.class);
    }

    /** 原子 $inc 更新回复点赞数 */
    public void updateLikeCount(String replyId, int delta) {
        Query query = Query.query(Criteria.where("_id").is(replyId));
        Update update = new Update().inc("likeCount", delta);
        mongoTemplate.updateFirst(query, update, ResourceCommentReplyEntity.class);
    }
}
