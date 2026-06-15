package com.oriole.wisepen.resource.service;

import com.oriole.wisepen.resource.domain.dto.req.CreateCommentRequest;
import com.oriole.wisepen.resource.domain.dto.req.CreateReplyRequest;
import com.oriole.wisepen.resource.domain.dto.req.DeleteCommentItemRequest;
import com.oriole.wisepen.resource.domain.dto.req.ToggleCommentLikeRequest;
import com.oriole.wisepen.resource.domain.dto.res.CursorPageResponse;
import com.oriole.wisepen.resource.domain.dto.res.ResourceCommentListItemResponse;
import com.oriole.wisepen.resource.domain.dto.res.ResourceCommentReplyListItemResponse;

public interface IResourceCommentService {

    String createComment(CreateCommentRequest request, String operatorUserId);

    String createReply(CreateReplyRequest request, String operatorUserId);

    void deleteCommentItem(DeleteCommentItemRequest request, String operatorUserId);

    boolean toggleLike(ToggleCommentLikeRequest request, String operatorUserId);

    CursorPageResponse<ResourceCommentListItemResponse> listComments(
            String resourceId, String sortBy, Long cursorCreateTime, Integer cursorLikeCount,
            int size, int page, String operatorUserId);

    CursorPageResponse<ResourceCommentReplyListItemResponse> listReplies(
            String rootCommentId, Long cursorCreateTime, int size, int page, String operatorUserId);
}
