package com.oriole.wisepen.resource.service.impl;

import com.oriole.wisepen.common.core.context.SecurityContextHolder;
import com.oriole.wisepen.common.core.exception.ServiceException;
import com.oriole.wisepen.resource.domain.dto.req.ResourceRateRequest;
import com.oriole.wisepen.resource.domain.dto.req.ResourceToggleLikeRequest;
import com.oriole.wisepen.resource.domain.dto.res.ResourceInteractStateResponse;
import com.oriole.wisepen.resource.domain.entity.ResourceUserInteractRecordEntity;
import com.oriole.wisepen.resource.exception.ResourceError;
import com.oriole.wisepen.resource.repository.CustomResourceInteractInfoRepository;
import com.oriole.wisepen.resource.repository.CustomResourceUserInteractRecordRepository;
import com.oriole.wisepen.resource.repository.ResourceItemRepository;
import com.oriole.wisepen.resource.repository.ResourceUserInteractRecordRepository;
import com.oriole.wisepen.resource.service.IResourceInteractService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ResourceInteractServiceImpl implements IResourceInteractService {

    private final ResourceItemRepository resourceItemRepository;
    private final ResourceUserInteractRecordRepository resourceUserInteractRecordRepository;
    private final CustomResourceInteractInfoRepository customResourceInteractInfoRepository;
    private final CustomResourceUserInteractRecordRepository customResourceUserInteractRecordRepository;

    /**
     * 点赞/取消点赞。
     * delta 基于 findAndModify 返回的实际旧状态计算，消除同用户并发双击导致的 likeCount 重复计数。
     */
    @Override
    public ResourceInteractStateResponse toggleLike(ResourceToggleLikeRequest request) {
        assertResourceExists(request.getResourceId());

        String userId = SecurityContextHolder.getUserId().toString();
        String resourceId = request.getResourceId();

        boolean currentLiked = resourceUserInteractRecordRepository
                .findByUserIdAndResourceId(userId, resourceId)
                .map(r -> Boolean.TRUE.equals(r.getLiked()))
                .orElse(false);
        boolean wantLiked = !currentLiked;

        ResourceUserInteractRecordEntity prevRecord =
                customResourceUserInteractRecordRepository.findAndSetLiked(resourceId, userId, wantLiked);

        boolean actualOldLiked = prevRecord != null && Boolean.TRUE.equals(prevRecord.getLiked());
        long delta = (wantLiked != actualOldLiked) ? (wantLiked ? 1L : -1L) : 0L;
        if (delta != 0) {
            customResourceInteractInfoRepository.incrementLikeCount(resourceId, delta);
        }

        ResourceInteractStateResponse response = new ResourceInteractStateResponse();
        response.setResourceId(resourceId);
        response.setLiked(wantLiked);
        response.setLikeCount(null);
        response.setUserScore(null);

        log.info("resource like toggled resourceId={} userId={} actualOldLiked={} wantLiked={} delta={}",
                resourceId, userId, actualOldLiked, wantLiked, delta);
        return response;
    }

    /**
     * 资源评分（1-5），支持覆盖更新。
     * findAndModify(upsert=true, returnNew=false) 将并发请求串行化，保证每个请求获取到的 oldScore 严格一致。
     */
    @Override
    public ResourceInteractStateResponse rateResource(ResourceRateRequest request) {
        assertResourceExists(request.getResourceId());

        Integer newScore = request.getScore();
        if (newScore == null || newScore < 1 || newScore > 5) {
            throw new ServiceException(ResourceError.SCORE_OUT_OF_RANGE);
        }

        String userId = SecurityContextHolder.getUserId().toString();
        String resourceId = request.getResourceId();

        ResourceUserInteractRecordEntity oldRecord =
                customResourceUserInteractRecordRepository.findAndSetScore(resourceId, userId, newScore);

        Integer oldScore = oldRecord == null ? null : oldRecord.getScore();
        if (oldScore == null) {
            // 首次评分
            customResourceInteractInfoRepository.updateScoreStats(resourceId, 1, newScore);
        } else if (!oldScore.equals(newScore)) {
            // 覆盖评分：scoreCount 不变，仅增量调整总分
            customResourceInteractInfoRepository.updateScoreStats(resourceId, 0, newScore - oldScore);
        }
        // oldScore == newScore：重复提交相同分数，幂等
        ResourceInteractStateResponse response = new ResourceInteractStateResponse();
        response.setResourceId(resourceId);
        // 回填当前 liked 状态，方便详情页同时刷新互动区
        boolean liked = oldRecord != null && Boolean.TRUE.equals(oldRecord.getLiked());
        response.setLiked(liked);
        response.setLikeCount(null);
        response.setUserScore(newScore);

        log.info("resource rated resourceId={} userId={} oldScore={} newScore={}",
                resourceId, userId, oldScore, newScore);
        return response;
    }

    private void assertResourceExists(String resourceId) {
        if (!resourceItemRepository.existsById(resourceId)) {
            throw new ServiceException(ResourceError.RESOURCE_NOT_FOUND);
        }
    }
}
