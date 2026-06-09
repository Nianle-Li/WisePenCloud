package com.oriole.wisepen.resource.domain.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 收藏集合公共字段基类 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteCollectionBase {
    private String collectionName;
    private String description;
    private Boolean isDefault;
}
