package com.oriole.wisepen.resource.domain.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * 评论/回复公共内容字段，顶级评论和回复均继承此类
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceCommentContentBase {
    private String authorId;
    private String content;
    private List<String> imageUrls = new ArrayList<>();
    private Integer likeCount = 0;
}
