package com.oriole.wisepen.resource.domain.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DeleteCommentItemRequest {
    /** 顶级评论 ID（不含 _）或回复 ID（含 _） */
    @NotBlank
    private String targetId;
}
