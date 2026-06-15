package com.oriole.wisepen.resource.domain.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CreateReplyRequest {
    /** 被回复目标 ID：顶级评论 ID（纯 ObjectId）或回复 ID（含 _） */
    @NotBlank
    private String parentId;
    @NotBlank
    private String replyToUserId;
    @NotBlank
    private String content;
    private List<String> imageUrls = new ArrayList<>();
}
