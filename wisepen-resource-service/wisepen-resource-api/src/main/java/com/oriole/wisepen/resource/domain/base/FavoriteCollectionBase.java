package com.oriole.wisepen.resource.domain.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 收藏集合公共 Base，供 FavoriteCollectionEntity 和 FavoriteCollectionResponse 共享字段
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteCollectionBase {
    private String collectionName;
    private String description;
    private Boolean isDefault;
}
