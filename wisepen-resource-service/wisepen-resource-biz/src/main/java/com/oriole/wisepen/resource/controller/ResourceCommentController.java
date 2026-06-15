package com.oriole.wisepen.resource.controller;

import com.oriole.wisepen.common.core.context.SecurityContextHolder;
import com.oriole.wisepen.common.core.domain.R;
import com.oriole.wisepen.common.core.domain.enums.BusinessType;
import com.oriole.wisepen.common.log.annotation.Log;
import com.oriole.wisepen.common.security.annotation.CheckLogin;
import com.oriole.wisepen.resource.domain.dto.req.CreateCommentRequest;
import com.oriole.wisepen.resource.domain.dto.req.CreateReplyRequest;
import com.oriole.wisepen.resource.domain.dto.req.DeleteCommentItemRequest;
import com.oriole.wisepen.resource.domain.dto.req.ToggleCommentLikeRequest;
import com.oriole.wisepen.resource.domain.dto.res.CursorPageResponse;
import com.oriole.wisepen.resource.domain.dto.res.ResourceCommentListItemResponse;
import com.oriole.wisepen.resource.domain.dto.res.ResourceCommentReplyListItemResponse;
import com.oriole.wisepen.resource.service.IResourceCommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "资源评论", description = "资源评论区的发布、删除、点赞与分页查询")
@RestController
@RequestMapping("/resource/comment")
@RequiredArgsConstructor
@CheckLogin
@Validated
public class ResourceCommentController {

    private final IResourceCommentService commentService;

    @Operation(
            summary = "发布顶级评论",
            description = """
                    - 用途：用户在资源展示页发布顶级评论。
                    - 请求：resourceId 指定目标资源；content 为评论正文，不能为空；imageUrls 为可选图片 URL 列表。
                    - 约束：当前用户必须已登录；目标资源必须存在且未被软删除。
                    - 处理：插入顶级评论文档，authorId 取当前登录用户；资源 commentCount +1。
                    - 失败：未登录 -> PermissionError.NOT_LOGIN；目标资源不存在或已删除 -> ResourceError.RESOURCE_NOT_FOUND。
                    - 响应：返回服务端生成的 commentId。
                    """
    )
    @PostMapping("/createComment")
    @Log(title = "发布顶级评论", businessType = BusinessType.INSERT)
    public R<String> createComment(@Validated @RequestBody CreateCommentRequest request) {
        String operatorUserId = SecurityContextHolder.getUserId().toString();
        return R.ok(commentService.createComment(request, operatorUserId));
    }

    @Operation(
            summary = "发布回复",
            description = """
                    - 用途：用户对某条顶级评论或某条回复进行回复。
                    - 请求：parentId 为被回复目标的 ID（顶级评论 ID 或回复 ID）；replyToUserId 为被回复人用户 ID；content 不能为空；imageUrls 可选。
                    - 约束：当前用户必须已登录；parentId 对应目标必须存在且未被软删除。
                    - 处理：生成回复 ID 为 parentId_newObjectId；所属顶级评论 replyCount +1；资源 commentCount +1。
                    - 失败：未登录 -> PermissionError.NOT_LOGIN；parentId 对应顶级评论不存在或已删除 -> ResourceError.COMMENT_NOT_FOUND；parentId 为回复 ID 时对应回复不存在或已删除 -> ResourceError.COMMENT_REPLY_PARENT_NOT_FOUND。
                    - 响应：返回服务端生成的 replyId。
                    """
    )
    @PostMapping("/createReply")
    @Log(title = "发布回复", businessType = BusinessType.INSERT)
    public R<String> createReply(@Validated @RequestBody CreateReplyRequest request) {
        String operatorUserId = SecurityContextHolder.getUserId().toString();
        return R.ok(commentService.createReply(request, operatorUserId));
    }

    @Operation(
            summary = "删除评论或回复",
            description = """
                    - 用途：用户软删除自己发布的顶级评论或回复。
                    - 请求：targetId 为 commentId（不含 _）或 replyId（含 _），Service 从 ID 格式推导操作目标类型。
                    - 约束：当前用户必须已登录；目标必须存在且未被软删除；操作人必须是该评论/回复的 authorId。
                    - 处理（顶级评论）：软删除评论；资源 commentCount -1；不级联删除回复。
                    - 处理（回复）：软删除回复；所属顶级评论 replyCount -1；资源 commentCount -1。
                    - 失败：未登录 -> PermissionError.NOT_LOGIN；顶级评论不存在或已删除 -> ResourceError.COMMENT_NOT_FOUND；回复不存在或已删除 -> ResourceError.COMMENT_REPLY_NOT_FOUND；操作人非 authorId -> ResourceError.COMMENT_DELETE_ACCESS_DENIED。
                    - 响应：成功时返回空结果。
                    """
    )
    @PostMapping("/deleteCommentItem")
    @Log(title = "删除评论或回复", businessType = BusinessType.DELETE)
    public R<Void> deleteCommentItem(@Validated @RequestBody DeleteCommentItemRequest request) {
        String operatorUserId = SecurityContextHolder.getUserId().toString();
        commentService.deleteCommentItem(request, operatorUserId);
        return R.ok();
    }

    @Operation(
            summary = "切换评论点赞状态",
            description = """
                    - 用途：用户对顶级评论或回复进行点赞或取消点赞（切换语义）。
                    - 请求：targetId 为目标评论/回复的 ID；无需传 targetType，Service 从 ID 格式推导。
                    - 约束：当前用户必须已登录；目标评论/回复必须存在且未被软删除。
                    - 处理：$addToSet/$pull 维护 likedCommentIds；同步 $inc 目标 likeCount。
                    - 失败：未登录 -> PermissionError.NOT_LOGIN；顶级评论不存在或已删除 -> ResourceError.COMMENT_NOT_FOUND；回复不存在或已删除 -> ResourceError.COMMENT_REPLY_NOT_FOUND。
                    - 响应：返回操作后的点赞状态（true 表示已点赞）。
                    """
    )
    @PostMapping("/toggleLike")
    @Log(title = "切换评论点赞状态", businessType = BusinessType.UPDATE)
    public R<Boolean> toggleLike(@Validated @RequestBody ToggleCommentLikeRequest request) {
        String operatorUserId = SecurityContextHolder.getUserId().toString();
        return R.ok(commentService.toggleLike(request, operatorUserId));
    }

    @Operation(
            summary = "游标分页查询顶级评论",
            description = """
                    - 用途：加载资源评论区首屏或下一页顶级评论列表。
                    - 请求：resourceId 必传；sortBy 可选（TIME / HOT，默认 TIME）；首页不传游标参数；热度排序翻页同时传 cursorLikeCount 和 cursorCreateTime；size 默认 10，最大 50。
                    - 约束：当前用户必须已登录。
                    - 处理：游标分页查询顶级评论（含已软删除），批量补 authorInfo，批量补点赞状态；不内嵌回复列表。
                    - 失败：未登录 -> PermissionError.NOT_LOGIN。
                    - 响应：CursorPageResponse 含顶级评论列表、hasMore、nextCursorCreateTime、nextCursorLikeCount（热度排序时有值）。
                    """
    )
    @GetMapping("/listComments")
    public R<CursorPageResponse<ResourceCommentListItemResponse>> listComments(
            @NotBlank @RequestParam String resourceId,
            @RequestParam(defaultValue = "TIME") String sortBy,
            @RequestParam(required = false) Long cursorCreateTime,
            @RequestParam(required = false) Integer cursorLikeCount,
            @Min(1) @Max(50) @RequestParam(defaultValue = "10") int size,
            @Min(1) @RequestParam(defaultValue = "1") int page) {
        String operatorUserId = SecurityContextHolder.getUserId().toString();
        return R.ok(commentService.listComments(resourceId, sortBy, cursorCreateTime, cursorLikeCount, size, page, operatorUserId));
    }

    @Operation(
            summary = "游标分页查询回复列表",
            description = """
                    - 用途：展开某顶级评论的回复弹窗，加载该评论下全部回复。
                    - 请求：rootCommentId 必传；首页不传 cursorCreateTime；翻页传上一页最后一条的 cursorCreateTime（毫秒时间戳）；size 默认 10，最大 50。
                    - 约束：当前用户必须已登录。
                    - 处理：按时间从新到旧游标分页，批量补 authorInfo 和 replyToUserInfo，批量补点赞状态；回复列表平铺返回。
                    - 失败：未登录 -> PermissionError.NOT_LOGIN。
                    - 响应：CursorPageResponse 含回复列表、hasMore、nextCursorCreateTime。
                    """
    )
    @GetMapping("/listReplies")
    public R<CursorPageResponse<ResourceCommentReplyListItemResponse>> listReplies(
            @NotBlank @RequestParam String rootCommentId,
            @RequestParam(required = false) Long cursorCreateTime,
            @Min(1) @Max(50) @RequestParam(defaultValue = "10") int size,
            @Min(1) @RequestParam(defaultValue = "1") int page) {
        String operatorUserId = SecurityContextHolder.getUserId().toString();
        return R.ok(commentService.listReplies(rootCommentId, cursorCreateTime, size, page, operatorUserId));
    }
}
