package com.oriole.wisepen.resource.service;

import com.oriole.wisepen.resource.domain.dto.req.ResourceRateRequest;
import com.oriole.wisepen.resource.domain.dto.req.ResourceToggleLikeRequest;
import com.oriole.wisepen.resource.domain.dto.res.ResourceInteractStateResponse;

public interface IResourceInteractService {
    ResourceInteractStateResponse toggleLike(ResourceToggleLikeRequest request);

    ResourceInteractStateResponse rateResource(ResourceRateRequest request);
}
