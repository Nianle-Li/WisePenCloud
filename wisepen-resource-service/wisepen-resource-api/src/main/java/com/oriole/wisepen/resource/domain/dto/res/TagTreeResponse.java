package com.oriole.wisepen.resource.domain.dto.res;

import com.oriole.wisepen.resource.domain.base.TagInfoPermissionBase;
import com.oriole.wisepen.resource.enums.ResourceAction;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class TagTreeResponse extends TagInfoPermissionBase {
    private String tagId;
    private String parentId;
    private List<ResourceAction> grantedActions;
    private List<TagTreeResponse> children; // 子节点列表，用于在内存中组装树返回给前端
}