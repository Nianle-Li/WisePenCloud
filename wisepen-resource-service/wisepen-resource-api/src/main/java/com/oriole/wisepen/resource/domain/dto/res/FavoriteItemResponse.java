package com.oriole.wisepen.resource.domain.dto.res;

import lombok.Data;

import java.time.LocalDateTime;

/** 收藏条目响应 */
@Data
public class FavoriteItemResponse {
    /** 资源详情聚合；accessible=false 时仅 resourceId 有值，其余字段为 null */
    private ResourceItemResponse resourceInfo;
    private LocalDateTime favoritedAt;
    /** 资源是否可访问；false 表示资源已被软删除 */
    private Boolean accessible;
}
