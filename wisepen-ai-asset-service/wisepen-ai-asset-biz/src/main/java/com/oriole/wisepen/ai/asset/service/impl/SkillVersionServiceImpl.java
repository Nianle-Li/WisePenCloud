package com.oriole.wisepen.ai.asset.service.impl;

import com.oriole.wisepen.ai.asset.domain.entity.SkillEntity;
import com.oriole.wisepen.ai.asset.domain.entity.SkillVersionBundleEntity;
import com.oriole.wisepen.ai.asset.enums.VersionStatus;
import com.oriole.wisepen.ai.asset.mq.AIAssetEventPublisher;
import com.oriole.wisepen.ai.asset.repository.AIResourceBaseRepository;
import com.oriole.wisepen.ai.asset.repository.VersionBundleBaseRepository;
import com.oriole.wisepen.file.storage.api.enums.StorageSceneEnum;
import com.oriole.wisepen.file.storage.api.feign.RemoteStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Slf4j
@Service
public class SkillVersionServiceImpl extends VersionServiceImpl<SkillVersionBundleEntity, SkillEntity> {

    public SkillVersionServiceImpl(VersionBundleBaseRepository<SkillVersionBundleEntity> versionBundleBaseRepository, AIResourceBaseRepository<SkillEntity> aiResourceBaseRepository, RemoteStorageService remoteStorageService, AIAssetEventPublisher eventPublisher) {
        super(versionBundleBaseRepository, aiResourceBaseRepository, remoteStorageService, eventPublisher);
    }

    @Override
    protected SkillVersionBundleEntity buildDraft(String resourceId, Integer draftVersion) {
        return SkillVersionBundleEntity.builder()
                .resourceId(resourceId).version(draftVersion).status(VersionStatus.DRAFT).assets(new ArrayList<>())
                .build();
    }

    @Override
    protected StorageSceneEnum getStorageScene() {
        return StorageSceneEnum.PRIVATE_SKILL_ASSET;
    }
}