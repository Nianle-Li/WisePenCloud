package com.oriole.wisepen.resource.service;

import com.oriole.wisepen.common.core.domain.PageR;
import com.oriole.wisepen.common.core.domain.enums.GroupRoleType;
import com.oriole.wisepen.resource.domain.dto.req.FavoriteCollectionCreateRequest;
import com.oriole.wisepen.resource.domain.dto.req.FavoriteCollectionDeleteRequest;
import com.oriole.wisepen.resource.domain.dto.req.FavoriteCollectionInfoUpdateRequest;
import com.oriole.wisepen.resource.domain.dto.req.ResourceFavoriteRequest;
import com.oriole.wisepen.resource.domain.dto.res.FavoriteCollectionResponse;
import com.oriole.wisepen.resource.domain.dto.res.FavoriteItemResponse;
import com.oriole.wisepen.resource.domain.dto.res.ResourceFavoriteStatusResponse;

import java.util.List;
import java.util.Map;

public interface IFavoriteService {

    void changeResourceFavoriteStatus(ResourceFavoriteRequest request, String userId);

    ResourceFavoriteStatusResponse getResourceFavoriteStatus(String resourceId, String userId);

    String createCollection(FavoriteCollectionCreateRequest request, String userId);

    void updateCollectionInfo(FavoriteCollectionInfoUpdateRequest request, String userId);

    void deleteCollection(FavoriteCollectionDeleteRequest request, String userId);

    List<FavoriteCollectionResponse> listCollections(String userId);

    PageR<FavoriteItemResponse> listFavoritedResources(String collectionId, int page, int size, String userId, Map<Long, GroupRoleType> groupRoles);
}
