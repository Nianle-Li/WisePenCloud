package com.oriole.wisepen.resource.domain.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 回复 Base 类（全部层级 flat 存储），存储集合 wisepen_resource_comment_replies
 */
@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceCommentReplyBase extends ResourceCommentContentBase {
    /** 所属顶级评论 ID，冗余存储以支持索引 */
    private String rootCommentId;
    /** 冗余存储资源 ID，用于资源删除时单步批量软删 */
    private String resourceId;
    /** 被回复人用户 ID，前端展示"回复 xxx" */
    private String replyToUserId;
}
