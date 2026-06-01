package com.oriole.wisepen.user.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.oriole.wisepen.user.api.domain.base.WalletTransactionRecordBase;

import com.oriole.wisepen.user.api.enums.WalletPayerType;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_token_record")
public class WalletTransactionRecordEntity extends WalletTransactionRecordBase implements Serializable {
	@TableId(type = IdType.ASSIGN_ID)
	Long id;
	// 计费方Id
	Long payerId;
	// 计费方类型
	WalletPayerType payerType;
	// 交易发起时间
	@TableField(value = "create_time", fill = FieldFill.INSERT)
	LocalDateTime createTime;
}