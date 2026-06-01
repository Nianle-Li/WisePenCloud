package com.oriole.wisepen.user.controller;

import cn.hutool.core.util.IdUtil;
import com.oriole.wisepen.common.core.context.SecurityContextHolder;
import com.oriole.wisepen.common.core.domain.R;
import com.oriole.wisepen.common.core.domain.enums.BusinessType;
import com.oriole.wisepen.common.core.domain.enums.IdentityType;
import com.oriole.wisepen.common.log.annotation.Log;
import com.oriole.wisepen.common.security.annotation.CheckRole;
import com.oriole.wisepen.user.api.domain.dto.req.WalletChangeCoinBalanceRequest;
import com.oriole.wisepen.user.service.IWalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/user/wallet")
@RequiredArgsConstructor
@CheckRole(IdentityType.ADMIN)
public class AdminWalletController {

    private final IWalletService walletService;

    @PostMapping("/changeBalance")
    @Log(title = "管理员调整用户信息点", businessType = BusinessType.UPDATE)
    public R<Void> changeBalance(@RequestBody @Valid WalletChangeCoinBalanceRequest req) {
        Long operatorId = SecurityContextHolder.getUserId();
        String traceId = IdUtil.randomUUID();
        walletService.changeCoinBalance(req.getUserId(), operatorId, traceId, req.getChangedCoin(), req.getWalletTransactionType(), req.getMeta());
        return R.ok();
    }
}
