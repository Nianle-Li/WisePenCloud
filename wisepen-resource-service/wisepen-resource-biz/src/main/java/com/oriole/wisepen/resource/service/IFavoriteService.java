package com.oriole.wisepen.resource.service;

import com.oriole.wisepen.common.core.domain.PageR;
import com.oriole.wisepen.resource.domain.dto.req.FavoriteCollectionCreateRequest;
import com.oriole.wisepen.resource.domain.dto.req.FavoriteCollectionUpdateRequest;
import com.oriole.wisepen.resource.domain.dto.req.FavoriteToggleRequest;
import com.oriole.wisepen.resource.domain.dto.res.FavoriteCollectionResponse;
import com.oriole.wisepen.resource.domain.dto.res.FavoriteItemResponse;

import java.util.List;

public interface IFavoriteService {

    /** 收藏/取消收藏（toggle 语义，per-collection）；前端成功后调用 getResourceUserInteractionRecord 获取最新状态 */
    void toggleFavorite(FavoriteToggleRequest request, String userId);

    /**
     * 新建收藏集合，返回服务端生成的 collectionId（ObjectId 字符串）。
     */
    String createCollection(FavoriteCollectionCreateRequest request, String userId);

    void updateCollection(String collectionId, FavoriteCollectionUpdateRequest request, String userId);

    void deleteCollection(String collectionId, String userId);

    List<FavoriteCollectionResponse> listCollections(String userId);

    PageR<FavoriteItemResponse> listByContent(int page, int size, String userId);

    PageR<FavoriteItemResponse> listByCollection(String collectionId, int page, int size, String userId);
}
