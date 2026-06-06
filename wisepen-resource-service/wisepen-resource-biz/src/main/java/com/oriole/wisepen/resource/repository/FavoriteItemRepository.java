package com.oriole.wisepen.resource.repository;

import com.oriole.wisepen.resource.domain.entity.FavoriteItemEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface FavoriteItemRepository extends MongoRepository<FavoriteItemEntity, String> {
    /** 按收藏集合分页查询（按收藏时间倒序） */
    List<FavoriteItemEntity> findByUserIdAndCollectionIdOrderByCreateTimeDesc(String userId, String collectionId, Pageable pageable);

    /** 查询收藏集合下所有条目（供 deleteCollection 级联删除使用） */
    List<FavoriteItemEntity> findByUserIdAndCollectionId(String userId, String collectionId);

    /** 查询用户对某资源的所有收藏记录（一个资源可在多个收藏集合） */
    List<FavoriteItemEntity> findByUserIdAndResourceId(String userId, String resourceId);

    /** 检查资源是否已在指定收藏集合中，供 toggleFavorite toggle 分支判断 */
    boolean existsByUserIdAndResourceIdAndCollectionId(String userId, String resourceId, String collectionId);

    /**
     * 删除指定用户在指定收藏集合对指定资源的收藏记录，返回删除记录数。
     * 通过返回值判断是否真正删除（避免先 find 后 delete 的 TOCTOU 问题）。
     * 注：Spring Data MongoDB 派生 deleteBy 方法固定返回 long，属框架约束，不可改为 int。
     */
    long deleteByUserIdAndResourceIdAndCollectionId(String userId, String resourceId, String collectionId);

    /** 查询用户对一批资源的所有收藏记录（供 deleteCollection 判断哪些资源仍在其他收藏集合） */
    List<FavoriteItemEntity> findByUserIdAndResourceIdIn(String userId, List<String> resourceIds);

    /** 统计收藏集合内条目数，供 listByCollection 分页 total 使用 */
    long countByUserIdAndCollectionId(String userId, String collectionId);

    /** 级联删除收藏集合下所有条目 */
    void deleteAllByCollectionId(String collectionId);

    /** 硬删除资源时清理孤立收藏记录 */
    void deleteAllByResourceIdIn(List<String> resourceIds);
}
