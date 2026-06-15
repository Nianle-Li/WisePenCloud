package com.oriole.wisepen.ai.asset.domain.entity;

import com.oriole.wisepen.ai.asset.domain.base.AIResourceInfoBase;
import com.oriole.wisepen.ai.asset.domain.base.VersionBundleBase;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
public abstract class AIResourceBaseEntity<T extends AIResourceBaseEntity<T>> extends AIResourceInfoBase {
    @Id
    private String resourceId;

    @CreatedDate
    private LocalDateTime createTime;

    @LastModifiedDate
    private LocalDateTime updateTime;
}
