package com.oriole.wisepen.resource.domain.entity;

import com.oriole.wisepen.resource.domain.base.ResourceCommentReplyBase;
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
 * 回复 Entity，全部层级 flat 存储，对应集合 wisepen_resource_comment_replies
 */
@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "wisepen_resource_comment_replies")
@CompoundIndexes({
        @CompoundIndex(def = "{'rootCommentId': 1, 'createTime': -1}"),
        @CompoundIndex(def = "{'resourceId': 1}")
})
public class ResourceCommentReplyEntity extends ResourceCommentReplyBase {
    @Id
    private String replyId;

    @CreatedDate
    private LocalDateTime createTime;

    /** null 表示未软删除 */
    private LocalDateTime deletedAt;
}
