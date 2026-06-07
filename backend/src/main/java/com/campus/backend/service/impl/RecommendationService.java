package com.campus.backend.service.impl;

import com.campus.backend.dto.FeedItemVO;
import com.campus.backend.dto.PostVO;
import com.campus.backend.dto.PostQueryDTO;
import com.campus.backend.dto.ProductVO;
import com.campus.backend.dto.ProductQueryDTO;
import com.campus.backend.entity.User;
import com.campus.backend.entity.Post;
import com.campus.backend.entity.Product;
import com.campus.backend.mapper.PostMapper;
import com.campus.backend.mapper.ProductMapper;
import com.campus.backend.mapper.UserBehaviorMapper;
import com.campus.backend.mapper.UserLikeMapper;
import com.campus.backend.mapper.UserMapper;
import com.campus.backend.service.PostService;
import com.campus.backend.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final UserBehaviorMapper userBehaviorMapper;
    private final PostMapper postMapper;
    private final ProductMapper productMapper;
    private final UserMapper userMapper;
    private final UserLikeMapper userLikeMapper;
    private final PostService postService;
    private final ProductService productService;

    public List<FeedItemVO> recommend(Long userId, int limit) {
        Set<String> interestTags = new LinkedHashSet<>();

        // 批量获取最近浏览帖子的标签
        List<Long> recentPostIds = userBehaviorMapper.selectRecentTargetIds(userId, "POST", 10);
        if (!recentPostIds.isEmpty()) {
            List<Post> recentPosts = postMapper.selectByIds(recentPostIds);
            for (Post post : recentPosts) {
                if (post.getTags() != null) {
                    for (String tag : post.getTags().split(",")) {
                        String t = tag.trim();
                        if (!t.isEmpty()) interestTags.add(t);
                    }
                }
            }
        }

        // 批量获取最近浏览商品的标签
        List<Long> recentProductIds = userBehaviorMapper.selectRecentTargetIds(userId, "PRODUCT", 10);
        if (!recentProductIds.isEmpty()) {
            List<Product> recentProducts = productMapper.selectByIds(recentProductIds);
            for (Product product : recentProducts) {
                if (product.getTags() != null) {
                    for (String tag : product.getTags().split(",")) {
                        String t = tag.trim();
                        if (!t.isEmpty()) interestTags.add(t);
                    }
                }
            }
        }

        if (interestTags.isEmpty()) {
            return getHotItems(userId, limit);
        }

        List<ScoredItem> scoredItems = new ArrayList<>();

        PostQueryDTO postQuery = new PostQueryDTO();
        postQuery.setPage(1);
        postQuery.setSize(50);
        List<PostVO> allPosts = postService.getPostList(postQuery, userId);
        for (PostVO post : allPosts) {
            if (recentPostIds.contains(post.getId())) continue;
            int score = matchScore(post.getTags(), interestTags);
            if (Boolean.TRUE.equals(post.getIsAd()) && post.getExposureBoost() != null && post.getExposureBoost() > 1) {
                score = score * post.getExposureBoost();
                if (score == 0) score = post.getExposureBoost();
            }
            if (score > 0) {
                scoredItems.add(new ScoredItem("POST", post.getId(), score));
            }
        }

        ProductQueryDTO productQuery = new ProductQueryDTO();
        productQuery.setPage(1);
        productQuery.setSize(50);
        productQuery.setStatus(1);
        List<ProductVO> allProducts = productService.getProductList(productQuery);
        for (ProductVO product : allProducts) {
            if (recentProductIds.contains(product.getId())) continue;
            int score = matchScore(product.getTags(), interestTags);
            if (score > 0) {
                scoredItems.add(new ScoredItem("PRODUCT", product.getId(), score));
            }
        }

        scoredItems.sort((a, b) -> Integer.compare(b.score, a.score));

        // 收集需要查询的ID
        List<Long> topPostIds = new ArrayList<>();
        List<Long> topProductIds = new ArrayList<>();
        for (ScoredItem si : scoredItems) {
            if (topPostIds.size() + topProductIds.size() >= limit) break;
            if ("POST".equals(si.itemType)) {
                topPostIds.add(si.id);
            } else {
                topProductIds.add(si.id);
            }
        }

        // 批量查询帖子实体
        Map<Long, Post> postMap = new HashMap<>();
        if (!topPostIds.isEmpty()) {
            for (Post p : postMapper.selectByIds(topPostIds)) {
                postMap.put(p.getId(), p);
            }
        }

        // 批量查询商品实体
        Map<Long, Product> productMap = new HashMap<>();
        if (!topProductIds.isEmpty()) {
            for (Product p : productMapper.selectByIds(topProductIds)) {
                if (p.getStatus() != null && p.getStatus() == 1) {
                    productMap.put(p.getId(), p);
                }
            }
        }

        // 收集所有需要的用户ID，批量查询
        Set<Long> userIds = new HashSet<>();
        for (Post p : postMap.values()) userIds.add(p.getUserId());
        for (Product p : productMap.values()) userIds.add(p.getSellerId());

        Map<Long, User> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            for (User u : userMapper.selectBasicInfoByIds(new ArrayList<>(userIds))) {
                userMap.put(u.getId(), u);
            }
        }

        // 构建 FeedItemVO
        List<FeedItemVO> result = new ArrayList<>();
        for (ScoredItem si : scoredItems) {
            if (result.size() >= limit) break;
            FeedItemVO vo = null;
            if ("POST".equals(si.itemType)) {
                Post postEntity = postMap.get(si.id);
                if (postEntity == null) continue;
                vo = new FeedItemVO();
                vo.setItemType("POST");
                vo.setId(postEntity.getId());
                vo.setTitle(postEntity.getTitle());
                vo.setContent(postEntity.getContent());
                vo.setLikeCount(postEntity.getLikeCount());
                vo.setCommentCount(postEntity.getCommentCount());
                vo.setViewCount(postEntity.getViewCount());
                vo.setUserId(postEntity.getUserId());
                vo.setPostType(postEntity.getPostType());
                vo.setCreatedAt(postEntity.getCreatedAt());
                vo.setLocation(postEntity.getLocation());
                vo.setIsAd(postEntity.getIsAd());
                vo.setExposureBoost(postEntity.getExposureBoost());
                vo.setTags(postEntity.getTags());
                User u = userMap.get(postEntity.getUserId());
                if (u != null) {
                    vo.setUserName(u.getNickname() != null ? u.getNickname() : u.getUsername());
                    vo.setUserAvatar(u.getAvatar());
                }
                vo.setIsLiked(userLikeMapper.exists(userId, "POST", postEntity.getId()));
            } else {
                Product productEntity = productMap.get(si.id);
                if (productEntity == null) continue;
                vo = new FeedItemVO();
                vo.setItemType("PRODUCT");
                vo.setId(productEntity.getId());
                vo.setTitle(productEntity.getName());
                vo.setContent(productEntity.getDescription());
                vo.setCoverImage(productEntity.getCoverImage());
                vo.setPrice(productEntity.getPrice() != null ? productEntity.getPrice().toString() : null);
                vo.setOriginalPrice(productEntity.getOriginalPrice() != null ? productEntity.getOriginalPrice().toString() : null);
                vo.setLikeCount(productEntity.getLikeCount());
                vo.setViewCount(productEntity.getViewCount());
                vo.setUserId(productEntity.getSellerId());
                vo.setLocation(productEntity.getLocation());
                vo.setCreatedAt(productEntity.getCreatedAt());
                vo.setTags(productEntity.getTags());
                User u = userMap.get(productEntity.getSellerId());
                if (u != null) {
                    vo.setUserName(u.getNickname() != null ? u.getNickname() : u.getUsername());
                    vo.setUserAvatar(u.getAvatar());
                }
                vo.setIsLiked(userLikeMapper.exists(userId, "PRODUCT", productEntity.getId()));
            }
            if (vo != null) result.add(vo);
        }

        log.info("推荐: userId={}, interestTags={}, results={}", userId, interestTags, result.size());
        return result;
    }

    private int matchScore(String itemTags, Set<String> interestTags) {
        if (itemTags == null || itemTags.isEmpty()) return 0;
        int score = 0;
        for (String tag : itemTags.split(",")) {
            if (interestTags.contains(tag.trim())) score++;
        }
        return score;
    }

    /** 新用户无浏览历史时，返回按点赞数排序的热门内容 */
    private List<FeedItemVO> getHotItems(Long userId, int limit) {
        List<FeedItemVO> result = new ArrayList<>();

        PostQueryDTO postQuery = new PostQueryDTO();
        postQuery.setPage(1);
        postQuery.setSize(limit);
        postQuery.setSortBy("hot");
        List<PostVO> hotPosts = postService.getPostList(postQuery, userId);
        for (PostVO post : hotPosts) {
            FeedItemVO vo = new FeedItemVO();
            vo.setItemType("POST");
            vo.setId(post.getId());
            vo.setTitle(post.getTitle());
            vo.setContent(post.getContent());
            vo.setLikeCount(post.getLikeCount());
            vo.setCommentCount(post.getCommentCount());
            vo.setViewCount(post.getViewCount());
            vo.setIsLiked(post.getIsLiked());
            vo.setUserId(post.getUserId());
            vo.setUserName(post.getUserName());
            vo.setUserAvatar(post.getUserAvatar());
            vo.setPostType(post.getPostType());
            vo.setCreatedAt(post.getCreatedAt());
            vo.setLocation(post.getLocation());
            vo.setIsAd(post.getIsAd());
            vo.setExposureBoost(post.getExposureBoost());
            vo.setTags(post.getTags());
            result.add(vo);
        }

        if (result.size() < limit) {
            ProductQueryDTO productQuery = new ProductQueryDTO();
            productQuery.setPage(1);
            productQuery.setSize(limit - result.size());
            productQuery.setStatus(1);
            productQuery.setSortBy("view_count");
            List<ProductVO> hotProducts = productService.getProductList(productQuery);
            for (ProductVO product : hotProducts) {
                FeedItemVO vo = new FeedItemVO();
                vo.setItemType("PRODUCT");
                vo.setId(product.getId());
                vo.setTitle(product.getName());
                vo.setContent(product.getDescription());
                vo.setCoverImage(product.getCoverImage());
                vo.setPrice(product.getPrice() != null ? product.getPrice().toString() : null);
                vo.setOriginalPrice(product.getOriginalPrice() != null ? product.getOriginalPrice().toString() : null);
                vo.setLikeCount(product.getLikeCount());
                vo.setViewCount(product.getViewCount());
                vo.setUserId(product.getSellerId());
                vo.setUserName(product.getSellerName());
                vo.setUserAvatar(product.getSellerAvatar());
                vo.setCategoryId(product.getCategoryId());
                vo.setCategoryName(product.getCategoryName());
                vo.setLocation(product.getLocation());
                vo.setCreatedAt(product.getCreatedAt());
                vo.setTags(product.getTags());
                result.add(vo);
            }
        }

        return result.stream().limit(limit).collect(Collectors.toList());
    }

    private static class ScoredItem {
        String itemType;
        Long id;
        int score;

        ScoredItem(String itemType, Long id, int score) {
            this.itemType = itemType;
            this.id = id;
            this.score = score;
        }
    }
}
