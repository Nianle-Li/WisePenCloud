package com.oriole.wisepen.resource.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import com.oriole.wisepen.common.core.domain.PageR;
import com.oriole.wisepen.common.core.domain.enums.GroupRoleType;
import com.oriole.wisepen.common.core.domain.enums.GroupType;
import com.oriole.wisepen.common.core.exception.ServiceException;
import com.oriole.wisepen.resource.domain.MarketOfferInfo;
import com.oriole.wisepen.resource.domain.dto.req.MarketAuditOfferRequest;
import com.oriole.wisepen.resource.domain.dto.req.MarketPublishOfferRequest;
import com.oriole.wisepen.resource.domain.dto.req.MarketOffShelfOfferRequest;
import com.oriole.wisepen.resource.domain.dto.req.MarketPurchaseRequest;
import com.oriole.wisepen.resource.domain.dto.res.MarketOrderResponse;
import com.oriole.wisepen.resource.domain.entity.MarketOrderEntity;
import com.oriole.wisepen.resource.domain.entity.ResourceItemEntity;
import com.oriole.wisepen.resource.domain.mq.ResourceForkMessage;
import com.oriole.wisepen.resource.enums.MarketOfferStatus;
import com.oriole.wisepen.resource.enums.MarketPurchaseType;
import com.oriole.wisepen.resource.exception.ResourceError;
import com.oriole.wisepen.resource.repository.MarketOrderRepository;
import com.oriole.wisepen.resource.repository.ResourceItemRepository;
import com.oriole.wisepen.resource.mq.IResourceEventPublisher;
import com.oriole.wisepen.resource.service.IMarketService;
import com.oriole.wisepen.resource.service.IResourceService;
import com.oriole.wisepen.user.api.domain.base.GroupDisplayBase;
import com.oriole.wisepen.user.api.domain.dto.req.WalletSettleCoinTradeRequest;
import com.oriole.wisepen.user.api.feign.RemoteUserService;
import com.oriole.wisepen.user.api.feign.RemoteWalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MarketServiceImpl implements IMarketService {

    private final MarketOrderRepository marketOrderRepository;
    private final ResourceItemRepository resourceItemRepository;
    private final IResourceService resourceService;
    private final IResourceEventPublisher resourceEventPublisher;
    private final RemoteUserService remoteUserService;
    private final RemoteWalletService remoteWalletService;

    @Override
    public void publishOffer(MarketPublishOfferRequest request, Long sellerId, Map<Long, GroupRoleType> groupRoles) {
        // 检验是否为资源拥有者
        ResourceItemEntity resource = resourceItemRepository.findById(request.getResourceId())
                .orElseThrow(() -> new ServiceException(ResourceError.RESOURCE_NOT_FOUND));
        if (!sellerId.toString().equals(resource.getOwnerId())) {
            throw new ServiceException(ResourceError.RESOURCE_PERMISSION_DENIED);
        }

        checkPermission(request.getMarketGroupId(), groupRoles);

        MarketOfferInfo offer = resource.getMarketOfferInfo();
        if (offer != null && offer.getStatus() == MarketOfferStatus.BANNED) {
            throw new ServiceException(ResourceError.MARKET_OFFER_BANNED);
        }
        if (offer != null && offer.getStatus() == MarketOfferStatus.PUBLISHED) {
            throw new ServiceException(ResourceError.MARKET_OFFER_ALREADY_EXISTS);
        }

        resourceService.updateGroupResourceTags(resource, request.getMarketGroupId(), sellerId.toString(), GroupRoleType.MEMBER, request.getTagIds());

        LocalDateTime now = LocalDateTime.now();
        offer = resource.getMarketOfferInfo();
        if (offer == null) {
            offer = MarketOfferInfo.builder().build();
            resource.setMarketOfferInfo(offer);
        }

        offer.setPrice(request.getPrice());
        offer.setOfferVersion(request.getOfferVersion());
        offer.setStatus(MarketOfferStatus.PENDING);
        offer.setSellerId(sellerId.toString());
        offer.setPublishedAt(now);
        offer.setOffShelfAt(null);
        offer.setAuditMessage(null);
        offer.setAuditedAt(null);
        offer.setAuditorId(null);
        resource = resourceItemRepository.save(resource);
        log.info("marketOffer published resourceId={} sellerId={} marketGroupId={}",
                resource.getResourceId(), sellerId, request.getMarketGroupId());
    }

    @Override
    public void offShelfOffer(MarketOffShelfOfferRequest request, Long operatorId, Map<Long, GroupRoleType> groupRoles) {
        ResourceItemEntity resource = resourceItemRepository.findById(request.getResourceId())
                .orElseThrow(() -> new ServiceException(ResourceError.RESOURCE_NOT_FOUND));
        MarketOfferInfo offer = resource.getMarketOfferInfo();
        if (offer == null) {
            throw new ServiceException(ResourceError.MARKET_OFFER_NOT_FOUND);
        }

        GroupRoleType marketRole = checkPermission(request.getMarketGroupId(), groupRoles);
        if (!operatorId.toString().equals(resource.getOwnerId()) && marketRole != GroupRoleType.OWNER && marketRole != GroupRoleType.ADMIN) {
            throw new ServiceException(ResourceError.RESOURCE_PERMISSION_DENIED);
        }

        resourceService.updateGroupResourceTags(resource, request.getMarketGroupId(), operatorId.toString(), marketRole, null);
        offer = resource.getMarketOfferInfo();
        if (offer == null) {
            throw new ServiceException(ResourceError.MARKET_OFFER_NOT_FOUND);
        }

        LocalDateTime now = LocalDateTime.now();
        offer.setStatus(MarketOfferStatus.OFF_SHELF);
        offer.setOffShelfAt(now);
        resource = resourceItemRepository.save(resource);
        log.info("marketOffer offShelf resourceId={} operatorId={}",
                resource.getResourceId(), operatorId);
    }

    @Override
    public void auditOffer(MarketAuditOfferRequest request, Long operatorId, Map<Long, GroupRoleType> groupRoles) {
        if ((request.getStatus() == MarketOfferStatus.REJECTED
                || request.getStatus() == MarketOfferStatus.BANNED)
                && !StringUtils.hasText(request.getAuditMessage())) {
            throw new ServiceException(ResourceError.MARKET_AUDIT_MESSAGE_REQUIRED);
        }

        ResourceItemEntity resource = resourceItemRepository.findById(request.getResourceId())
                .orElseThrow(() -> new ServiceException(ResourceError.RESOURCE_NOT_FOUND));
        MarketOfferInfo offer = resource.getMarketOfferInfo();
        if (offer == null) {
            throw new ServiceException(ResourceError.MARKET_OFFER_NOT_FOUND);
        }

        checkPermission(request.getMarketGroupId(), groupRoles);

        offer.setStatus(request.getStatus());
        offer.setAuditMessage(request.getAuditMessage());
        offer.setAuditedAt(LocalDateTime.now());
        offer.setAuditorId(operatorId.toString());
        resourceItemRepository.save(resource);
        log.info("marketOffer audited resourceId={} operatorId={} status={}",
                resource.getResourceId(), operatorId, request.getStatus());
    }

    @Override
    public MarketOrderResponse purchase(MarketPurchaseRequest request, Long buyerId, Map<Long, GroupRoleType> groupRoles) {
        ResourceItemEntity resource = resourceItemRepository.findById(request.getResourceId())
                .orElseThrow(() -> new ServiceException(ResourceError.RESOURCE_NOT_FOUND));
        MarketOfferInfo offer = resource.getMarketOfferInfo();
        if (offer == null) {
            throw new ServiceException(ResourceError.MARKET_OFFER_NOT_FOUND);
        }
        if (offer.getStatus() != MarketOfferStatus.PUBLISHED) {
            throw new ServiceException(ResourceError.MARKET_OFFER_NOT_ACTIVE);
        }
        if (buyerId.toString().equals(offer.getSellerId())) {
            throw new ServiceException(ResourceError.MARKET_SELF_ORDER_NOT_ALLOWED);
        }

        checkPermission(request.getMarketGroupId(), groupRoles);

        String traceId = "market:" + resource.getResourceId() + ":" + request.getPurchaseType() + ":" + buyerId;
        MarketOrderEntity existing = marketOrderRepository.findByTradeTraceId(traceId).orElse(null);
        if (existing != null) {
            // TODO：补差价购买方式
            throw new ServiceException(ResourceError.MARKET_ORDER_ALREADY_EXISTS);
        }

        // TODO: 目前两种购买方式采用相同价格，后面可以卖家自己设置两种金额 or 同一配置比率（FORK 一次价格 = 50% × FORK 无限次价格）
        Integer paidPrice = offer.getPrice();
        WalletSettleCoinTradeRequest tradeRequest = WalletSettleCoinTradeRequest.builder()
                .traceId(traceId)
                .buyerId(buyerId)
                .sellerId(Long.valueOf(offer.getSellerId()))
                .price(paidPrice)
                .meta("market resource " + resource.getResourceId() + " " + request.getPurchaseType())
                .build();
        remoteWalletService.settleCoinTrade(tradeRequest);

        MarketOrderEntity order = BeanUtil.copyProperties(resource, MarketOrderEntity.class);
        BeanUtil.copyProperties(offer, order);
        order.setSourceResourceId(resource.getResourceId());
        order.setBuyerId(buyerId.toString());
        order.setPurchaseType(request.getPurchaseType());
        order.setPaidPrice(paidPrice);
        order.setPurchasedOfferVersion(offer.getOfferVersion());
        order.setForkCount(0);
        order.setTradeTraceId(traceId);
        MarketOrderEntity saved = marketOrderRepository.save(order);
        fork(saved.getOrderId(), buyerId);
        saved = marketOrderRepository.findById(saved.getOrderId()).orElse(saved);
        log.info("marketOrder created orderId={} resourceId={} buyerId={} purchaseType={} forkCount={}",
                saved.getOrderId(), resource.getResourceId(), buyerId, request.getPurchaseType(), saved.getForkCount());
        return BeanUtil.copyProperties(saved, MarketOrderResponse.class);
    }

    @Override
    public void fork(String orderId, Long buyerId) {
        MarketOrderEntity order = marketOrderRepository.findById(orderId)
                .orElseThrow(() -> new ServiceException(ResourceError.MARKET_ORDER_NOT_FOUND));
        if (!buyerId.toString().equals(order.getBuyerId())) {
            throw new ServiceException(ResourceError.RESOURCE_PERMISSION_DENIED);
        }

        ResourceItemEntity source = resourceItemRepository.findById(order.getSourceResourceId())
                .orElseThrow(() -> new ServiceException(ResourceError.RESOURCE_NOT_FOUND));
        MarketOfferInfo offer = source.getMarketOfferInfo();
        if (offer == null) {
            throw new ServiceException(ResourceError.MARKET_OFFER_NOT_FOUND);
        }
        if (offer.getStatus() == MarketOfferStatus.BANNED) {
            throw new ServiceException(ResourceError.MARKET_OFFER_BANNED);
        }

        if (order.getPurchaseType() == MarketPurchaseType.FORK_ONCE) {
            if (order.getForkCount() >= 1) {
                throw new ServiceException(ResourceError.MARKET_FORK_QUOTA_EXHAUSTED);
            }
        } else if (order.getPurchaseType() != MarketPurchaseType.FORK_UNLIMITED) {
            throw new ServiceException(ResourceError.MARKET_PURCHASE_TYPE_INVALID);
        }
        order.setForkCount(order.getForkCount() + 1);
        order = marketOrderRepository.save(order);

        String forkTaskId = IdUtil.fastSimpleUUID();
        ResourceForkMessage forkMessage = ResourceForkMessage.builder()
                .forkTaskId(forkTaskId)
                .sourceResourceId(order.getSourceResourceId())
                .resourceType(source.getResourceType())
                .version(offer.getOfferVersion())
                .buyerId(buyerId)
                .resourceName(source.getResourceName())
                .preview(source.getPreview())
                .size(source.getSize())
                .build();
        resourceEventPublisher.publishResourceForkEvent(forkMessage);
        log.info("marketFork published orderId={} forkTaskId={} sourceResourceId={} purchaseType={} version={} forkCount={}",
                order.getOrderId(), forkTaskId, order.getSourceResourceId(), order.getPurchaseType(), offer.getOfferVersion(), order.getForkCount());
    }

    @Override
    public PageR<MarketOrderResponse> listMyOrders(String buyerId, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), size);
        Page<MarketOrderEntity> entityPage = marketOrderRepository.findByBuyerId(buyerId, pageable);
        PageR<MarketOrderResponse> pageR = new PageR<>(entityPage.getTotalElements(), page, size);
        pageR.addAll(entityPage.getContent().stream()
                .map(entity -> BeanUtil.copyProperties(entity, MarketOrderResponse.class))
                .toList());
        return pageR;
    }

    // 检验是否为 MarketGroup（如果 MarketGroup 能有固定 ID 可以不要这个逻辑）
    private GroupRoleType checkPermission(String marketGroupId, Map<Long, GroupRoleType> groupRoles) {
        Long marketGroupIdValue = Long.valueOf(marketGroupId);
        Map<Long, GroupDisplayBase> groupMap = remoteUserService.getGroupDisplayInfo(List.of(marketGroupIdValue)).getData();
        GroupDisplayBase groupInfo = groupMap == null ? null : groupMap.get(marketGroupIdValue);
        if (groupInfo == null || groupInfo.getGroupType() != GroupType.MARKET_GROUP) {
            throw new ServiceException(ResourceError.MARKET_GROUP_REQUIRED);
        }
        return groupRoles == null ? null : groupRoles.get(marketGroupIdValue);
    }
}
