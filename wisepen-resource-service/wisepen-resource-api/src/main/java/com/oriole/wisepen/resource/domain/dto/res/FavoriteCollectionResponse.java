package com.oriole.wisepen.resource.domain.dto.res;

import com.oriole.wisepen.resource.domain.base.FavoriteCollectionBase;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
public class FavoriteCollectionResponse extends FavoriteCollectionBase {
    private String collectionId;
    private Integer itemCount;
    private LocalDateTime createTime;
}
