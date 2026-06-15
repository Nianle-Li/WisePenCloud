package com.oriole.wisepen.resource.service.impl;

import com.oriole.wisepen.common.core.exception.ServiceException;
import com.oriole.wisepen.resource.domain.dto.req.CreateCommentRequest;
import com.oriole.wisepen.resource.domain.dto.req.CreateReplyRequest;
import com.oriole.wisepen.resource.domain.dto.req.DeleteCommentItemRequest;
import com.oriole.wisepen.resource.domain.dto.req.ToggleCommentLikeRequest;
import com.oriole.wisepen.resource.domain.dto.res.CursorPageResponse;
import com.oriole.wisepen.resource.domain.dto.res.ResourceCommentListItemResponse;
import com.oriole.wisepen.resource.domain.dto.res.ResourceCommentReplyListItemResponse;
import com.oriole.wisepen.resource.domain.entity.ResourceCommentEntity;
import com.oriole.wisepen.resource.domain.entity.ResourceCommentReplyEntity;
import com.oriole.wisepen.resource.domain.entity.ResourceItemEntity;
import com.oriole.wisepen.resource.domain.entity.ResourceUserInteractionRecordEntity;
import com.oriole.wisepen.resource.exception.ResourceError;
import com.oriole.wisepen.resource.repository.CustomResourceCommentRepository;
import com.oriole.wisepen.resource.repository.CustomResourceCommentReplyRepository;
import com.oriole.wisepen.resource.repository.CustomResourceItemRepository;
import com.oriole.wisepen.resource.repository.CustomResourceUserInteractionRecordRepository;
import com.oriole.wisepen.resource.repository.ResourceCommentRepository;
import com.oriole.wisepen.resource.repository.ResourceCommentReplyRepository;
import com.oriole.wisepen.resource.repository.ResourceItemRepository;
import com.oriole.wisepen.resource.service.IResourceCommentService;
import com.oriole.wisepen.user.api.domain.base.UserDisplayBase;
import com.oriole.wisepen.user.api.feign.RemoteUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ResourceCommentServiceImpl implements IResourceCommentService {

    private final ResourceItemRepository resourceItemRepository;
    private final ResourceCommentRepository commentRepository;
    private final ResourceCommentReplyRepository replyRepository;
    private final CustomResourceCommentRepository customCommentRepository;
    private final CustomResourceCommentReplyRepository customReplyRepository;
    private final CustomResourceItemRepository customResourceItemRepository;
    private final CustomResourceUserInteractionRecordRepository customInteractionRecordRepository;
    private final RemoteUserService remoteUserService;

    @Override
    public String createComment(CreateCommentRequest request, String operatorUserId) {
        String resourceId = request.getResourceId();
        ResourceItemEntity resource = resourceItemRepository.findById(resourceId)
                .orElseThrow(() -> new ServiceException(ResourceError.RESOURCE_NOT_FOUND));
        if (resource.getDeletedAt() != null) {
            throw new ServiceException(ResourceError.RESOURCE_NOT_FOUND);
        }
        ResourceCommentEntity comment = new ResourceCommentEntity();
        comment.setResourceId(resourceId);
        comment.setAuthorId(operatorUserId);
        comment.setContent(request.getContent());
        comment.setImageUrls(request.getImageUrls());
        ResourceCommentEntity saved = commentRepository.save(comment);
        customResourceItemRepository.updateCommentCount(resourceId, 1);
        log.info("comment created. resourceId={} commentId={} authorId={}", resourceId, saved.getCommentId(), operatorUserId);
        return saved.getCommentId();
    }

    @Override
    public String createReply(CreateReplyRequest request, String operatorUserId) {
        String parentId = request.getParentId();
        // 从 parentId 推导 rootCommentId：取第一个 _ 前的部分
        String rootCommentId = parentId.contains("_") ? parentId.substring(0, parentId.indexOf('_')) : parentId;

        ResourceCommentEntity rootComment = commentRepository.findByIdAndDeletedAtIsNull(rootCommentId)
                .orElseThrow(() -> new ServiceException(ResourceError.COMMENT_NOT_FOUND));

        // parentId 含 _ 时还需校验父回复存在且未删除
        if (parentId.contains("_")) {
            replyRepository.findByIdAndDeletedAtIsNull(parentId)
                    .orElseThrow(() -> new ServiceException(ResourceError.COMMENT_REPLY_PARENT_NOT_FOUND));
        }

        String resourceId = rootComment.getResourceId();
        // 生成回复 ID：parentId_newObjectId
        String newObjectId = new ObjectId().toHexString();
        String replyId = parentId + "_" + newObjectId;

        ResourceCommentReplyEntity reply = new ResourceCommentReplyEntity();
        reply.setReplyId(replyId);
        reply.setRootCommentId(rootCommentId);
        reply.setResourceId(resourceId);
        reply.setReplyToUserId(request.getReplyToUserId());
        reply.setAuthorId(operatorUserId);
        reply.setContent(request.getContent());
        reply.setImageUrls(request.getImageUrls());
        // @CreatedDate 不会自动填充；此处显式赋值保证 createTime 不为 null
        reply.setCreateTime(LocalDateTime.now());
        replyRepository.save(reply);

        customCommentRepository.updateReplyCount(rootCommentId, 1);
        customResourceItemRepository.updateCommentCount(resourceId, 1);
        log.info("reply created. resourceId={} rootCommentId={} replyId={} authorId={}", resourceId, rootCommentId, replyId, operatorUserId);
        return replyId;
    }

    @Override
    public void deleteCommentItem(DeleteCommentItemRequest request, String operatorUserId) {
        String targetId = request.getTargetId();
        if (targetId.contains("_")) {
            // 删除回复
            ResourceCommentReplyEntity reply = replyRepository.findByIdAndDeletedAtIsNull(targetId)
                    .orElseThrow(() -> new ServiceException(ResourceError.COMMENT_REPLY_NOT_FOUND));
            if (!reply.getAuthorId().equals(operatorUserId)) {
                throw new ServiceException(ResourceError.COMMENT_DELETE_ACCESS_DENIED);
            }
            reply.setDeletedAt(LocalDateTime.now());
            replyRepository.save(reply);
            customCommentRepository.updateReplyCount(reply.getRootCommentId(), -1);
            customResourceItemRepository.updateCommentCount(reply.getResourceId(), -1);
            log.info("reply deleted. replyId={} operatorUserId={}", targetId, operatorUserId);
        } else {
            // 删除顶级评论
            ResourceCommentEntity comment = commentRepository.findByIdAndDeletedAtIsNull(targetId)
                    .orElseThrow(() -> new ServiceException(ResourceError.COMMENT_NOT_FOUND));
            if (!comment.getAuthorId().equals(operatorUserId)) {
                throw new ServiceException(ResourceError.COMMENT_DELETE_ACCESS_DENIED);
            }
            comment.setDeletedAt(LocalDateTime.now());
            commentRepository.save(comment);
            customResourceItemRepository.updateCommentCount(comment.getResourceId(), -1);
            log.info("comment deleted. commentId={} operatorUserId={}", targetId, operatorUserId);
        }
    }

    @Override
    public boolean toggleLike(ToggleCommentLikeRequest request, String operatorUserId) {
        String targetId = request.getTargetId();
        boolean isReply = targetId.contains("_");
        String resourceId;
        if (isReply) {
            ResourceCommentReplyEntity reply = replyRepository.findByIdAndDeletedAtIsNull(targetId)
                    .orElseThrow(() -> new ServiceException(ResourceError.COMMENT_REPLY_NOT_FOUND));
            resourceId = reply.getResourceId();
        } else {
            ResourceCommentEntity comment = commentRepository.findByIdAndDeletedAtIsNull(targetId)
                    .orElseThrow(() -> new ServiceException(ResourceError.COMMENT_NOT_FOUND));
            resourceId = comment.getResourceId();
        }

        // 检查当前点赞状态：加载用户互动记录，判断 targetId 是否在 likedCommentIds
        ResourceUserInteractionRecordEntity record =
                customInteractionRecordRepository.findInteractionRecord(resourceId, operatorUserId);
        boolean alreadyLiked = record != null
                && record.getLikedCommentIds() != null
                && record.getLikedCommentIds().contains(targetId);

        if (alreadyLiked) {
            customInteractionRecordRepository.pullFromLikedCommentIds(resourceId, operatorUserId, targetId);
            if (isReply) customReplyRepository.updateLikeCount(targetId, -1);
            else customCommentRepository.updateLikeCount(targetId, -1);
            log.info("comment like removed. targetId={} resourceId={} operatorUserId={}", targetId, resourceId, operatorUserId);
            return false;
        } else {
            customInteractionRecordRepository.addToLikedCommentIds(resourceId, operatorUserId, targetId);
            if (isReply) customReplyRepository.updateLikeCount(targetId, 1);
            else customCommentRepository.updateLikeCount(targetId, 1);
            log.info("comment like added. targetId={} resourceId={} operatorUserId={}", targetId, resourceId, operatorUserId);
            return true;
        }
    }

    @Override
    public CursorPageResponse<ResourceCommentListItemResponse> listComments(
            String resourceId, String sortBy, Long cursorCreateTime, Integer cursorLikeCount,
            int size, int page, String operatorUserId) {

        size = Math.min(size, 50);

        LocalDateTime cursorTime = cursorCreateTime != null
                ? LocalDateTime.ofInstant(Instant.ofEpochMilli(cursorCreateTime), ZoneId.systemDefault())
                : null;

        List<ResourceCommentEntity> rawList;
        if ("HOT".equalsIgnoreCase(sortBy)) {
            rawList = customCommentRepository.listByResourceIdOrderByHotDesc(resourceId, cursorLikeCount, cursorTime, size);
        } else {
            rawList = customCommentRepository.listByResourceIdOrderByTimeDesc(resourceId, cursorTime, size);
        }

        boolean hasMore = rawList.size() > size;
        List<ResourceCommentEntity> pageData = hasMore ? rawList.subList(0, size) : rawList;

        // 批量查询用户信息，避免 N+1（authorId 由 createComment 强制设置，始终非 null）
        Set<Long> authorIdSet = pageData.stream()
                .filter(c -> c.getDeletedAt() == null)
                .map(c -> Long.parseLong(c.getAuthorId()))
                .collect(Collectors.toSet());
        Map<Long, UserDisplayBase> userMap = authorIdSet.isEmpty()
                ? Map.of()
                : remoteUserService.getUserDisplayInfo(new ArrayList<>(authorIdSet)).getData();

        // 批量获取当前用户点赞状态
        ResourceUserInteractionRecordEntity interactionRecord =
                customInteractionRecordRepository.findInteractionRecord(resourceId, operatorUserId);
        Set<String> likedSet = (interactionRecord != null && interactionRecord.getLikedCommentIds() != null)
                ? new HashSet<>(interactionRecord.getLikedCommentIds())
                : Set.of();

        List<ResourceCommentListItemResponse> list = pageData.stream().map(comment -> {
            ResourceCommentListItemResponse item = new ResourceCommentListItemResponse();
            item.setCommentId(comment.getCommentId());
            item.setResourceId(comment.getResourceId());
            item.setContent(comment.getContent());
            item.setImageUrls(comment.getImageUrls());
            item.setLikeCount(comment.getLikeCount());
            item.setReplyCount(comment.getReplyCount());
            item.setDeleted(comment.getDeletedAt() != null);
            item.setCreateTime(comment.getCreateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
            item.setLiked(likedSet.contains(comment.getCommentId()));
            if (comment.getDeletedAt() == null) {
                item.setAuthorInfo(userMap.get(Long.parseLong(comment.getAuthorId())));
            }
            return item;
        }).toList();

        CursorPageResponse<ResourceCommentListItemResponse> response = new CursorPageResponse<>();
        response.setList(list);
        response.setHasMore(hasMore);
        response.setPage(page);
        if (!list.isEmpty()) {
            ResourceCommentListItemResponse last = list.get(list.size() - 1);
            response.setNextCursorCreateTime(last.getCreateTime());
            if ("HOT".equalsIgnoreCase(sortBy)) {
                response.setNextCursorLikeCount(pageData.get(pageData.size() - 1).getLikeCount());
            }
        }
        return response;
    }

    @Override
    public CursorPageResponse<ResourceCommentReplyListItemResponse> listReplies(
            String rootCommentId, Long cursorCreateTime, int size, int page, String operatorUserId) {

        size = Math.min(size, 50);

        LocalDateTime cursorTime = cursorCreateTime != null
                ? LocalDateTime.ofInstant(Instant.ofEpochMilli(cursorCreateTime), ZoneId.systemDefault())
                : null;

        List<ResourceCommentReplyEntity> rawList =
                customReplyRepository.listByRootCommentIdOrderByTimeDesc(rootCommentId, cursorTime, size);

        boolean hasMore = rawList.size() > size;
        List<ResourceCommentReplyEntity> pageData = hasMore ? rawList.subList(0, size) : rawList;

        // 收集所有需要查询的用户 ID（authorId + replyToUserId），去重后一次 Feign 调用
        Set<Long> userIdSet = new HashSet<>();
        // authorId/replyToUserId 由 createReply 强制设置，始终非 null
        for (ResourceCommentReplyEntity reply : pageData) {
            if (reply.getDeletedAt() == null) {
                userIdSet.add(Long.parseLong(reply.getAuthorId()));
                userIdSet.add(Long.parseLong(reply.getReplyToUserId()));
            }
        }
        Map<Long, UserDisplayBase> userMap = userIdSet.isEmpty()
                ? Map.of()
                : remoteUserService.getUserDisplayInfo(new ArrayList<>(userIdSet)).getData();

        // resourceId 由 createReply 强制写入（冗余字段），直接取首条即可
        String resourceId = pageData.isEmpty() ? null : pageData.get(0).getResourceId();

        Set<String> likedSet = Set.of();
        if (resourceId != null) {
            ResourceUserInteractionRecordEntity interactionRecord =
                    customInteractionRecordRepository.findInteractionRecord(resourceId, operatorUserId);
            if (interactionRecord != null && interactionRecord.getLikedCommentIds() != null) {
                likedSet = new HashSet<>(interactionRecord.getLikedCommentIds());
            }
        }
        final Set<String> finalLikedSet = likedSet;

        List<ResourceCommentReplyListItemResponse> list = pageData.stream().map(reply -> {
            ResourceCommentReplyListItemResponse item = new ResourceCommentReplyListItemResponse();
            item.setReplyId(reply.getReplyId());
            item.setRootCommentId(reply.getRootCommentId());
            item.setParentReplyId(deriveParentReplyId(reply.getReplyId()));
            item.setContent(reply.getContent());
            item.setImageUrls(reply.getImageUrls());
            item.setLikeCount(reply.getLikeCount());
            item.setDeleted(reply.getDeletedAt() != null);
            item.setCreateTime(reply.getCreateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
            item.setLiked(finalLikedSet.contains(reply.getReplyId()));
            if (reply.getDeletedAt() == null) {
                item.setAuthorInfo(userMap.get(Long.parseLong(reply.getAuthorId())));
                item.setReplyToUserInfo(userMap.get(Long.parseLong(reply.getReplyToUserId())));
            }
            return item;
        }).toList();

        CursorPageResponse<ResourceCommentReplyListItemResponse> response = new CursorPageResponse<>();
        response.setList(list);
        response.setHasMore(hasMore);
        response.setPage(page);
        if (!list.isEmpty()) {
            response.setNextCursorCreateTime(list.get(list.size() - 1).getCreateTime());
        }
        return response;
    }

    /**
     * 从 replyId 推导 parentReplyId：去掉最后一个 _<segment>
     * 若只有一个 _ 则父节点为顶级评论，返回 null
     */
    private String deriveParentReplyId(String replyId) {
        int lastUnderscore = replyId.lastIndexOf('_');
        if (lastUnderscore < 0) return null;
        String parent = replyId.substring(0, lastUnderscore);
        return parent.contains("_") ? parent : null;
    }
}
