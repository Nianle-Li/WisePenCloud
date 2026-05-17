package com.oriole.wisepen.resource.domain.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * 用户维度互动记录，存储每个用户对每个资源的点赞状态与评分。
 * 仅供 resource-biz 内部使用，不通过 Feign 对外暴露。
 */
@Data
@Document(collection = "wisepen_resource_user_interact_record")
@CompoundIndex(def = "{'resourceId': 1, 'userId': 1}", unique = true)
public class ResourceUserInteractRecordEntity {
    @Id
    private String id;

    private String resourceId;
    private String userId;
    /** 是否点赞；null 表示从未点过赞（仅评分而未点赞的场景） */
    private Boolean liked;
    /** 评分（1-5）；null 表示从未评分 */
    private Integer score;
}
