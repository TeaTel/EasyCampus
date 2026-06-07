package com.campus.backend.service.impl;

import com.campus.backend.dto.AdCreateDTO;
import com.campus.backend.dto.AdPackageVO;
import com.campus.backend.dto.PostVO;
import com.campus.backend.entity.Post;
import com.campus.backend.exception.NotFoundException;
import com.campus.backend.mapper.PostMapper;
import com.campus.backend.service.AdService;
import com.campus.backend.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdServiceImpl implements AdService {

    private final PostMapper postMapper;
    private final PostService postService;

    private static final List<AdPackageVO> AD_PACKAGES = new ArrayList<>();

    static {
        AdPackageVO trial = new AdPackageVO();
        trial.setId("trial");
        trial.setName("体验推流");
        trial.setDescription("首次推流体验，覆盖本校区活跃用户，适合试水推广");
        trial.setPrice(new BigDecimal("1.90"));
        trial.setExposureBoost(2);
        trial.setDurationDays(1);
        trial.setBadge("体验");
        trial.setEstimatedReach("约 200-500 次曝光");
        trial.setScenario("适合首次使用推流的用户，推广闲置物品、二手教材等");
        trial.setTerms("每人限购1次；曝光量受内容质量和发布时段影响；不支持退款");
        AD_PACKAGES.add(trial);

        AdPackageVO basic = new AdPackageVO();
        basic.setId("basic");
        basic.setName("基础推流");
        basic.setDescription("覆盖本校区及关联校区，持续3天稳定曝光");
        basic.setPrice(new BigDecimal("4.90"));
        basic.setExposureBoost(3);
        basic.setDurationDays(3);
        basic.setBadge("推荐");
        basic.setEstimatedReach("约 800-2000 次曝光");
        basic.setScenario("适合推广二手数码、社团活动、兼职招聘等个人发布");
        basic.setTerms("同一内容不可重复购买同套餐；曝光量为预估值，实际受内容质量影响；到期后自动停止推流");
        AD_PACKAGES.add(basic);

        AdPackageVO standard = new AdPackageVO();
        standard.setId("standard");
        standard.setName("热门推流");
        standard.setDescription("全平台推荐位优先展示，覆盖多个校区，持续7天");
        standard.setPrice(new BigDecimal("14.90"));
        standard.setExposureBoost(5);
        standard.setDurationDays(7);
        standard.setBadge("热门");
        standard.setEstimatedReach("约 3000-8000 次曝光");
        standard.setScenario("适合推广高价值商品、考研资料、校园周边服务等");
        standard.setTerms("可叠加使用（最多2次）；享有首页推荐位优先排序；到期后自动停止推流；内容违规将终止推流且不退款");
        AD_PACKAGES.add(standard);

        AdPackageVO premium = new AdPackageVO();
        premium.setId("premium");
        premium.setName("校园爆款");
        premium.setDescription("全平台置顶推荐，覆盖所有校区，持续14天长效曝光");
        premium.setPrice(new BigDecimal("29.90"));
        premium.setExposureBoost(10);
        premium.setDurationDays(14);
        premium.setBadge("爆款");
        premium.setEstimatedReach("约 10000-30000 次曝光");
        premium.setScenario("适合社团招新、商家入驻推广、大型活动宣传、毕业季清仓等");
        premium.setTerms("可叠加使用（最多2次）；享有首页置顶推荐位；专属客服支持；到期后自动停止推流；内容违规将终止推流且不退款");
        AD_PACKAGES.add(premium);
    }

    @Override
    @Transactional
    public PostVO createAd(AdCreateDTO dto, Long userId) {
        Post post = new Post();
        post.setUserId(userId);
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setPostType(dto.getPostType() != null ? dto.getPostType() : "SHOWCASE");
        post.setTags(dto.getTags());
        post.setIsAd(true);

        AdPackageVO pkg = getAdPackage(dto.getPackageId());
        if (pkg != null) {
            post.setExposureBoost(pkg.getExposureBoost());
        } else {
            post.setExposureBoost(1);
        }

        postMapper.insert(post);
        log.info("发布广告: id={}, userId={}, packageId={}, exposureBoost={}",
                post.getId(), userId, dto.getPackageId(), post.getExposureBoost());
        return postService.getPostDetail(post.getId(), userId);
    }

    @Override
    public List<AdPackageVO> getAdPackages() {
        return AD_PACKAGES;
    }

    @Override
    public AdPackageVO getAdPackage(String packageId) {
        if (packageId == null) return null;
        return AD_PACKAGES.stream()
                .filter(p -> p.getId().equals(packageId))
                .findFirst()
                .orElse(null);
    }

    @Override
    @Transactional
    public boolean simulatePayment(Long userId, Long postId, String packageId) {
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new NotFoundException("Post", postId);
        }
        if (!post.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此广告");
        }

        AdPackageVO pkg = getAdPackage(packageId);
        if (pkg == null) {
            throw new RuntimeException("无效的推流套餐");
        }

        post.setExposureBoost(pkg.getExposureBoost());
        postMapper.update(post);

        log.info("模拟付费推流: postId={}, userId={}, packageId={}, exposureBoost={}",
                postId, userId, packageId, pkg.getExposureBoost());
        return true;
    }
}
