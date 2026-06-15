package com.oriole.wisepen.resource.domain.base;

import com.oriole.wisepen.resource.enums.AccessControlScope;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class TagInfoBase extends TagSpaceBase{
    private String tagName;
    private String tagDesc;
    private String tagIcon;
    private String tagColor;

    private String tagCreator;

    // 节点类型标识：true 表示 FOLDER(路径)，false 表示普通 TAG
    private Boolean isPath;
}