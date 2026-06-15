package com.oriole.wisepen.resource.domain.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 顶级评论 Base 类，存储集合 wisepen_resource_comments
 */
@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceCommentBase extends ResourceCommentContentBase {
    private String resourceId;
    private Integer replyCount = 0;
}
