package com.oriole.wisepen.ai.asset.domain.entity;

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
public abstract class VersionBundleBaseEntity<T extends VersionBundleBaseEntity<T>> extends VersionBundleBase {

    @Id
    private String id;

    private String resourceId;

    @CreatedDate
    private LocalDateTime createTime;

    @LastModifiedDate
    private LocalDateTime updateTime;

    // 发布前的类型相关校验：skill 校验核心文件，agent 校验 spec
    public abstract void checkCoreAssetReady();

    public abstract void copyBy(T entity);
}
