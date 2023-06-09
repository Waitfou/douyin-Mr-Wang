package com.wangguo.controller;

import com.wangguo.base.BaseInfoProperties;
import com.wangguo.grace.result.GraceJSONResult;
import com.wangguo.grace.result.ResponseStatusEnum;
import com.wangguo.pojo.Users;
import com.wangguo.service.FansService;
import com.wangguo.service.UserService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 粉丝相关的业务
 */
@Slf4j
@Api(tags = "粉丝相关业务功能的接口")
@RequestMapping("fans")
@RestController // 表示这是控制层的
public class FansController extends BaseInfoProperties {
    @Autowired
    private UserService userService;

    @Autowired
    private FansService fansService;
    /**
     * 只需要我的id和我要关注的人的id即可
     * @param myId 我的id
     * @param vlogerId 我要关注的人的id
     * @return
     */
    @PostMapping("follow")
    public GraceJSONResult follow(@RequestParam String myId,
                                  @RequestParam String vlogerId) {
        if (StringUtils.isBlank(myId) || StringUtils.isBlank(vlogerId)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_ERROR);
        }

        // 自己不能关注自己
        if (myId.equalsIgnoreCase(vlogerId)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_RESPONSE_NO_INFO);
        }

        Users vloger = userService.getUser(vlogerId);
        Users myInfo = userService.getUser(myId);

        // 我或者我要关注的人的信息不能为空
        if (myInfo == null || vloger == null) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_RESPONSE_NO_INFO);
        }

        // 保存粉丝关系到数据库
        fansService.doFollow(myId, vlogerId);

        // 我的关注+1, increment是实现自增
        redis.increment(REDIS_MY_FOLLOWS_COUNTS + ":" + myId, 1);
        // 博主的粉丝数+1
        redis.increment(REDIS_MY_FANS_COUNTS + ":" + vlogerId, 1);

        // 同步在redis中保存我和博主的关联关系
        redis.set(REDIS_FANS_AND_VLOGGER_RELATIONSHIP + ":" + myId + ":" + vlogerId, "1");
        return GraceJSONResult.ok();
    }
    /**
     * 取消关注
     */
    @PostMapping("cancel")
    public GraceJSONResult cancel(@RequestParam String myId,
                                  @RequestParam String vlogerId) {
        // 先在数据库中对相关结果进行修改
        fansService.doCancel(myId, vlogerId);
        // 然后把redis中的相关数据进行修改，delta表示步长, decrement表示累减
        redis.decrement(REDIS_MY_FOLLOWS_COUNTS + ":" + myId, 1);
        // 把被取消关注的人的粉丝数减1
        redis.decrement(REDIS_MY_FANS_COUNTS + ":" + vlogerId, 1);

        // 删除我和博主之间的朋友关系
        redis.del(REDIS_FANS_AND_VLOGGER_RELATIONSHIP + ":" + myId + ":" + vlogerId);
        return GraceJSONResult.ok();
    }

    /**
     * 用于新打开一个vlog博主的主页时，查看我是否关注了他
     * @param myId
     * @param vlogerId
     * @return
     */
    @GetMapping("queryDoIFollowVloger")
    public GraceJSONResult queryDoIFollowVloger(@RequestParam String myId,
                                                @RequestParam String vlogerId) {
        return GraceJSONResult.ok(fansService.queryDoIFollowVloger(myId, vlogerId));
    }

    /**
     * 查询我的关注
     * @param myId
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("queryMyFollows")
    public GraceJSONResult queryMyFollows(@RequestParam String myId,
                                       @RequestParam Integer page,
                                       @RequestParam Integer pageSize) {
        return GraceJSONResult.ok(fansService.queryMyFollows(
                myId,
                page,
                pageSize));
    }
    @GetMapping("queryMyFans")
    public GraceJSONResult queryMyFans(@RequestParam String myId,
                                       @RequestParam Integer page,
                                       @RequestParam Integer pageSize) {
        return GraceJSONResult.ok(fansService.queryMyFans(
                myId,
                page,
                pageSize));
    }
}
