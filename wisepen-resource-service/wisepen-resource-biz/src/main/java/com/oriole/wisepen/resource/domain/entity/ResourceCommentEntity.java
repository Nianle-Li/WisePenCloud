package com.oriole.wisepen.resource.domain.entity;

import com.oriole.wisepen.resource.domain.base.ResourceCommentBase;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * 顶级评论 Entity，对应集合 wisepen_resource_comments
 */
@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "wisepen_resource_comments")
@CompoundIndexes({
        @CompoundIndex(def = "{'resourceId': 1, 'createTime': -1}"),
        @CompoundIndex(def = "{'resourceId': 1, 'likeCount': -1, 'createTime': -1}")
})
public class ResourceCommentEntity extends ResourceCommentBase {
    @Id
    private String commentId;

    @CreatedDate
    private LocalDateTime createTime;

    /** null 表示未软删除 */
    private LocalDateTime deletedAt;
}
