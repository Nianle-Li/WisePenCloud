package com.oriole.wisepen.resource.domain.base;

import com.oriole.wisepen.resource.enums.MarketOfferStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class MarketOfferInfoBase {
    private String sellerId;
    private Integer price;
    private Long offerVersion;
    private MarketOfferStatus status;
    private String auditorId;// 审核人用户 ID
    private String auditMessage;// 审核说明（驳回原因、管理员备注等）
    private LocalDateTime auditedAt;// 审核完成时间
    private LocalDateTime publishedAt;
    private LocalDateTime offShelfAt;
}
