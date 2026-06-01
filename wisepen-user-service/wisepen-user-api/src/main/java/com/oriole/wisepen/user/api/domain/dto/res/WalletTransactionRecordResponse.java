package com.oriole.wisepen.user.api.domain.dto.res;

import com.oriole.wisepen.user.api.domain.base.WalletTransactionRecordBase;
import com.oriole.wisepen.user.api.domain.base.UserDisplayBase;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
public class WalletTransactionRecordResponse extends WalletTransactionRecordBase {
	UserDisplayBase operatorDisplay;
	LocalDateTime createTime;
}
