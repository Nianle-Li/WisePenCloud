package com.oriole.wisepen.resource.domain.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CreateCommentRequest {
    @NotBlank
    private String resourceId;
    @NotBlank
    private String content;
    private List<String> imageUrls = new ArrayList<>();
}
