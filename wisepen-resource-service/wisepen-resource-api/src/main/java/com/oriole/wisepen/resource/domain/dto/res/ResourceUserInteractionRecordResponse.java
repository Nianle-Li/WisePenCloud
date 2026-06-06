package com.oriole.wisepen.resource.domain.dto.res;

import com.oriole.wisepen.resource.domain.base.ResourceUserInteractionRecordBase;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class ResourceUserInteractionRecordResponse extends ResourceUserInteractionRecordBase {
    private String resourceId;
    // 收藏状态读自 FavoriteItemEntity，不加入 Base/Entity
    private Boolean favorited;
    private List<String> favoritedCollectionIds;
}
