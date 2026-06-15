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
public class TagInfoPermissionBase extends TagInfoBase{
    // 权限配置
    private AccessControlScope taggedResourceAclGrantScope;         // 资源权限授予范围
    private List<String> taggedResourceAclGrantSpecifiedUsers;      // 供 资源权限授予范围中 白名单/黑名单使用的 userId 列表
    private Integer taggedResourceGrantedActionsMask;               // 匹配该标签时授予的权限掩码

    private AccessControlScope tagMountPermissionScope;             // 标签挂载权限范围
    private List<String> tagMountSpecifiedUsers;                    // 供 标签挂载权限范围中 白名单/黑名单使用的 userId 列表
}