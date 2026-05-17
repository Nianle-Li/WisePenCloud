package com.oriole.wisepen.resource.domain.dto.res;

import lombok.Data;

@Data
public class ResourceInteractStateResponse {
    private String resourceId;
    private Boolean liked;
    private Long likeCount;
    private Integer userScore;
}
