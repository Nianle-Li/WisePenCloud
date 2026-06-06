package com.oriole.wisepen.resource.domain.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Document(collection = "wisepen_favorite_items")
@CompoundIndexes({
    @CompoundIndex(def = "{'userId': 1, 'resourceId': 1, 'collectionId': 1}", unique = true),
    @CompoundIndex(def = "{'userId': 1, 'resourceId': 1}"),
    @CompoundIndex(def = "{'userId': 1, 'collectionId': 1}"),
    @CompoundIndex(def = "{'userId': 1, 'createTime': -1}")
})
public class FavoriteItemEntity {
    @Id
    private String id;

    private String userId;
    private String resourceId;
    private String collectionId;

    @CreatedDate
    private LocalDateTime createTime;
    @LastModifiedDate
    private LocalDateTime updateTime;

    public FavoriteItemEntity(String userId, String resourceId, String collectionId) {
        this.userId = userId;
        this.resourceId = resourceId;
        this.collectionId = collectionId;
    }
}
