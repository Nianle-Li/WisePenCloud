package com.oriole.wisepen.resource.controller;

import com.oriole.wisepen.common.core.context.SecurityContextHolder;
import com.oriole.wisepen.common.core.domain.PageR;
import com.oriole.wisepen.common.core.domain.R;
import com.oriole.wisepen.common.core.domain.enums.BusinessType;
import com.oriole.wisepen.common.log.annotation.Log;
import com.oriole.wisepen.common.security.annotation.CheckLogin;
import com.oriole.wisepen.resource.constant.ResourceValidationMsg;
import com.oriole.wisepen.resource.domain.dto.req.FavoriteCollectionCreateRequest;
import com.oriole.wisepen.resource.domain.dto.req.FavoriteCollectionDeleteRequest;
import com.oriole.wisepen.resource.domain.dto.req.FavoriteCollectionItemRequest;
import com.oriole.wisepen.resource.domain.dto.req.FavoriteCollectionUpdateRequest;
import com.oriole.wisepen.resource.domain.dto.res.FavoriteCollectionResponse;
import com.oriole.wisepen.resource.domain.dto.res.FavoriteItemResponse;
import com.oriole.wisepen.resource.service.IFavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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

    @Operation(
            summary = "切换资源收藏状态",
            description = """
                    - 用途：在指定收藏集合中添加或取消收藏资源，同步维护资源的去重收藏人数。
                    - 请求：resourceId 指定目标资源；collectionId 指定目标收藏集合，不传时操作当前用户的默认收藏集合（默认集合不存在时自动创建）。
                    - 约束：当前用户必须已登录；指定 collectionId 时该集合必须归属当前用户；添加收藏时目标资源必须存在。
                    - 处理：若目标资源不在该集合中则添加，并在该用户首次收藏此资源时递增 favoriteCount；若已在集合中则移除，并在该用户不再收藏此资源时递减 favoriteCount；favoriteCount 为去重用户收藏数，允许极低概率的并发误差。
                    - 失败：目标资源不存在（仅添加时触发）-> ResourceError.RESOURCE_NOT_FOUND；指定的收藏集合不存在 -> ResourceError.FAVORITE_COLLECTION_NOT_FOUND；操作他人收藏集合 -> ResourceError.FAVORITE_COLLECTION_ACCESS_DENIED。
                    - 响应：成功时返回空结果。
                    """
    )
    @PostMapping("/switch")
    @Log(title = "收藏管理", businessType = BusinessType.UPDATE)
    public R<Void> switchCollectionItem(@Validated @RequestBody FavoriteCollectionItemRequest request) {
        String userId = SecurityContextHolder.getUserId().toString();
        favoriteService.switchCollectionItem(request, userId);
        return R.ok();
    }

    @Operation(
            summary = "查询当前用户收藏集合列表",
            description = """
                    - 用途：获取当前用户创建的所有收藏集合，用于收藏集合列表展示和收藏操作时的集合选择。
                    - 请求：无入参，取当前登录用户的全部收藏集合。
                    - 约束：当前用户必须已登录。
                    - 处理：查询当前用户全部收藏集合，按默认集合置顶、创建时间倒序排列；每条返回集合基本信息和当前收藏资源数量（含已删除资源的引用）；不加载资源详情。
                    - 失败：无业务失败点。
                    - 响应：返回收藏集合列表，默认集合排在最前。
                    """
    )
    @GetMapping("/collection/list")
    public R<List<FavoriteCollectionResponse>> listCollections() {
        String userId = SecurityContextHolder.getUserId().toString();
        return R.ok(favoriteService.listCollections(userId));
    }

    @Operation(
            summary = "新建收藏集合",
            description = """
                    - 用途：为当前用户创建一个自定义收藏集合，用于分类管理收藏资源。
                    - 请求：collectionName 为集合名称，不能为空；description 为可选描述，不传表示无描述。
                    - 约束：当前用户必须已登录；collectionName 不能为空字符串。
                    - 处理：创建新收藏集合，isDefault 固定为 false；不自动添加任何资源；返回服务端生成的 collectionId。
                    - 失败：无业务失败点。
                    - 响应：返回服务端生成的收藏集合 ID（ObjectId 字符串）。
                    """
    )
    @PostMapping("/collection/create")
    @Log(title = "收藏管理", businessType = BusinessType.INSERT)
    public R<String> createCollection(@Validated @RequestBody FavoriteCollectionCreateRequest request) {
        String userId = SecurityContextHolder.getUserId().toString();
        return R.ok(favoriteService.createCollection(request, userId));
    }

    @Operation(
            summary = "修改收藏集合名称或描述",
            description = """
                    - 用途：更新收藏集合的展示名称或描述，不影响集合内的资源引用和收藏关系。
                    - 请求：collectionId、collectionName、description 均在请求体中；Full Update 语义，必须同时传入 collectionName 和 description，description 传 null 表示清除描述。
                    - 约束：当前用户必须已登录；目标收藏集合必须存在且归属当前用户；collectionName 不能为空字符串。
                    - 处理：更新集合名称和描述；不修改 isDefault 标记、资源引用列表或创建时间。
                    - 失败：目标收藏集合不存在 -> ResourceError.FAVORITE_COLLECTION_NOT_FOUND；操作他人收藏集合 -> ResourceError.FAVORITE_COLLECTION_ACCESS_DENIED。
                    - 响应：成功时返回空结果。
                    """
    )
    @PostMapping("/collection/update")
    @Log(title = "收藏管理", businessType = BusinessType.UPDATE)
    public R<Void> updateCollection(@Validated @RequestBody FavoriteCollectionUpdateRequest request) {
        String userId = SecurityContextHolder.getUserId().toString();
        favoriteService.updateCollection(request, userId);
        return R.ok();
    }

    @Operation(
            summary = "删除收藏集合",
            description = """
                    - 用途：删除当前用户的指定收藏集合，并同步维护被影响资源的 favoriteCount。
                    - 请求：collectionId 在请求体中指定目标集合。
                    - 约束：当前用户必须已登录；目标收藏集合必须存在且归属当前用户；默认收藏集合不可删除。
                    - 处理：删除目标收藏集合；对集合内资源，若当前用户在其他收藏集合中不再持有该资源，则批量递减对应资源的 favoriteCount；不删除资源本身。
                    - 失败：目标收藏集合不存在 -> ResourceError.FAVORITE_COLLECTION_NOT_FOUND；操作他人收藏集合 -> ResourceError.FAVORITE_COLLECTION_ACCESS_DENIED；尝试删除默认收藏集合 -> ResourceError.DEFAULT_COLLECTION_CANNOT_DELETE。
                    - 响应：成功时返回空结果。
                    """
    )
    @PostMapping("/collection/delete")
    @Log(title = "收藏管理", businessType = BusinessType.DELETE)
    public R<Void> deleteCollection(@Validated @RequestBody FavoriteCollectionDeleteRequest request) {
        String userId = SecurityContextHolder.getUserId().toString();
        favoriteService.deleteCollection(request.getCollectionId(), userId);
        return R.ok();
    }

    @Operation(
            summary = "分页查询已收藏资源（跨集合去重）",
            description = """
                    - 用途：汇总当前用户在所有收藏集合中收藏的资源，跨集合去重后按最近收藏时间倒序分页展示。
                    - 请求：page 为页码（从 1 开始，默认 1）；size 为每页条数（默认 20，最大 100）。
                    - 约束：当前用户必须已登录；page 不能小于 1；size 不能超过 100。
                    - 处理：加载当前用户所有收藏集合，对所有资源按 resourceId 去重，同一资源出现在多个集合时取最新 favoritedAt；按 favoritedAt 倒序在内存中分页；批量查询资源详情，已删除资源 accessible=false 且仅返回 resourceId。
                    - 失败：无业务失败点。
                    - 响应：分页收藏条目列表，每条包含资源详情、最近收藏时间和 accessible 标记。若需查询某资源属于哪些集合，请使用 GET /resource/favorite/resourceCollections。
                    """
    )
    @GetMapping("/resources")
    public R<PageR<FavoriteItemResponse>> listFavoritedResources(
            @Min(value = 1, message = ResourceValidationMsg.PAGE_MIN_INVALID) @RequestParam(defaultValue = "1") int page,
            @Min(value = 1, message = ResourceValidationMsg.SIZE_MIN_INVALID) @Max(value = 100, message = ResourceValidationMsg.SIZE_MAX_INVALID) @RequestParam(defaultValue = "20") int size) {
        String userId = SecurityContextHolder.getUserId().toString();
        return R.ok(favoriteService.listFavoritedResources(page, size, userId));
    }

    @Operation(
            summary = "按收藏集合分页查询收藏条目",
            description = """
                    - 用途：分页查询指定收藏集合内的所有收藏条目，按收藏时间倒序展示。
                    - 请求：collectionId 通过 Query 参数指定目标集合；page 为页码（从 1 开始，默认 1）；size 为每页条数（默认 20，最大 100）。
                    - 约束：当前用户必须已登录；目标收藏集合必须存在且归属当前用户；page 不能小于 1；size 不能超过 100。
                    - 处理：加载目标收藏集合，对其 resources 按 favoritedAt 倒序在内存中分页；批量查询资源详情，已删除资源 accessible=false 且仅返回 resourceId。
                    - 失败：目标收藏集合不存在 -> ResourceError.FAVORITE_COLLECTION_NOT_FOUND；操作他人收藏集合 -> ResourceError.FAVORITE_COLLECTION_ACCESS_DENIED。
                    - 响应：分页收藏条目列表，每条包含资源详情、收藏时间和 accessible 标记。若需查询某资源属于哪些集合，请使用 GET /resource/favorite/resourceCollections。
                    """
    )
    @GetMapping("/listByCollection")
    public R<PageR<FavoriteItemResponse>> listByCollection(
            @NotBlank(message = ResourceValidationMsg.COLLECTION_ID_NOT_BLANK) @RequestParam String collectionId,
            @Min(value = 1, message = ResourceValidationMsg.PAGE_MIN_INVALID) @RequestParam(defaultValue = "1") int page,
            @Min(value = 1, message = ResourceValidationMsg.SIZE_MIN_INVALID) @Max(value = 100, message = ResourceValidationMsg.SIZE_MAX_INVALID) @RequestParam(defaultValue = "20") int size) {
        String userId = SecurityContextHolder.getUserId().toString();
        return R.ok(favoriteService.listFavoritesByCollection(collectionId, page, size, userId));
    }

    @Operation(
            summary = "查询当前用户收藏了指定资源的所有集合",
            description = """
                    - 用途：查询当前用户收藏了指定资源的所有集合，提供资源到集合的反向归属信息。
                    - 请求：resourceId 通过 Query 参数指定目标资源。
                    - 约束：当前用户必须已登录；resourceId 不能为空。
                    - 处理：遍历当前用户所有收藏集合，返回包含该资源的集合 ID 列表；不加载资源详情。
                    - 失败：无业务失败点。
                    - 响应：返回收藏了该资源的收藏集合 ID 列表，未收藏时返回空列表。
                    """
    )
    @GetMapping("/resourceCollections")
    public R<List<String>> listResourceCollections(
            @NotBlank(message = ResourceValidationMsg.RESOURCE_ID_NOT_BLANK) @RequestParam String resourceId) {
        String userId = SecurityContextHolder.getUserId().toString();
        return R.ok(favoriteService.listResourceCollections(resourceId, userId));
    }
}
