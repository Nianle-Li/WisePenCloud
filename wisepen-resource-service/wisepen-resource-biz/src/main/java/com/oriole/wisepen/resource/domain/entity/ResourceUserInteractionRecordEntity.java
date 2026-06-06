package com.oriole.wisepen.resource.domain.entity;

import com.oriole.wisepen.resource.domain.base.ResourceUserInteractionRecordBase;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户维度互动记录，存储每个用户对每个资源的点赞状态与评分
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "wisepen_resource_user_interact_record")
@CompoundIndex(def = "{'resourceId': 1, 'userId': 1}", unique = true)
public class ResourceUserInteractionRecordEntity extends ResourceUserInteractionRecordBase {
    @Id
    private String id;

    private String resourceId;

    private String userId;

    // 写端追踪字段，由 CustomRepository 用 $addToSet/$pull 原子维护；读端从 FavoriteItemEntity 独立填充
    private List<String> favoritedCollectionIds;

    @CreatedDate
    private LocalDateTime createTime;
    @LastModifiedDate
    private LocalDateTime updateTime;

    public ResourceUserInteractionRecordEntity(String resourceId, String userId) {
        super(false, false, null);
        this.resourceId = resourceId;
        this.userId = userId;
    }
}
