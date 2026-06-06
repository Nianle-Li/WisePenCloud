package com.oriole.wisepen.resource.controller;

import com.oriole.wisepen.common.core.context.SecurityContextHolder;
import com.oriole.wisepen.common.core.domain.PageR;
import com.oriole.wisepen.common.core.domain.R;
import com.oriole.wisepen.common.core.domain.enums.BusinessType;
import com.oriole.wisepen.common.log.annotation.Log;
import com.oriole.wisepen.common.security.annotation.CheckLogin;
import com.oriole.wisepen.resource.constant.ResourceValidationMsg;
import com.oriole.wisepen.resource.domain.dto.req.FavoriteCollectionCreateRequest;
import com.oriole.wisepen.resource.domain.dto.req.FavoriteCollectionUpdateRequest;
import com.oriole.wisepen.resource.domain.dto.req.FavoriteToggleRequest;
import com.oriole.wisepen.resource.domain.dto.res.FavoriteCollectionResponse;
import com.oriole.wisepen.resource.domain.dto.res.FavoriteItemResponse;
import com.oriole.wisepen.resource.service.IFavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "收藏管理", description = "资源收藏与收藏集合管理")
@RestController
@RequestMapping("/resource/favorite")
@RequiredArgsConstructor
@CheckLogin
@Validated
public class FavoriteController {

    private final IFavoriteService favoriteService;

    @Operation(summary = "收藏/取消收藏资源")
    @PostMapping("/toggle")
    @Log(title = "收藏管理", businessType = BusinessType.UPDATE)
    public R<Void> toggleFavorite(@Validated @RequestBody FavoriteToggleRequest request) {
        String userId = SecurityContextHolder.getUserId().toString();
        favoriteService.toggleFavorite(request, userId);
        return R.ok();
    }

    @Operation(summary = "获取当前用户的收藏集合列表")
    @GetMapping("/collection/list")
    public R<List<FavoriteCollectionResponse>> listCollections() {
        String userId = SecurityContextHolder.getUserId().toString();
        return R.ok(favoriteService.listCollections(userId));
    }

    @Operation(summary = "新建收藏集合")
    @PostMapping("/collection/create")
    @Log(title = "收藏管理", businessType = BusinessType.INSERT)
    public R<String> createCollection(@Validated @RequestBody FavoriteCollectionCreateRequest request) {
        String userId = SecurityContextHolder.getUserId().toString();
        return R.ok(favoriteService.createCollection(request, userId));
    }

    @Operation(summary = "修改收藏集合名称或描述")
    @PostMapping("/collection/update")
    @Log(title = "收藏管理", businessType = BusinessType.UPDATE)
    public R<Void> updateCollection(
            @NotBlank(message = ResourceValidationMsg.COLLECTION_ID_NOT_BLANK) @RequestParam String collectionId,
            @Validated @RequestBody FavoriteCollectionUpdateRequest request) {
        String userId = SecurityContextHolder.getUserId().toString();
        favoriteService.updateCollection(collectionId, request, userId);
        return R.ok();
    }

    @Operation(summary = "删除收藏集合（默认收藏集合不可删除）")
    @DeleteMapping("/collection/delete")
    @Log(title = "收藏管理", businessType = BusinessType.DELETE)
    public R<Void> deleteCollection(
            @NotBlank(message = ResourceValidationMsg.COLLECTION_ID_NOT_BLANK) @RequestParam String collectionId) {
        String userId = SecurityContextHolder.getUserId().toString();
        favoriteService.deleteCollection(collectionId, userId);
        return R.ok();
    }

    @Operation(summary = "按内容分页查询收藏（同一资源归并展示）")
    @GetMapping("/listByContent")
    public R<PageR<FavoriteItemResponse>> listByContent(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        String userId = SecurityContextHolder.getUserId().toString();
        return R.ok(favoriteService.listByContent(page, size, userId));
    }

    @Operation(summary = "按收藏集合分页查询收藏条目")
    @GetMapping("/listByCollection")
    public R<PageR<FavoriteItemResponse>> listByCollection(
            @NotBlank(message = ResourceValidationMsg.COLLECTION_ID_NOT_BLANK) @RequestParam String collectionId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        String userId = SecurityContextHolder.getUserId().toString();
        return R.ok(favoriteService.listByCollection(collectionId, page, size, userId));
    }
}
