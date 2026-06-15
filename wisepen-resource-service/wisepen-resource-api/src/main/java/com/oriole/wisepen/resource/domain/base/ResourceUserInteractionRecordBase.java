package com.oriole.wisepen.resource.domain.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceUserInteractionRecordBase {
    private Boolean read = false;   // 用户是否阅读过该资源
    private Boolean liked = false;  // 用户是否赞过该资源
    private Integer score;  // 用户对该资源的评分
    private List<String> likedCommentIds = new ArrayList<>();  // 用户已点赞的评论/回复 ID 列表（顶级评论为纯 ObjectId，回复为 parentId_newObjectId 格式）
}
