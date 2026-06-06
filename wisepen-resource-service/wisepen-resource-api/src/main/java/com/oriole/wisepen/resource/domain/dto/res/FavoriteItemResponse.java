package com.oriole.wisepen.resource.domain.dto.res;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 收藏列表条目响应（"按内容"或"按收藏集合"视图均使用此结构）
 */
@Data
public class FavoriteItemResponse {
    /** 资源基本信息 + resourceInteractionInfo 聚合数据；accessible=false 时仅 resourceId 有值 */
    private ResourceItemResponse resourceInfo;
    /** 该资源所在的所有收藏集合 ID 列表 */
    private List<String> collectionIds;
    private LocalDateTime favoritedAt;
    /** false 表示资源已删除/不存在，resourceInfo 中仅 resourceId 有值 */
    private Boolean accessible;
}
