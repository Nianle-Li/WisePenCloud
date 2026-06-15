package com.oriole.wisepen.resource.domain.dto.res;

import lombok.Data;

import java.util.List;

/**
 * 游标分页统一响应壳，仅供评论相关接口使用，不提前抽象至 wisepen-common
 */
@Data
public class CursorPageResponse<T> {
    private List<T> list;
    private Boolean hasMore;
    /** 当前页码，由客户端传入并原样回填，服务端不依赖此值做 skip */
    private Integer page;
    /** 时间排序/回复列表翻页游标（当前页最后一条 createTime 毫秒时间戳） */
    private Long nextCursorCreateTime;
    /** 热度排序翻页时与 nextCursorCreateTime 组合使用 */
    private Integer nextCursorLikeCount;
}
