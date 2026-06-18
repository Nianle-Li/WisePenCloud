package com.oriole.wisepen.note.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.oriole.wisepen.common.core.exception.ServiceException;
import com.oriole.wisepen.note.api.domain.base.NoteInfoBase;
import com.oriole.wisepen.note.api.domain.dto.req.NoteCreateRequest;
import com.oriole.wisepen.note.api.domain.dto.req.NoteForkRequest;
import com.oriole.wisepen.note.api.domain.enums.VersionType;
import com.oriole.wisepen.note.domain.entity.NoteInfoEntity;
import com.oriole.wisepen.note.domain.entity.NoteVersionEntity;
import com.oriole.wisepen.note.exception.NoteError;
import com.oriole.wisepen.note.repository.NoteDocumentRepository;
import com.oriole.wisepen.note.repository.NoteVersionRepository;
import com.oriole.wisepen.note.service.INoteOperationLogService;
import com.oriole.wisepen.note.service.INoteService;
import com.oriole.wisepen.note.service.INoteVersionService;
import com.oriole.wisepen.resource.domain.dto.ResourceCreateReqDTO;
import com.oriole.wisepen.resource.enums.ResourceType;
import com.oriole.wisepen.resource.feign.RemoteResourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoteServiceImpl implements INoteService {

    private final NoteDocumentRepository noteDocumentRepository;

    private final NoteVersionRepository noteVersionRepository;
    private final INoteVersionService noteVersionService;
    private final INoteOperationLogService noteOperationLogService;
    private final RemoteResourceService remoteResourceService;

    @Override
    public String createNote(NoteCreateRequest request, String userId) {

        // 向 resource 服务注册Note资源
        String resourceId;
        try {
            resourceId = remoteResourceService.createResource(
                    ResourceCreateReqDTO.builder()
                            .resourceName(request.getTitle())
                            .resourceType(ResourceType.NOTE)
                            .ownerId(userId)
                            .build()
            ).getData();
        } catch (Exception e) {
            log.error("note resource register failed. dependency=resourceService", e);
            throw new ServiceException(NoteError.NOTE_REGISTER_RESOURCE_FAILED, e.getMessage());
        }

        List<Long> authors = new ArrayList<>();
        authors.add(Long.valueOf(userId));

        NoteInfoEntity doc = NoteInfoEntity.builder()
                .resourceId(resourceId)
                .lastUpdatedAt(LocalDateTime.now())
                .authors(authors)
                .build();
        noteDocumentRepository.save(doc);
        return resourceId;
    }

    @Override
    @Transactional
    public void deleteNotes(List<String> resourceIds) {
        if (resourceIds == null || resourceIds.isEmpty()) {
            return;
        }
        // 移除所有内容
        noteDocumentRepository.deleteByResourceIdIn(resourceIds);

        noteVersionService.deleteAllVersionsByResourceIds(resourceIds);
        noteOperationLogService.deleteAllOpLogsByResourceIds(resourceIds);
    }

    @Override
    public NoteInfoBase getNoteInfo(String resourceId) {
        NoteInfoEntity noteInfoEntity = noteDocumentRepository.findByResourceId(resourceId)
                .orElseThrow(() -> new ServiceException(NoteError.NOTE_NOT_FOUND));
        return BeanUtil.copyProperties(noteInfoEntity, NoteInfoBase.class);
    }

    @Override
    @Transactional
    public String forkNote(NoteForkRequest request, String forkedResourceOwnerId) {
        NoteInfoEntity sourceInfo = noteDocumentRepository.findByResourceId(request.getResourceId())
                .orElseThrow(() -> new ServiceException(NoteError.NOTE_NOT_FOUND));

        List<NoteVersionEntity> sourceVersions = new ArrayList<>();

        // 向 resource 服务注册 Forked 资源
        String targetResourceId;
        try {
            targetResourceId = remoteResourceService.createResource(ResourceCreateReqDTO.builder()
                    .resourceName(request.getForkedResourceName())
                    .resourceType(ResourceType.NOTE)
                    .ownerId(forkedResourceOwnerId)
                    .build()
            ).getData();
        } catch (Exception e) {
            log.error("note resource register failed. dependency=resourceService", e);
            throw new ServiceException(NoteError.NOTE_REGISTER_RESOURCE_FAILED, e.getMessage());
        }

        try {
            // 建立文档元信息
            NoteInfoEntity targetInfo = NoteInfoEntity.builder()
                    .resourceId(targetResourceId)
                    .authors(sourceInfo.getAuthors())
                    .plainText(sourceInfo.getPlainText())
                    .build();
            noteDocumentRepository.save(targetInfo);

            Long forkedResourceVersion = request.getForkedResourceVersion() == null
                    ? noteVersionRepository.findFirstByResourceIdOrderByVersionDesc(request.getResourceId())
                      .map(NoteVersionEntity::getVersion)
                      .orElse(0L)
                    : request.getForkedResourceVersion().longValue();

            // 复制 笔记内容
            // 查询指定资源在指定版本号（含）之前的最新 FULL 版本记录
            Optional<NoteVersionEntity> latestFull = noteVersionRepository
                    .findFirstByResourceIdAndTypeAndVersionLessThanEqualOrderByVersionDesc(
                            request.getResourceId(), VersionType.FULL, forkedResourceVersion);

            Long latestFullVersion = 0L;
            if (latestFull.isPresent()) { // 存在这样的 FULL 版本
                sourceVersions.add(latestFull.get());
                latestFullVersion = latestFull.get().getVersion();
            }
            // 查询指定资源在指定版本号区间内（从当前版本到最近的 FULL 版本）的所有 DELTA 版本记录，并按版本号升序排列
            // 如果没有最近的 FULL 版本，则到 0
            sourceVersions.addAll(noteVersionRepository
                    .findByResourceIdAndVersionGreaterThanAndVersionLessThanEqualAndTypeOrderByVersionAsc(
                            request.getResourceId(), latestFullVersion, forkedResourceVersion, VersionType.DELTA));

            if (!sourceVersions.isEmpty()) {
                List<NoteVersionEntity> targetVersions = sourceVersions.stream()
                        .peek(sourceVersion->sourceVersion.setResourceId(targetResourceId)).toList();
                noteVersionRepository.saveAll(targetVersions);
            }

            log.info("note fork finished. sourceResourceId={} resourceId={} version={}",
                    request.getResourceId(), targetResourceId, forkedResourceVersion);

            return targetResourceId;
        } catch (Exception e) {
            // 异常时回滚
            noteVersionRepository.deleteByResourceIdIn(List.of(targetResourceId));
            noteDocumentRepository.deleteByResourceIdIn(List.of(targetResourceId));
            log.warn("note fork compensated. sourceResourceId={} resourceId={}",
                    request.getResourceId(), targetResourceId, e);
            throw new ServiceException(NoteError.NOTE_FORK_FAILED, e.getMessage());
        }
    }
}
