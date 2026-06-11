package com.oriole.wisepen.resource.service;

import com.oriole.wisepen.common.core.domain.PageR;
import com.oriole.wisepen.resource.domain.dto.req.FavoriteCollectionCreateRequest;
import com.oriole.wisepen.resource.domain.dto.req.FavoriteCollectionItemRequest;
import com.oriole.wisepen.resource.domain.dto.req.FavoriteCollectionUpdateRequest;
import com.oriole.wisepen.resource.domain.dto.res.FavoriteCollectionResponse;
import com.oriole.wisepen.resource.domain.dto.res.FavoriteItemResponse;

import java.util.List;

public interface IFavoriteService {

    /** 向指定收藏集合添加或移除资源；前端成功后调用 getResourceUserInteractionRecord 刷新状态 */
    void switchCollectionItem(FavoriteCollectionItemRequest request, String userId);

    /** 新建收藏集合，返回服务端生成的 collectionId（ObjectId 字符串） */
    String createCollection(FavoriteCollectionCreateRequest request, String userId);

    /** collectionId 在 request 中 */
    void updateCollection(FavoriteCollectionUpdateRequest request, String userId);

    void deleteCollection(String collectionId, String userId);

    List<FavoriteCollectionResponse> listCollections(String userId);

    /** 展示用户所有已收藏资源（跨集合去重，按最近收藏时间倒序分页） */
    PageR<FavoriteItemResponse> listFavoritedResources(int page, int size, String userId);

    PageR<FavoriteItemResponse> listFavoritesByCollection(String collectionId, int page, int size, String userId);

    /** 查询当前用户收藏了指定资源的所有收藏集合 ID 列表 */
    List<String> listResourceCollections(String resourceId, String userId);
}
