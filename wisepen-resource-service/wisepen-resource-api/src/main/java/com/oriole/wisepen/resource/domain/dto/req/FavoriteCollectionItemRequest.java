package com.oriole.wisepen.resource.domain.dto.req;

import com.oriole.wisepen.resource.constant.ResourceValidationMsg;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FavoriteCollectionItemRequest {
    @NotBlank(message = ResourceValidationMsg.RESOURCE_ID_NOT_BLANK)
    private String resourceId;

    /** 目标收藏集合 ID；不传则操作默认收藏集合（懒初始化） */
    private String collectionId;
}
