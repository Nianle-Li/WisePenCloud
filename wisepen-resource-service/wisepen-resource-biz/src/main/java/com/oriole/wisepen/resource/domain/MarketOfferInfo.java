package com.oriole.wisepen.resource.domain;

import com.oriole.wisepen.resource.domain.base.MarketOfferInfoBase;
import lombok.*;

/**
 * 嵌入 {@link com.oriole.wisepen.resource.domain.entity.ResourceItemEntity#marketOfferInfo} 的集市上架信息。
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketOfferInfo extends MarketOfferInfoBase {

}
