package com.oriole.wisepen.resource.domain.dto.req;

import com.oriole.wisepen.resource.constant.ResourceValidationMsg;
import com.oriole.wisepen.resource.domain.base.TagInfoPermissionBase;
import com.oriole.wisepen.resource.enums.ResourceAction;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class TagUpdateRequest extends TagInfoPermissionBase {
    @NotBlank(message = ResourceValidationMsg.TAG_ID_NOT_BLANK)
    private String targetTagId;
    private List<ResourceAction> grantedActions;
}
