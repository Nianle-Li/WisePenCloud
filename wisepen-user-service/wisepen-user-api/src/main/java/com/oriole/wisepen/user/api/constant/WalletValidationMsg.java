package com.oriole.wisepen.user.api.constant;

public interface WalletValidationMsg {
    String COIN_CHANGE_AMOUNT_NOT_NULL = "变动数量不能为空";
    String COIN_CHANGE_AMOUNT_NOT_ZERO = "变动数量不能为0";
    String COIN_TRADE_TRACE_ID_NOT_BLANK = "交易追踪ID不能为空";
    String COIN_TRADE_USER_ID_NOT_NULL = "用户ID不能为空";
    String COIN_TRADE_PRICE_NOT_NULL = "交易价格不能为空";
    String COIN_INVALID_PRICE = "价格必须大于零";
    String COIN_SELF_TRANSACTION_NOT_ALLOWED = "不能与自己交易";
    String COIN_TRADE_TYPE_NOT_NULL = "交易类型不能为空";
}
