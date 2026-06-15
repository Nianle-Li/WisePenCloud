package com.oriole.wisepen.resource.domain.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ToggleCommentLikeRequest {
    /** 顶级评论 ID（纯 ObjectId）或回复 ID（含 _） */
    @NotBlank
    private String targetId;
}
