package com.oriole.wisepen.resource.domain.dto.res;

import com.oriole.wisepen.user.api.domain.base.UserDisplayBase;
import lombok.Data;

import java.util.List;

@Data
public class ResourceCommentListItemResponse {
    private String commentId;
    private String resourceId;
    private UserDisplayBase authorInfo;
    private String content;
    private List<String> imageUrls;
    private Integer likeCount;
    private Boolean liked;
    private Integer replyCount;
    private Long createTime;
    /** true 时前端展示"评论已删除"占位 */
    private Boolean deleted;
}
