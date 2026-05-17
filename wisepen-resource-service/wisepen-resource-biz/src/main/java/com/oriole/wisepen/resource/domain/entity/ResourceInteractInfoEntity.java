package com.oriole.wisepen.resource.domain.entity;

import com.oriole.wisepen.resource.domain.base.ResourceInteractInfoBase;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@Document(collection = "wisepen_resource_interact_info")
public class ResourceInteractInfoEntity extends ResourceInteractInfoBase {
    @Id
    private String resourceId;

    @CreatedDate
    private LocalDateTime createTime;
    @LastModifiedDate
    private LocalDateTime updateTime;

    /**
     * scoreAvg 为派生值，不存储于 MongoDB，从 scoreCount / scoreTotal 实时计算。
     * scoreCount = 0 或数据缺失时返回 null（展示"暂无评分"）。
     */
    public Double getScoreAvg() {
        Integer count = getScoreCount();
        Long total = getScoreTotal();
        if (count == null || count <= 0 || total == null) return null;
        return (double) total / count;
    }
}
