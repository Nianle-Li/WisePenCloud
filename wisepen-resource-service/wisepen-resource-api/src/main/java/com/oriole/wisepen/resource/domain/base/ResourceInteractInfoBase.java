package com.oriole.wisepen.resource.domain.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 资源互动信息基类，承载阅读量等可持续扩展的互动统计字段。
 * scoreAvg 为派生值（scoreTotal / scoreCount），不存储于 MongoDB，由实体层动态计算。
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceInteractInfoBase {
    private Long readCount;     // 资源有效阅读量，默认 0
    private Long likeCount;     // 点赞总数，默认 0
    private Integer scoreCount; // 评分人数，默认 0
    private Long scoreTotal;    // 评分总和，默认 0
}
