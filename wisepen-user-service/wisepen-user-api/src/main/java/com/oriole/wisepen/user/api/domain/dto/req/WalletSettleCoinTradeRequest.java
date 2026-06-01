package com.oriole.wisepen.user.api.domain.dto.req;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.oriole.wisepen.user.api.constant.WalletValidationMsg;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletSettleCoinTradeRequest {

    @NotBlank(message = WalletValidationMsg.COIN_TRADE_TRACE_ID_NOT_BLANK)
    private String traceId;

    @NotNull(message = WalletValidationMsg.COIN_TRADE_USER_ID_NOT_NULL)
    private Long buyerId;

    @NotNull(message = WalletValidationMsg.COIN_TRADE_USER_ID_NOT_NULL)
    private Long sellerId;

    @NotNull(message = WalletValidationMsg.COIN_TRADE_PRICE_NOT_NULL)
    @Positive(message = WalletValidationMsg.COIN_INVALID_PRICE)
    private Integer price;

    private String meta;

    @JsonIgnore
    @AssertTrue(message = WalletValidationMsg.COIN_SELF_TRANSACTION_NOT_ALLOWED)
    public boolean isNotSelfTransaction() {
        return buyerId == null || !buyerId.equals(sellerId);
    }
}
