package com.wangguo.controller;

import com.wangguo.base.BaseInfoProperties;
import com.wangguo.bo.VlogBO;
import com.wangguo.enums.YesOrNo;
import com.wangguo.grace.result.GraceJSONResult;
import com.wangguo.service.VlogService;
import com.wangguo.utils.PagedGridResult;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Api(tags = "VlogController 短视频相关业务功能的接口")
@RequestMapping("vlog")
@RestController
@RefreshScope
public class VlogController extends BaseInfoProperties {
    @Autowired
    private VlogService vlogService;

    /**
     * 发布视频
     * @param vlogBO vlog业务对象
     * @return
     */
    // 可以在参数部分添加一些校验VlogBO的操作
    @PostMapping("publish")
    public GraceJSONResult publish(@RequestBody VlogBO vlogBO) { // 前端传送过来的data，和VlogBO中的内容一一对应
        // 根据前端传递的BO实体信息创建Vlog
        vlogService.createVlog(vlogBO);
        return GraceJSONResult.ok();
    }

    /**
     * 展示视频
     * 通过不同的参数组合可以实现查询首页展示视频（userId）/搜索视频（search）/用户浏览别人的视频（userId，主要用于用户是否点赞该视频）等
     * @param userId
     * @param search
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("indexList")
    public GraceJSONResult indexList(@RequestParam(defaultValue = "") String userId,
                                     @RequestParam(defaultValue = "") String search,
                                     @RequestParam Integer page,
                                     @RequestParam Integer pageSize) {
        if (page == null) {
            page = COMMON_START_PAGE;
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }
        PagedGridResult gridResult = vlogService.getIndexVlogList(userId, search, page, pageSize);

        return GraceJSONResult.ok(gridResult);
    }

    /**
     * 获取视频详情，用于点开视频时候的视频页展示
     * @param userId
     * @param vlogId
     * @return
     */
    @GetMapping("detail")
    public GraceJSONResult detail(@RequestParam(defaultValue = "") String userId,
                                  @RequestParam String vlogId) {
        return GraceJSONResult.ok(vlogService.getVlogDetailById(userId, vlogId));
    }

    /**
     * 只有作者本人能够把自己的视频设置为私密
     * @param userId 用户id
     * @param vlogId 视频id
     * @return
     */
    @PostMapping("changeToPrivate")
    public GraceJSONResult changeToPrivate(@RequestParam String userId,
                                           @RequestParam String vlogId) {
        vlogService.changeToPrivateOrPublic(userId,
                vlogId,
                YesOrNo.YES.type); // yes表示设置为私密
        return GraceJSONResult.ok();
    }
    @PostMapping("changeToPublic")
    public GraceJSONResult changeToPublic(@RequestParam String userId,
                                          @RequestParam String vlogId) {
        vlogService.changeToPrivateOrPublic(userId,
                vlogId,
                YesOrNo.NO.type); // No表示不设置为私密视频
        return GraceJSONResult.ok(); //因为是Post请求不用向前端返回数据
    }

    @GetMapping("myPublicList")
    public GraceJSONResult myPublicList(@RequestParam String userId,
                                        @RequestParam Integer page,
                                        @RequestParam Integer pageSize){
        if (page == null) {
            page = COMMON_START_PAGE;
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }
        PagedGridResult gridResult = vlogService.queryMyVlogList(userId, page, pageSize, YesOrNo.NO.type);
        return GraceJSONResult.ok(gridResult);
    }

    @GetMapping("myPrivateList")
    public GraceJSONResult myPrivateList(@RequestParam String userId,
                                         @RequestParam Integer page,
                                         @RequestParam Integer pageSize) {
        if (page == null) {
            page = COMMON_START_PAGE;
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }
        // 查询私密视频
        PagedGridResult gridResult = vlogService.queryMyVlogList(userId, page, pageSize, YesOrNo.YES.type);
        return GraceJSONResult.ok(gridResult);
    }

    @Value("${nacos.counts}") // 在nacos的配置类中配置
    private Integer nacosCounts;
    @PostMapping("like")
    public GraceJSONResult like(@RequestParam String userId,
                                @RequestParam String vlogerId,
                                @RequestParam String vlogId) {
        // 把我点赞的视频的相关点赞信息保存到数据库
        vlogService.userLikeVlog(userId, vlogId);
        // 用户喜欢了一个视频之后，那么该视频的赞就要+1，该视频的发布者的赞也要加1
        redis.increment(REDIS_VLOGER_BE_LIKED_COUNTS + ":" + vlogerId, 1);
        redis.increment(REDIS_VLOG_BE_LIKED_COUNTS + ":" + vlogId, 1);

        // 把点赞的信息存入redis方便使用
        redis.set(REDIS_USER_LIKE_VLOG + ":" + userId + ":" + vlogId, "1");


        // 点赞之后，要判断是否将我点赞的个数刷新到数据库（通过动态配置阈值）
        String countsStr = redis.get(REDIS_VLOG_BE_LIKED_COUNTS + ":" + vlogId);
        log.info("======" + REDIS_VLOG_BE_LIKED_COUNTS + ":" + vlogId + "======");
        Integer counts = 0;
        if (StringUtils.isNotBlank(countsStr)) {
            counts = Integer.valueOf(countsStr);
            if (counts >= nacosCounts) {   // todo 以后每一次超过阈值的都要存入数据库，怎么优化？？
                vlogService.flushCounts(vlogId, counts);
            }
        }
        return GraceJSONResult.ok();
    }

    @PostMapping("unlike")
    public GraceJSONResult unlike(@RequestParam String userId,
                                  @RequestParam String vlogerId,
                                  @RequestParam String vlogId) {
        vlogService.userUnLikeVlog(userId, vlogId);
        redis.decrement(REDIS_VLOGER_BE_LIKED_COUNTS + ":" + vlogerId, 1);
        redis.decrement(REDIS_VLOG_BE_LIKED_COUNTS + ":" + vlogId, 1);
        redis.del(REDIS_USER_LIKE_VLOG + ":" + userId + ":" + vlogId);

        return GraceJSONResult.ok();
    }

    /**
     * 用于前端及时更新视频页的点赞总数（点赞之后，前端自动刷新调用的接口）
     * @param vlogId
     * @return
     */
    @PostMapping("totalLikedCounts")
    public GraceJSONResult totalLikedCounts(@RequestParam String vlogId) {
        System.out.println("test11111");
        return GraceJSONResult.ok(vlogService.getVlogBeLikedCounts(vlogId));
    }

    /**
     * 展示个人主页点赞过的视频
     * @param userId
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("myLikedList")
    public GraceJSONResult myLikedList(@RequestParam String userId,
                                       @RequestParam Integer page,
                                       @RequestParam Integer pageSize) {
        if (page == null) {
            page = COMMON_START_PAGE;
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }
        PagedGridResult gridResult = vlogService.getMyLikedVlogList(userId, page, pageSize);
        return GraceJSONResult.ok(gridResult);
    }

    /**
     *展示首页中head上面点关注之后展示的视频信息
     */
    @GetMapping("followList")
    public GraceJSONResult followList(@RequestParam String myId,
                                      @RequestParam Integer page,
                                      @RequestParam Integer pageSize) {
        if (page == null) {
            page = COMMON_START_PAGE;
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }
        System.out.println("testtesttest");
        PagedGridResult gridResult = vlogService.getMyFollowVlogList(myId, page, pageSize);
        return GraceJSONResult.ok(gridResult);
    }

    /**
     * 在朋友标签下展示的视频
     * @param myId 我的id
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("friendList")
    public GraceJSONResult friendList(@RequestParam String myId,
                                      @RequestParam Integer page,
                                      @RequestParam Integer pageSize) {
        if (page == null) {
            page = COMMON_START_PAGE;
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }
        PagedGridResult gridResult = vlogService.getMyFriendVlogList(myId, page, pageSize);
        return GraceJSONResult.ok(gridResult);
    }
}
