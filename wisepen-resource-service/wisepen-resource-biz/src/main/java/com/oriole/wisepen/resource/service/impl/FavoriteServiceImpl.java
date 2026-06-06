package com.oriole.wisepen.resource.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.oriole.wisepen.common.core.domain.PageR;
import com.oriole.wisepen.common.core.exception.ServiceException;
import com.oriole.wisepen.resource.domain.dto.req.FavoriteCollectionCreateRequest;
import com.oriole.wisepen.resource.domain.dto.req.FavoriteCollectionUpdateRequest;
import com.oriole.wisepen.resource.domain.dto.req.FavoriteToggleRequest;
import com.oriole.wisepen.resource.domain.dto.res.FavoriteCollectionResponse;
import com.oriole.wisepen.resource.domain.dto.res.FavoriteItemResponse;
import com.oriole.wisepen.resource.domain.dto.res.ResourceItemResponse;
import com.oriole.wisepen.resource.domain.entity.FavoriteCollectionEntity;
import com.oriole.wisepen.resource.domain.entity.FavoriteItemEntity;
import com.oriole.wisepen.resource.domain.entity.ResourceInteractionInfoEntity;
import com.oriole.wisepen.resource.domain.entity.ResourceItemEntity;
import com.oriole.wisepen.resource.exception.ResourceError;
import com.oriole.wisepen.resource.repository.CustomFavoriteCollectionRepository;
import com.oriole.wisepen.resource.repository.CustomFavoriteItemRepository;
import com.oriole.wisepen.resource.repository.CustomFavoriteItemRepository.FavoriteContentAggResult;
import com.oriole.wisepen.resource.repository.CustomResourceInteractionInfoRepository;
import com.oriole.wisepen.resource.repository.CustomResourceUserInteractionRecordRepository;
import com.oriole.wisepen.resource.repository.FavoriteCollectionRepository;
import com.oriole.wisepen.resource.repository.FavoriteItemRepository;
import com.oriole.wisepen.resource.repository.ResourceInteractionInfoRepository;
import com.oriole.wisepen.resource.repository.ResourceItemRepository;
import com.oriole.wisepen.resource.service.IFavoriteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class FavoriteServiceImpl implements IFavoriteService {

    private final FavoriteCollectionRepository favoriteCollectionRepository;
    private final FavoriteItemRepository favoriteItemRepository;
    private final CustomFavoriteCollectionRepository customFavoriteCollectionRepository;
    private final CustomFavoriteItemRepository customFavoriteItemRepository;
    private final CustomResourceInteractionInfoRepository customResourceInteractionInfoRepository;
    private final CustomResourceUserInteractionRecordRepository customResourceUserInteractionRecordRepository;
    private final ResourceItemRepository resourceItemRepository;
    private final ResourceInteractionInfoRepository resourceInteractionInfoRepository;

    // 收藏 toggle
    @Override
    public void toggleFavorite(FavoriteToggleRequest request, String userId) {
        String resourceId = request.getResourceId();

        // 确定目标收藏集合
        String collectionId;
        if (StringUtils.hasText(request.getCollectionId())) {
            validateCollectionOwnership(request.getCollectionId(), userId);
            collectionId = request.getCollectionId();
        } else {
            collectionId = customFavoriteCollectionRepository.findOrCreateDefaultCollection(userId);
        }

        boolean alreadyInCollection = favoriteItemRepository.existsByUserIdAndResourceIdAndCollectionId(userId, resourceId, collectionId);
        if (!alreadyInCollection) {
            // 添加收藏
            if (!resourceItemRepository.existsById(resourceId)) {
                throw new ServiceException(ResourceError.RESOURCE_NOT_FOUND);
            }
            // $addToSet 原子操作：返回操作前集合是否为空，消除并发多收藏集合时的 check-then-act 竞态
            boolean isFirstFavorite = customResourceUserInteractionRecordRepository
                    .addToFavoritedCollections(resourceId, userId, collectionId);
            favoriteItemRepository.save(new FavoriteItemEntity(userId, resourceId, collectionId));
            if (isFirstFavorite) {
                customResourceInteractionInfoRepository.incrementFavoriteCount(resourceId, 1);
            }
        } else {
            // 取消收藏
            long deleted = favoriteItemRepository.deleteByUserIdAndResourceIdAndCollectionId(userId, resourceId, collectionId);
            if (deleted == 0) {
                // 并发情况下已被删除，幂等返回
                return;
            }
            // $pull 原子操作：返回操作前集合是否仅含该 collectionId，消除并发多收藏集合时的竞态
            boolean isLastFavorite = customResourceUserInteractionRecordRepository
                    .removeFromFavoritedCollections(resourceId, userId, collectionId);
            if (isLastFavorite) {
                customResourceInteractionInfoRepository.incrementFavoriteCount(resourceId, -1);
            }
        }
    }


    // 收藏集合 CRUD
    @Override
    public String createCollection(FavoriteCollectionCreateRequest request, String userId) {
        FavoriteCollectionEntity entity = new FavoriteCollectionEntity(
                userId, request.getCollectionName(), request.getDescription(), false);
        return favoriteCollectionRepository.save(entity).getCollectionId();
    }

    @Override
    public void updateCollection(String collectionId, FavoriteCollectionUpdateRequest request, String userId) {
        FavoriteCollectionEntity entity = validateCollectionOwnership(collectionId, userId);
        entity.setCollectionName(request.getCollectionName());
        entity.setDescription(request.getDescription());
        favoriteCollectionRepository.save(entity);
    }

    @Override
    public void deleteCollection(String collectionId, String userId) {
        FavoriteCollectionEntity collection = validateCollectionOwnership(collectionId, userId);
        if (Boolean.TRUE.equals(collection.getIsDefault())) {
            throw new ServiceException(ResourceError.DEFAULT_COLLECTION_CANNOT_DELETE);
        }

        List<FavoriteItemEntity> items = favoriteItemRepository.findByUserIdAndCollectionId(userId, collectionId);
        List<String> resourceIds = items.stream()
                .map(FavoriteItemEntity::getResourceId)
                .distinct()
                .toList();

        if (!resourceIds.isEmpty()) {
            // 查询这批资源在所有收藏集合的记录，判断哪些资源在其他收藏集合还有记录
            List<FavoriteItemEntity> allRecords = favoriteItemRepository.findByUserIdAndResourceIdIn(userId, resourceIds);
            Set<String> stillFavoritedIds = allRecords.stream()
                    .filter(r -> !collectionId.equals(r.getCollectionId()))
                    .map(FavoriteItemEntity::getResourceId)
                    .collect(Collectors.toSet());
            List<String> toDecrement = resourceIds.stream()
                    .filter(id -> !stillFavoritedIds.contains(id))
                    .toList();
            if (!toDecrement.isEmpty()) {
                customResourceInteractionInfoRepository.decrementFavoriteCountForResources(toDecrement);
            }
            // 同步 favoritedCollectionIds 追踪集合，保证 toggleFavorite 原子判断的正确性
            customResourceUserInteractionRecordRepository.batchPullCollectionFromFavoritedSet(userId, resourceIds, collectionId);
        }

        favoriteItemRepository.deleteAllByCollectionId(collectionId);
        favoriteCollectionRepository.deleteById(collectionId);
    }

    // ────────────────────────────────────────────────────────────────────────────
    // 查询
    // ────────────────────────────────────────────────────────────────────────────

    @Override
    public List<FavoriteCollectionResponse> listCollections(String userId) {
        List<FavoriteCollectionEntity> collections =
                favoriteCollectionRepository.findByUserIdOrderByIsDefaultDescCreateTimeAsc(userId);
        List<String> collectionIds = collections.stream().map(FavoriteCollectionEntity::getCollectionId).toList();
        Map<String, Long> countMap = customFavoriteItemRepository.countItemsByCollectionIds(collectionIds);

        return collections.stream().map(c -> {
            FavoriteCollectionResponse resp = new FavoriteCollectionResponse();
            BeanUtil.copyProperties(c, resp);
            resp.setItemCount(countMap.getOrDefault(c.getCollectionId(), 0L).intValue());
            return resp;
        }).toList();
    }

    @Override
    public PageR<FavoriteItemResponse> listByContent(int page, int size, String userId) {
        Page<FavoriteContentAggResult> aggPage = customFavoriteItemRepository.pageByUserId(userId, page, size);
        List<FavoriteContentAggResult> aggs = aggPage.getContent();
        if (aggs.isEmpty()) {
            return new PageR<>(aggPage.getTotalElements(), page, size);
        }

        List<String> resourceIds = aggs.stream().map(FavoriteContentAggResult::getResourceId).toList();
        Map<String, ResourceItemEntity> resourceMap = resourceItemRepository.findAllById(resourceIds).stream()
                .collect(Collectors.toMap(ResourceItemEntity::getResourceId, r -> r));
        Map<String, ResourceInteractionInfoEntity> interactMap =
                resourceInteractionInfoRepository.findByResourceIdIn(resourceIds).stream()
                        .collect(Collectors.toMap(ResourceInteractionInfoEntity::getResourceId, r -> r));

        List<FavoriteItemResponse> respList = aggs.stream().map(agg -> {
            FavoriteItemResponse resp = new FavoriteItemResponse();
            resp.setCollectionIds(agg.getCollectionIds());
            resp.setFavoritedAt(agg.getFirstFavoritedAt());
            boolean accessible = resourceMap.containsKey(agg.getResourceId());
            resp.setAccessible(accessible);
            resp.setResourceInfo(buildResourceItemResponse(agg.getResourceId(), accessible, resourceMap, interactMap));
            return resp;
        }).toList();

        PageR<FavoriteItemResponse> pageR = new PageR<>(aggPage.getTotalElements(), page, size);
        pageR.addAll(respList);
        return pageR;
    }

    @Override
    public PageR<FavoriteItemResponse> listByCollection(String collectionId, int page, int size, String userId) {
        validateCollectionOwnership(collectionId, userId);

        Pageable pageable = PageRequest.of(page - 1, size);
        List<FavoriteItemEntity> items =
                favoriteItemRepository.findByUserIdAndCollectionIdOrderByCreateTimeDesc(userId, collectionId, pageable);
        long total = favoriteItemRepository.countByUserIdAndCollectionId(userId, collectionId);

        if (items.isEmpty()) {
            return new PageR<>(total, page, size);
        }

        List<String> resourceIds = items.stream().map(FavoriteItemEntity::getResourceId).distinct().toList();
        Map<String, ResourceItemEntity> resourceMap = resourceItemRepository.findAllById(resourceIds).stream()
                .collect(Collectors.toMap(ResourceItemEntity::getResourceId, r -> r));
        Map<String, ResourceInteractionInfoEntity> interactMap =
                resourceInteractionInfoRepository.findByResourceIdIn(resourceIds).stream()
                        .collect(Collectors.toMap(ResourceInteractionInfoEntity::getResourceId, r -> r));

        // 查询各资源在所有收藏集合的归属，用于填充 collectionIds 字段
        List<FavoriteItemEntity> allCollectionRecords =
                favoriteItemRepository.findByUserIdAndResourceIdIn(userId, resourceIds);
        Map<String, List<String>> collectionIdsMap = allCollectionRecords.stream()
                .collect(Collectors.groupingBy(
                        FavoriteItemEntity::getResourceId,
                        Collectors.mapping(FavoriteItemEntity::getCollectionId, Collectors.toList())
                ));

        List<FavoriteItemResponse> respList = items.stream().map(item -> {
            FavoriteItemResponse resp = new FavoriteItemResponse();
            resp.setCollectionIds(collectionIdsMap.get(item.getResourceId()));
            resp.setFavoritedAt(item.getCreateTime());
            boolean accessible = resourceMap.containsKey(item.getResourceId());
            resp.setAccessible(accessible);
            resp.setResourceInfo(buildResourceItemResponse(item.getResourceId(), accessible, resourceMap, interactMap));
            return resp;
        }).toList();

        PageR<FavoriteItemResponse> pageR = new PageR<>(total, page, size);
        pageR.addAll(respList);
        return pageR;
    }

    /** 根据资源是否可访问构建响应体，不可访问时仅保留 resourceId。 */
    private ResourceItemResponse buildResourceItemResponse(String resourceId, boolean accessible,
            Map<String, ResourceItemEntity> resourceMap,
            Map<String, ResourceInteractionInfoEntity> interactMap) {
        ResourceItemResponse resourceInfo = new ResourceItemResponse();
        if (accessible) {
            BeanUtil.copyProperties(resourceMap.get(resourceId), resourceInfo);
            resourceInfo.setResourceInteractionInfo(
                    interactMap.getOrDefault(resourceId, new ResourceInteractionInfoEntity()));
        } else {
            resourceInfo.setResourceId(resourceId);
        }
        return resourceInfo;
    }

    /**
     * 校验收藏集合存在且归属当前用户，通过则返回实体。
     */
    private FavoriteCollectionEntity validateCollectionOwnership(String collectionId, String userId) {
        FavoriteCollectionEntity entity = favoriteCollectionRepository.findById(collectionId)
                .orElseThrow(() -> new ServiceException(ResourceError.FAVORITE_COLLECTION_NOT_FOUND));
        if (!userId.equals(entity.getUserId())) {
            throw new ServiceException(ResourceError.FAVORITE_COLLECTION_ACCESS_DENIED);
        }
        return entity;
    }
}
