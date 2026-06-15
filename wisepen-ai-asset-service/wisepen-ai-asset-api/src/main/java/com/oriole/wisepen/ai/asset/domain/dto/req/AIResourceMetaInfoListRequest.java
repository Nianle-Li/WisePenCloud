package com.oriole.wisepen.ai.asset.domain.dto.req;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AIResourceMetaInfoListRequest {
    private List<String> resourceIds = new ArrayList<>();
}
