package com.oriole.wisepen.resource.domain.dto.res;

import com.oriole.wisepen.resource.domain.base.ResourceUserInteractionRecordBase;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class ResourceUserInteractionRecordResponse extends ResourceUserInteractionRecordBase {
    private String resourceId;
    /** 是否已收藏（查询时从 wisepen_favorite_collections.resources 实时计算，不持久化） */
    private boolean favorited;
    /** 所属收藏集合 ID 列表（空列表表示未收藏） */
    private List<String> favoritedCollectionIds;
}
