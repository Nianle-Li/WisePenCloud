package com.oriole.wisepen.resource.domain.entity;

import com.oriole.wisepen.resource.domain.base.FavoriteCollectionBase;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@Document(collection = "wisepen_favorite_collections")
@CompoundIndexes({
    @CompoundIndex(def = "{'userId': 1}"),
    @CompoundIndex(def = "{'userId': 1, 'isDefault': 1}")
})
public class FavoriteCollectionEntity extends FavoriteCollectionBase {
    @Id
    private String collectionId;

    private String userId;

    /** 资源引用列表，按 favoritedAt 倒序 */
    private List<FavoriteResourceRef> resources = new ArrayList<>();

    @CreatedDate
    private LocalDateTime createTime;

    public FavoriteCollectionEntity(String userId, String collectionName, String description, Boolean isDefault) {
        super(collectionName, description, isDefault);
        this.userId = userId;
    }
}
