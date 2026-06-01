package com.oriole.wisepen.user.api.feign;

import com.oriole.wisepen.common.core.domain.R;
import com.oriole.wisepen.user.api.domain.dto.req.WalletSettleCoinTradeRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "内部钱包服务", description = "提供给其他微服务的钱包接口")
@FeignClient(contextId = "remoteWalletService", value = "wisepen-user-service")
public interface RemoteWalletService {

    @Operation(summary = "内部信息点交易结算（幂等）")
    @PostMapping("/internal/user/wallet/settleTrade")
    R<Void> settleCoinTrade(@RequestBody WalletSettleCoinTradeRequest req);
}
