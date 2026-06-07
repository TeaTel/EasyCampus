package com.campus.backend.controller;

import com.campus.backend.common.Result;
import com.campus.backend.common.SecurityUtils;
import com.campus.backend.dto.*;
import com.campus.backend.entity.UserBehavior;
import com.campus.backend.entity.UserFollow;
import com.campus.backend.entity.User;
import com.campus.backend.mapper.UserBehaviorMapper;
import com.campus.backend.mapper.UserFollowMapper;
import com.campus.backend.mapper.UserMapper;
import com.campus.backend.service.PostService;
import com.campus.backend.service.ProductService;
import com.campus.backend.service.impl.RecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v2/feed")
@RequiredArgsConstructor
public class FeedController {

    private final PostService postService;
    private final ProductService productService;
    private final UserFollowMapper userFollowMapper;
    private final UserMapper userMapper;
    private final UserBehaviorMapper userBehaviorMapper;
    private final RecommendationService recommendationService;

    @GetMapping("/recommend")
    public Result<Map<String, Object>> getRecommendations(@RequestParam(defaultValue = "6") int limit) {
        Long userId = SecurityUtils.getCurrentUserIdOrNull();
        if (userId == null) {
            return Result.success(Map.of("list", List.of(), "total", 0));
        }
        List<FeedItemVO> items = recommendationService.recommend(userId, limit);
        return Result.success(Map.of("list", items, "total", items.size()));
    }

    @PostMapping("/behavior")
    public Result<Void> recordBehavior(@RequestBody Map<String, Object> body) {
        Long userId = SecurityUtils.getCurrentUserIdOrNull();
        if (userId == null) return Result.success(null);

        String targetType = (String) body.get("targetType");
        Long targetId = body.get("targetId") != null ? Long.valueOf(body.get("targetId").toString()) : null;
        if (targetType == null || targetId == null) return Result.success(null);

        UserBehavior behavior = new UserBehavior();
        behavior.setUserId(userId);
        behavior.setBehaviorType("VIEW");
        behavior.setTargetType(targetType);
        behavior.setTargetId(targetId);
        userBehaviorMapper.insert(behavior);
        return Result.success(null);
    }

    @GetMapping
    public Result<Map<String, Object>> getFeed(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Long circleId,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String campusTag) {

        Long userId = SecurityUtils.getCurrentUserIdOrNull();

        // 限制最大页码，防止深度分页导致性能问题
        int MAX_PAGE = 50;
        if (page > MAX_PAGE) {
            return Result.success(Map.of("list", List.of(), "page", page, "size", size, "total", 0));
        }

        PostQueryDTO postQuery = new PostQueryDTO();
        postQuery.setPage(page);
        postQuery.setSize(size);

        ProductQueryDTO productQuery = new ProductQueryDTO();
        productQuery.setPage(page);
        productQuery.setSize(size);
        productQuery.setStatus(1);

        if (circleId != null) {
            postQuery.setBoardId(circleId);
            productQuery.setCategoryId(circleId.intValue());
        }

        if (tag != null && !tag.isEmpty()) {
            postQuery.setTag(tag);
            productQuery.setTag(tag);
        }

        if (campusTag != null && !campusTag.isEmpty()) {
            postQuery.setCampusTag(campusTag);
            productQuery.setCampusTag(campusTag);
        }

        if ("following".equals(type) && userId != null) {
            List<Long> followeeIds = getFolloweeIds(userId);
            if (followeeIds.isEmpty()) {
                // 关注列表为空时，fallback到发现模式，显示所有内容
                type = "discover";
            } else {
                postQuery.setUserIds(followeeIds);
                productQuery.setSellerIds(followeeIds);
            }
        } else if ("campus".equals(type) && userId != null) {
            List<Long> campusUserIds = getCampusUserIds(userId);
            if (campusUserIds.isEmpty()) {
                // 校区用户为空时，fallback到发现模式
                type = "discover";
            } else {
                postQuery.setUserIds(campusUserIds);
                productQuery.setSellerIds(campusUserIds);
            }
        }

        // 获取总数
        int postTotal = postService.getPostCount(postQuery);
        int productTotal = productService.getProductCount(productQuery);
        int total = postTotal + productTotal;

        // 合并分页策略：查询足够多的数据以保证合并排序后能正确分页
        // 需要查询 (page * size) 条数据，因为合并排序后位置可能变化
        int fetchSize = page * size;
        postQuery.setSize(fetchSize);
        productQuery.setSize(fetchSize);
        // 对于合并查询，始终从第1页开始获取，在内存中做统一分页
        postQuery.setPage(1);
        productQuery.setPage(1);

        List<PostVO> posts = postService.getPostList(postQuery, userId);
        List<ProductVO> products = productService.getProductList(productQuery);

        List<FeedItemVO> items = new ArrayList<>();

        for (PostVO post : posts) {
            FeedItemVO item = new FeedItemVO();
            item.setItemType("POST");
            item.setId(post.getId());
            item.setTitle(post.getTitle());
            item.setContent(post.getContent());
            item.setLikeCount(post.getLikeCount());
            item.setCommentCount(post.getCommentCount());
            item.setViewCount(post.getViewCount());
            item.setIsLiked(post.getIsLiked());
            item.setUserId(post.getUserId());
            item.setUserName(post.getUserName());
            item.setUserAvatar(post.getUserAvatar());
            item.setBoardId(post.getBoardId());
            item.setBoardName(post.getBoardName());
            item.setPostType(post.getPostType());
            item.setLocation(post.getLocation());
            item.setCreatedAt(post.getCreatedAt());
            item.setStartTime(post.getStartTime());
            item.setIsAd(post.getIsAd());
            item.setExposureBoost(post.getExposureBoost());
            item.setTags(post.getTags());
            item.setCampusTag(post.getCampusTag());
            item.setUserCampus(post.getUserCampus());
            item.setCoverImage(post.getCoverImage());
            items.add(item);
        }

        for (ProductVO product : products) {
            FeedItemVO item = new FeedItemVO();
            item.setItemType("PRODUCT");
            item.setId(product.getId());
            item.setTitle(product.getName());
            item.setContent(product.getDescription());
            item.setCoverImage(product.getCoverImage());
            item.setPrice(product.getPrice() != null ? product.getPrice().toString() : null);
            item.setOriginalPrice(product.getOriginalPrice() != null ? product.getOriginalPrice().toString() : null);
            item.setLikeCount(product.getLikeCount());
            item.setViewCount(product.getViewCount());
            item.setIsLiked(product.getIsFavorited());
            item.setUserId(product.getSellerId());
            item.setUserName(product.getSellerName());
            item.setUserAvatar(product.getSellerAvatar());
            item.setCategoryId(product.getCategoryId());
            item.setCategoryName(product.getCategoryName());
            item.setHasStory(product.getHasStory());
            item.setConditionText(product.getConditionText());
            item.setLocation(product.getLocation());
            item.setCreatedAt(product.getCreatedAt());
            item.setTags(product.getTags());
            item.setCampusTag(product.getCampusTag());
            item.setUserCampus(product.getSellerCampus());
            items.add(item);
        }

        // 按创建时间降序排列，广告根据曝光率提升排序权重
        items.sort((a, b) -> {
            long aTime = a.getCreatedAt() != null ? a.getCreatedAt().toEpochSecond(java.time.ZoneOffset.UTC) : 0;
            long bTime = b.getCreatedAt() != null ? b.getCreatedAt().toEpochSecond(java.time.ZoneOffset.UTC) : 0;
            int aBoost = (Boolean.TRUE.equals(a.getIsAd()) && a.getExposureBoost() != null) ? a.getExposureBoost() : 0;
            int bBoost = (Boolean.TRUE.equals(b.getIsAd()) && b.getExposureBoost() != null) ? b.getExposureBoost() : 0;
            long aScore = aTime + (long) aBoost * 3600;
            long bScore = bTime + (long) bBoost * 3600;
            return Long.compare(bScore, aScore);
        });

        // 按 itemType+id 去重
        List<FeedItemVO> deduped = new ArrayList<>();
        java.util.Set<String> seen = new java.util.HashSet<>();
        for (FeedItemVO item : items) {
            String key = item.getItemType() + ":" + item.getId();
            if (seen.add(key)) {
                deduped.add(item);
            }
        }

        // 正确的分页截取：从 (page-1)*size 开始，截取 size 条
        int fromIndex = Math.min((page - 1) * size, deduped.size());
        int toIndex = Math.min(page * size, deduped.size());
        List<FeedItemVO> pageItems = deduped.subList(fromIndex, toIndex);

        Map<String, Object> result = Map.of(
                "list", pageItems,
                "page", page,
                "size", size,
                "total", total
        );
        return Result.success(result);
    }

    private List<Long> getFolloweeIds(Long userId) {
        List<UserFollow> follows = userFollowMapper.selectFollowing(userId, 0, 200);
        return follows.stream()
                .map(UserFollow::getFolloweeId)
                .collect(Collectors.toList());
    }

    private List<Long> getCampusUserIds(Long userId) {
        User currentUser = userMapper.selectById(userId);
        if (currentUser == null || currentUser.getSchool() == null || currentUser.getSchool().isEmpty()) {
            return List.of();
        }
        return userMapper.selectIdsBySchool(currentUser.getSchool());
    }
}
