package com.oriole.wisepen.user.api.domain.dto.req;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.oriole.wisepen.user.api.constant.WalletValidationMsg;
import com.oriole.wisepen.user.api.enums.WalletTransactionType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WalletChangeCoinBalanceRequest {

    @NotNull(message = WalletValidationMsg.COIN_TRADE_USER_ID_NOT_NULL)
    private Long userId;

    @NotNull(message = WalletValidationMsg.COIN_CHANGE_AMOUNT_NOT_NULL)
    private Integer changedCoin;

    @NotNull(message = WalletValidationMsg.COIN_TRADE_TYPE_NOT_NULL)
    private WalletTransactionType walletTransactionType;

    private String meta;
}
