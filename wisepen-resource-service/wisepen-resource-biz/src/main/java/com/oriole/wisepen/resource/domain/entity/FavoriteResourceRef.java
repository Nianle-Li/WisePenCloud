package com.oriole.wisepen.resource.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** 收藏集合内的资源引用 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteResourceRef {
    private String resourceId;
    /** 加入该收藏集合的时间 */
    private LocalDateTime favoritedAt;
}
