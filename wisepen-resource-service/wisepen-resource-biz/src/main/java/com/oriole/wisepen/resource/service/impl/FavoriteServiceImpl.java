package com.oriole.wisepen.resource.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.oriole.wisepen.common.core.domain.PageR;
import com.oriole.wisepen.common.core.exception.ServiceException;
import com.oriole.wisepen.resource.domain.dto.req.FavoriteCollectionCreateRequest;
import com.oriole.wisepen.resource.domain.dto.req.FavoriteCollectionItemRequest;
import com.oriole.wisepen.resource.domain.dto.req.FavoriteCollectionUpdateRequest;
import com.oriole.wisepen.resource.domain.dto.res.FavoriteCollectionResponse;
import com.oriole.wisepen.resource.domain.dto.res.FavoriteItemResponse;
import com.oriole.wisepen.resource.domain.dto.res.ResourceItemResponse;
import com.oriole.wisepen.resource.domain.entity.FavoriteCollectionEntity;
import com.oriole.wisepen.resource.domain.entity.FavoriteResourceRef;
import com.oriole.wisepen.resource.domain.entity.ResourceItemEntity;
import com.oriole.wisepen.resource.exception.ResourceError;
import com.oriole.wisepen.resource.repository.CustomFavoriteCollectionRepository;
import com.oriole.wisepen.resource.repository.CustomResourceItemRepository;
import com.oriole.wisepen.resource.repository.FavoriteCollectionRepository;
import com.oriole.wisepen.resource.repository.ResourceItemRepository;
import com.oriole.wisepen.resource.service.IFavoriteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class FavoriteServiceImpl implements IFavoriteService {

    private final FavoriteCollectionRepository favoriteCollectionRepository;
    private final CustomFavoriteCollectionRepository customFavoriteCollectionRepository;
    private final CustomResourceItemRepository customResourceItemRepository;
    private final ResourceItemRepository resourceItemRepository;

    @Override
    public void switchCollectionItem(FavoriteCollectionItemRequest request, String userId) {
        String resourceId = request.getResourceId();

        // 确定目标收藏集合
        FavoriteCollectionEntity collection;
        if (StringUtils.hasText(request.getCollectionId())) {
            collection = validateCollectionOwnership(request.getCollectionId(), userId);
        } else {
            collection = customFavoriteCollectionRepository.findOrCreateDefaultCollection(userId);
        }
        String collectionId = collection.getCollectionId();

        boolean alreadyInCollection = collection.getResources() != null
                && collection.getResources().stream().anyMatch(r -> resourceId.equals(r.getResourceId()));

        if (!alreadyInCollection) {
            // 添加收藏：排除软删除资源（deletedAt != null 的资源对用户不可见）
            ResourceItemEntity targetResource = resourceItemRepository.findById(resourceId)
                    .orElseThrow(() -> new ServiceException(ResourceError.RESOURCE_NOT_FOUND));
            if (targetResource.getDeletedAt() != null) {
                throw new ServiceException(ResourceError.RESOURCE_NOT_FOUND);
            }
            customFavoriteCollectionRepository.addResource(collectionId, new FavoriteResourceRef(resourceId, LocalDateTime.now()));

            // 首次收藏判断：若其他集合均不含该资源则 favoriteCount +1
            if (isOnlyFavoriteCollection(userId, resourceId, collectionId)) {
                customResourceItemRepository.incrementFavoriteCount(resourceId, 1);
            }
            log.info("resource favorited. resourceId={} collectionId={} userId={}", resourceId, collectionId, userId);
        } else {
            // 移除收藏
            customFavoriteCollectionRepository.removeResource(collectionId, resourceId);

            // 若其他集合均不含该资源则 favoriteCount -1
            if (isOnlyFavoriteCollection(userId, resourceId, collectionId)) {
                customResourceItemRepository.incrementFavoriteCount(resourceId, -1);
            }
            log.info("resource unfavorited. resourceId={} collectionId={} userId={}", resourceId, collectionId, userId);
        }
    }

    @Override
    public String createCollection(FavoriteCollectionCreateRequest request, String userId) {
        FavoriteCollectionEntity entity = new FavoriteCollectionEntity(
                userId, request.getCollectionName(), request.getDescription(), false);
        String newCollectionId = favoriteCollectionRepository.save(entity).getCollectionId();
        log.info("favorite collection created. collectionId={} userId={}", newCollectionId, userId);
        return newCollectionId;
    }

    @Override
    public void updateCollection(FavoriteCollectionUpdateRequest request, String userId) {
        FavoriteCollectionEntity entity = validateCollectionOwnership(request.getCollectionId(), userId);
        entity.setCollectionName(request.getCollectionName());
        entity.setDescription(request.getDescription());
        favoriteCollectionRepository.save(entity);
        log.info("favorite collection updated. collectionId={} userId={}", request.getCollectionId(), userId);
    }

    @Override
    public void deleteCollection(String collectionId, String userId) {
        FavoriteCollectionEntity collection = validateCollectionOwnership(collectionId, userId);
        if (Boolean.TRUE.equals(collection.getIsDefault())) {
            throw new ServiceException(ResourceError.DEFAULT_COLLECTION_CANNOT_DELETE);
        }

        // 批量计算并递减仅属于本集合的资源的 favoriteCount
        List<String> resourceIdsInThisCollection = collection.getResources() == null
            ? List.of()
            : collection.getResources().stream().map(FavoriteResourceRef::getResourceId).toList();

        int decrementCount = 0;
        if (!resourceIdsInThisCollection.isEmpty()) {
            List<FavoriteCollectionEntity> otherCollections = favoriteCollectionRepository
                .findByUserIdOrderByIsDefaultDescCreateTimeDesc(userId).stream()
                .filter(c -> !collectionId.equals(c.getCollectionId()))
                .toList();

            List<String> toDecrement = resourceIdsInThisCollection.stream()
                .filter(rid -> otherCollections.stream()
                    .noneMatch(c -> c.getResources() != null
                        && c.getResources().stream().anyMatch(r -> rid.equals(r.getResourceId()))))
                .toList();

            customResourceItemRepository.decrementFavoriteCountForResources(toDecrement);
            decrementCount = toDecrement.size();
        }

        favoriteCollectionRepository.deleteById(collectionId);
        log.info("favorite collection deleted. collectionId={} userId={} decrementCount={}",
                collectionId, userId, decrementCount);
    }

    @Override
    public List<FavoriteCollectionResponse> listCollections(String userId) {
        List<FavoriteCollectionEntity> collections =
                favoriteCollectionRepository.findByUserIdOrderByIsDefaultDescCreateTimeDesc(userId);
        return collections.stream().map(c -> {
            FavoriteCollectionResponse resp = new FavoriteCollectionResponse();
            BeanUtil.copyProperties(c, resp);
            resp.setItemCount(c.getResources() == null ? 0 : c.getResources().size());
            return resp;
        }).toList();
    }

    @Override
    public PageR<FavoriteItemResponse> listFavoritedResources(int page, int size, String userId) {
        List<FavoriteCollectionEntity> collections =
                favoriteCollectionRepository.findByUserIdOrderByIsDefaultDescCreateTimeDesc(userId);

        // 拉平所有 resources，按 resourceId 去重，同一资源 favoritedAt 取最近的
        Map<String, LocalDateTime> latestFavoritedAtMap = new HashMap<>();
        for (FavoriteCollectionEntity c : collections) {
            if (c.getResources() == null) continue;
            for (FavoriteResourceRef ref : c.getResources()) {
                latestFavoritedAtMap.merge(ref.getResourceId(), ref.getFavoritedAt(),
                        (existing, newVal) -> newVal.isAfter(existing) ? newVal : existing);
            }
        }

        // 按 favoritedAt 倒序排列后内存分页
        List<Map.Entry<String, LocalDateTime>> sorted = latestFavoritedAtMap.entrySet().stream()
                .sorted(Map.Entry.<String, LocalDateTime>comparingByValue(Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();

        long total = sorted.size();
        int skip = (page - 1) * size;
        List<Map.Entry<String, LocalDateTime>> pageEntries = sorted.stream().skip(skip).limit(size).toList();

        if (pageEntries.isEmpty()) {
            return new PageR<>(total, page, size);
        }

        List<String> resourceIds = pageEntries.stream().map(Map.Entry::getKey).toList();
        // 过滤软删除资源（deletedAt != null），软删除资源对用户不可见，accessible=false
        Map<String, ResourceItemEntity> resourceMap = resourceItemRepository.findAllById(resourceIds).stream()
                .filter(r -> r.getDeletedAt() == null)
                .collect(Collectors.toMap(ResourceItemEntity::getResourceId, r -> r));

        List<FavoriteItemResponse> respList = pageEntries.stream().map(entry -> {
            String rid = entry.getKey();
            FavoriteItemResponse resp = new FavoriteItemResponse();
            resp.setFavoritedAt(entry.getValue());
            boolean accessible = resourceMap.containsKey(rid);
            resp.setAccessible(accessible);
            resp.setResourceInfo(buildResourceItemResponse(rid, accessible, resourceMap));
            return resp;
        }).toList();

        PageR<FavoriteItemResponse> pageR = new PageR<>(total, page, size);
        pageR.addAll(respList);
        return pageR;
    }

    @Override
    public PageR<FavoriteItemResponse> listFavoritesByCollection(String collectionId, int page, int size, String userId) {
        FavoriteCollectionEntity collection = validateCollectionOwnership(collectionId, userId);

        List<FavoriteResourceRef> allRefs = collection.getResources() == null ? List.of() : collection.getResources();
        // 按 favoritedAt 倒序
        List<FavoriteResourceRef> sorted = allRefs.stream()
                .sorted(Comparator.comparing(FavoriteResourceRef::getFavoritedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();

        long total = sorted.size();
        int skip = (page - 1) * size;
        List<FavoriteResourceRef> pageRefs = sorted.stream().skip(skip).limit(size).toList();

        if (pageRefs.isEmpty()) {
            return new PageR<>(total, page, size);
        }

        List<String> resourceIds = pageRefs.stream().map(FavoriteResourceRef::getResourceId).toList();
        // 过滤软删除资源（deletedAt != null），软删除资源对用户不可见，accessible=false
        Map<String, ResourceItemEntity> resourceMap = resourceItemRepository.findAllById(resourceIds).stream()
                .filter(r -> r.getDeletedAt() == null)
                .collect(Collectors.toMap(ResourceItemEntity::getResourceId, r -> r));

        List<FavoriteItemResponse> respList = pageRefs.stream().map(ref -> {
            FavoriteItemResponse resp = new FavoriteItemResponse();
            resp.setFavoritedAt(ref.getFavoritedAt());
            boolean accessible = resourceMap.containsKey(ref.getResourceId());
            resp.setAccessible(accessible);
            resp.setResourceInfo(buildResourceItemResponse(ref.getResourceId(), accessible, resourceMap));
            return resp;
        }).toList();

        PageR<FavoriteItemResponse> pageR = new PageR<>(total, page, size);
        pageR.addAll(respList);
        return pageR;
    }

    @Override
    public List<String> listResourceCollections(String resourceId, String userId) {
        return favoriteCollectionRepository.findByUserIdOrderByIsDefaultDescCreateTimeDesc(userId).stream()
                .filter(c -> c.getResources() != null
                        && c.getResources().stream().anyMatch(r -> resourceId.equals(r.getResourceId())))
                .map(FavoriteCollectionEntity::getCollectionId)
                .toList();
    }

    // 内部辅助方法

    private FavoriteCollectionEntity validateCollectionOwnership(String collectionId, String userId) {
        FavoriteCollectionEntity entity = favoriteCollectionRepository.findById(collectionId)
                .orElseThrow(() -> new ServiceException(ResourceError.FAVORITE_COLLECTION_NOT_FOUND));
        if (!userId.equals(entity.getUserId())) {
            throw new ServiceException(ResourceError.FAVORITE_COLLECTION_ACCESS_DENIED);
        }
        return entity;
    }

    /** 判断 excludeCollectionId 之外的其他收藏集合是否均不含此资源（首次/最后一次收藏的统一判断条件） */
    private boolean isOnlyFavoriteCollection(String userId, String resourceId, String excludeCollectionId) {
        return favoriteCollectionRepository.findByUserIdOrderByIsDefaultDescCreateTimeDesc(userId).stream()
                .filter(c -> !excludeCollectionId.equals(c.getCollectionId()))
                .noneMatch(c -> c.getResources() != null
                        && c.getResources().stream().anyMatch(r -> resourceId.equals(r.getResourceId())));
    }

    private ResourceItemResponse buildResourceItemResponse(String resourceId, boolean accessible,
            Map<String, ResourceItemEntity> resourceMap) {
        ResourceItemResponse resourceInfo = new ResourceItemResponse();
        if (accessible) {
            ResourceItemEntity entity = resourceMap.get(resourceId);
            BeanUtil.copyProperties(entity, resourceInfo);
            resourceInfo.setResourceInteractionInfo(entity.getInteractionInfo());
        } else {
            resourceInfo.setResourceId(resourceId);
        }
        return resourceInfo;
    }
}
