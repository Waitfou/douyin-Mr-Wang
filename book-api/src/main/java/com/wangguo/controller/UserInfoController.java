package com.wangguo.controller;

import com.wangguo.base.BaseInfoProperties;
import com.wangguo.bo.UpdatedUserBO;
import com.wangguo.enums.UserInfoModifyType;
import com.wangguo.grace.result.GraceJSONResult;
import com.wangguo.pojo.Users;
import com.wangguo.service.UserService;
import com.wangguo.vo.UsersVO;
import io.netty.util.internal.StringUtil;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Api(tags = "userInfoController 用户信息接口模块")
@RequestMapping("userInfo")
@RestController
public class UserInfoController extends BaseInfoProperties {
    @Autowired
    private UserService userService;

    /**
     *
     * @param userId
     * @return
     * @throws Exception
     */
    // userId要和前端一致，因为是接收前端传入的内容
    @GetMapping("query")
    public GraceJSONResult query(@RequestParam String userId) throws Exception{
        // 根据用户信息id查询到用户信息
        Users user = userService.getUser(userId);
        UsersVO usersVO = new UsersVO();
        BeanUtils.copyProperties(user, usersVO);

        // 我的关注博主总数量
        String myFollowCountsStr = redis.get(REDIS_MY_FOLLOWS_COUNTS + ":" + userId);
        // 我的粉丝总数
        String myFansCountsStr = redis.get(REDIS_MY_FANS_COUNTS + ":" + userId);

        //用户获赞总数，视频博主（点赞/喜欢）总和
//        String likedVlogerCountsStr = redis.get(REDIS_VLOG_BE_LIKED_COUNTS + ":" + userId);
        String likedVlogerCountsStr = redis.get(REDIS_VLOGER_BE_LIKED_COUNTS + userId);

        Integer myFollowsCounts = 0;
        Integer myFansCounts = 0;
        Integer likedVlogCounts = 0;
        Integer likedVlogerCounts = 0;
        Integer totalLikedMeCounts = 0;

        if (StringUtils.isNotBlank(myFollowCountsStr)) {
            myFollowsCounts = Integer.valueOf(myFollowCountsStr);
        }
        if (StringUtils.isNotBlank(myFansCountsStr)) {
            myFansCounts = Integer.valueOf(myFansCountsStr);
        }
        if (StringUtils.isNotBlank(likedVlogerCountsStr)) {
            likedVlogerCounts = Integer.valueOf(likedVlogerCountsStr);
        }
        totalLikedMeCounts = likedVlogCounts + likedVlogerCounts;
        usersVO.setMyFollowsCounts(myFollowsCounts);
        usersVO.setMyFansCounts(myFansCounts);
        usersVO.setTotalLikeMeCounts(totalLikedMeCounts);

        return GraceJSONResult.ok(usersVO);
    }

    /**
     * 修改用户信息
     * @param updatedUserBO 前端传来的用户修改信息
     * @param type 修改的类型
     * @return 返回修改之后的结果
     * @throws Exception
     */
    @PostMapping("modifyUserInfo")
    public GraceJSONResult modifyUserInfo(@RequestBody UpdatedUserBO updatedUserBO,
                                          @RequestParam Integer type)
        throws Exception {
        UserInfoModifyType.checkUserInfoTypeIsRight(type);
        Users newUserInfo = userService.updateUserInfo(updatedUserBO, type);

        // 把最新的信息返回给前端更新
        return GraceJSONResult.ok(newUserInfo);
    }
}
