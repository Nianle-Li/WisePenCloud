package com.oriole.wisepen.ai.asset.exception;

import com.oriole.wisepen.common.core.domain.IResult;
import com.oriole.wisepen.common.core.domain.ResultKey;
import com.oriole.wisepen.common.core.domain.enums.BusinessDomain;
import com.oriole.wisepen.common.core.exception.ErrorReason;
import com.oriole.wisepen.ai.asset.constant.AIAssetSubject;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * AI Resource 微服务(10)专属业务错误
 */
@Getter
@AllArgsConstructor
public enum AIResourceError implements IResult {

    // AI Resource 相关异常
    AI_RESOURCE_NOT_FOUND(9111, new ResultKey(BusinessDomain.AI_RESOURCE, AIAssetSubject.AI_RESOURCE, ErrorReason.NOT_FOUND), "AI资源不存在"),
    AI_RESOURCE_PERMISSION_DENIED(9121, new ResultKey(BusinessDomain.AI_RESOURCE, AIAssetSubject.AI_RESOURCE, ErrorReason.PERMISSION_DENIED), "无权访问或操作该AI资源"),
    AI_RESOURCE_REGISTER_FAILED(9131, new ResultKey(BusinessDomain.AI_RESOURCE, AIAssetSubject.AI_RESOURCE, ErrorReason.FAILED), "注册AI资源失败"),
    AI_RESOURCE_FORK_FAILED(9132, new ResultKey(BusinessDomain.AI_RESOURCE, AIAssetSubject.AI_RESOURCE, ErrorReason.FAILED), "AI资源复制失败"),

    // AI Resource 版本相关异常
    AI_RESOURCE_VERSION_NOT_FOUND(9211, new ResultKey(BusinessDomain.AI_RESOURCE, AIAssetSubject.AI_RESOURCE_VERSION, ErrorReason.NOT_FOUND), "AI资源版本不存在"),
    CANNOT_OPERATE_NON_DRAFT_AI_RESOURCE_VERSION(9221, new ResultKey(BusinessDomain.AI_RESOURCE, AIAssetSubject.AI_RESOURCE_VERSION, ErrorReason.STATE_INVALID), "不能操作非草稿状态的AI资源版本"),

    // AI Resource 资源相关异常
    AI_RESOURCE_CORE_ASSET_NOT_FOUND(9311, new ResultKey(BusinessDomain.AI_RESOURCE, AIAssetSubject.AI_RESOURCE_ASSET, ErrorReason.NOT_FOUND), "AI资源的关键Asset不存在"),
    AI_RESOURCE_ASSET_NOT_READY(9321, new ResultKey(BusinessDomain.AI_RESOURCE, AIAssetSubject.AI_RESOURCE_ASSET, ErrorReason.STATE_INVALID), "AI资源的Asset未就绪"),
    AI_RESOURCE_ASSET_PATH_INVALID(9331, new ResultKey(BusinessDomain.AI_RESOURCE, AIAssetSubject.AI_RESOURCE_ASSET, ErrorReason.INVALID), "AI资源的Asset路径不合法"),
    AI_RESOURCE_ASSET_UPLOAD_URL_APPLY_FAILED(9341, new ResultKey(BusinessDomain.AI_RESOURCE, AIAssetSubject.AI_RESOURCE_ASSET, ErrorReason.FAILED), "AI资源的Asset文件上传初始化失败");

    private final Integer code;
    private final ResultKey key;
    private final String msg;
}
