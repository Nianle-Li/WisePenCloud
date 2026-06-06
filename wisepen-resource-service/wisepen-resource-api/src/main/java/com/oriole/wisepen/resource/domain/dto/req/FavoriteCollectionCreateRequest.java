package com.oriole.wisepen.resource.domain.dto.req;

import com.oriole.wisepen.resource.constant.ResourceValidationMsg;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FavoriteCollectionCreateRequest {
    @NotBlank(message = ResourceValidationMsg.COLLECTION_NAME_NOT_BLANK)
    private String collectionName;

    /** 可选描述 */
    private String description;
}
