package com.oriole.wisepen.resource.domain.dto.res;

import com.oriole.wisepen.user.api.domain.base.UserDisplayBase;
import lombok.Data;

import java.util.List;

@Data
public class ResourceCommentReplyListItemResponse {
    private String replyId;
    private String rootCommentId;
    /** 由 replyId 推导（去掉最后 _<segment>），父节点为顶级评论时为 null */
    private String parentReplyId;
    private UserDisplayBase authorInfo;
    private UserDisplayBase replyToUserInfo;
    private String content;
    private List<String> imageUrls;
    private Integer likeCount;
    private Boolean liked;
    private Long createTime;
    /** true 时前端展示"回复已删除"占位 */
    private Boolean deleted;
}
