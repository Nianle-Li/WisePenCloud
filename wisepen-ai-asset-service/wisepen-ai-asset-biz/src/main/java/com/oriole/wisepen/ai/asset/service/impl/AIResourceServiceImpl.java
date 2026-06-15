package com.oriole.wisepen.ai.asset.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.oriole.wisepen.ai.asset.domain.base.AIResourceInfoBase;
import com.oriole.wisepen.ai.asset.domain.dto.req.AIResourceCreateRequest;
import com.oriole.wisepen.ai.asset.domain.dto.req.AIResourceUpdateRequest;
import com.oriole.wisepen.ai.asset.domain.dto.res.AIResourceMetaInfoResponse;
import com.oriole.wisepen.ai.asset.domain.entity.AIResourceBaseEntity;
import com.oriole.wisepen.ai.asset.domain.entity.VersionBundleBaseEntity;
import com.oriole.wisepen.ai.asset.enums.SkillSourceType;
import com.oriole.wisepen.ai.asset.exception.AIResourceError;
import com.oriole.wisepen.ai.asset.repository.AIResourceBaseRepository;
import com.oriole.wisepen.ai.asset.service.IAIResourceService;
import com.oriole.wisepen.ai.asset.service.IVersionService;
import com.oriole.wisepen.common.core.exception.ServiceException;
import com.oriole.wisepen.resource.domain.dto.ResourceCreateReqDTO;
import com.oriole.wisepen.resource.enums.ResourceType;
import com.oriole.wisepen.resource.feign.RemoteResourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public abstract class AIResourceServiceImpl<AT extends AIResourceBaseEntity<AT>, VT extends VersionBundleBaseEntity<VT>> implements IAIResourceService {

    private final AIResourceBaseRepository<AT> aiResourceBaseRepository;
    private final IVersionService<VT> versionService;
    private final RemoteResourceService remoteResourceService;

    protected abstract AT buildNewResource(String resourceId, String name, String description, SkillSourceType skillSourceType);

    protected abstract ResourceType getResourceType();

    @Override
    public String createAIResource(AIResourceCreateRequest req, String userId) {
        String resourceId = remoteResourceService.createResource(ResourceCreateReqDTO.builder()
                .resourceName(req.getTitle())
                .resourceType(getResourceType())
                .ownerId(userId)
                .build()).getData();
        if (!StringUtils.hasText(resourceId)) {
            throw new ServiceException(AIResourceError.AI_RESOURCE_REGISTER_FAILED);
        }

        String name = req.getName() == null ? "" : req.getName();
        String description = req.getDescription() == null ? "" : req.getDescription();
        SkillSourceType sourceType = req.getSourceType() == null ? SkillSourceType.MANUAL : req.getSourceType();
        AT entity = buildNewResource(resourceId, name, description, sourceType);
        aiResourceBaseRepository.save(entity);
        // 直接新建首份草案(1)
        versionService.createDraftVersion(resourceId, 1);
        return resourceId;
    }

    @Override
    @Transactional
    public void deleteAIResources(List<String> resourceIds) {
        versionService.deleteAllVersionsByResourceIds(resourceIds);
        aiResourceBaseRepository.deleteByResourceIdIn(resourceIds);
    }

    @Override
    public void updateAIResourceInfo(AIResourceUpdateRequest req) {
        AT entity = aiResourceBaseRepository.findByResourceId(req.getResourceId())
                .orElseThrow(() -> new ServiceException(AIResourceError.AI_RESOURCE_NOT_FOUND));

        if (req.getName() != null) entity.setName(req.getName());
        if (req.getDescription() != null) entity.setDescription(req.getDescription());

        aiResourceBaseRepository.save(entity);
    }

    @Override
    public AIResourceInfoBase getAIResourceInfo(String resourceId) {
        return aiResourceBaseRepository.findByResourceId(resourceId)
                .orElseThrow(() -> new ServiceException(AIResourceError.AI_RESOURCE_NOT_FOUND));
    }

    @Override
    public List<AIResourceMetaInfoResponse> listPublishedAIResourcesMeta(List<String> resourceIds) {
        return aiResourceBaseRepository.findByResourceIdInAndVersionGreaterThan(resourceIds, 0)
                .stream()
                .map(entity -> BeanUtil.copyProperties(entity, AIResourceMetaInfoResponse.class))
                .toList();
    }
}
