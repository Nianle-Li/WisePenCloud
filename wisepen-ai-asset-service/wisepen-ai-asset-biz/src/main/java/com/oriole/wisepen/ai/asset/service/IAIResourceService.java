package com.oriole.wisepen.ai.asset.service;

import com.oriole.wisepen.ai.asset.domain.base.AIResourceInfoBase;
import com.oriole.wisepen.ai.asset.domain.dto.req.AIResourceCreateRequest;
import com.oriole.wisepen.ai.asset.domain.dto.req.AIResourceForkRequest;
import com.oriole.wisepen.ai.asset.domain.dto.req.AIResourceUpdateRequest;
import com.oriole.wisepen.ai.asset.domain.dto.res.AIResourceMetaInfoResponse;

import java.util.List;

public interface IAIResourceService {

    String createAIResource(AIResourceCreateRequest req, String userId);

    String forkAIResource(AIResourceForkRequest req, String forkedResourceOwnerId);

    void deleteAIResources(List<String> resourceIds);

    void updateAIResourceInfo(AIResourceUpdateRequest req);

    AIResourceInfoBase getAIResourceInfo(String resourceId);

    List<AIResourceMetaInfoResponse> listPublishedAIResourcesMeta(List<String> resourceIds);

}
