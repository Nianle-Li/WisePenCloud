package com.oriole.wisepen.ai.asset.constant;

import com.oriole.wisepen.common.core.domain.IBusinessSubject;

import java.util.Locale;

public enum AIAssetSubject implements IBusinessSubject {
    AI_RESOURCE,
    AI_RESOURCE_VERSION,
    AI_RESOURCE_ASSET;

    @Override
    public String key() {
        return name().toLowerCase(Locale.ROOT);
    }
}