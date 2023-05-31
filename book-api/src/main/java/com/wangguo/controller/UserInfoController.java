package com.wangguo.controller;

import com.wangguo.MinIOConfig;
import com.wangguo.base.BaseInfoProperties;
import com.wangguo.bo.UpdatedUserBO;
import com.wangguo.enums.FileTypeEnum;
import com.wangguo.enums.UserInfoModifyType;
import com.wangguo.grace.result.GraceJSONResult;
import com.wangguo.grace.result.ResponseStatusEnum;
import com.wangguo.pojo.Users;
import com.wangguo.service.UserService;
import com.wangguo.utils.MinIOUtils;
import com.wangguo.vo.UsersVO;
import io.netty.util.internal.StringUtil;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Api(tags = "userInfoController 用户信息接口模块")
@RequestMapping("userInfo")
@RestController
public class UserInfoController extends BaseInfoProperties {
    @Autowired
    private UserService userService;

    /**
     * @param userId
     * @return
     * @throws Exception
     */
    // userId要和前端一致，因为是接收前端传入的内容
    @GetMapping("query")
    public GraceJSONResult query(@RequestParam String userId) throws Exception {
        // 根据用户信息id查询到用户信息
        Users user = userService.getUser(userId);
        UsersVO usersVO = new UsersVO();
        BeanUtils.copyProperties(user, usersVO);

        // 我的关注博主总数量（从redis中获取更快）
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
     *
     * @param updatedUserBO 前端传来的用户修改信息
     * @param type          修改的类型
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

    @Autowired
    private MinIOConfig minIOConfig;

    @PostMapping("modifyImage")
    public GraceJSONResult modifyImage(@RequestParam String userId,
                                       @RequestParam Integer type,
                                       MultipartFile file) throws Exception {
        // 文件类型不对（如果既不是1也不是2的话），那么就直接返回上传失败
        if (type != FileTypeEnum.BGIMG.type && type != FileTypeEnum.FACE.type) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.FILE_UPLOAD_FAILD);
        }
        // 获取源文件名
        String fileName = file.getOriginalFilename();
        MinIOUtils.uploadFile(minIOConfig.getBucketName(),
                fileName,
                file.getInputStream());
        // 相当于组合成一个链接url
        String imgUrl = minIOConfig.getFileHost()
                + "/"
                + minIOConfig.getBucketName()
                + "/"
                + fileName;
        // 修改图片地址到数据库
        UpdatedUserBO updatedUserBO = new UpdatedUserBO();
        updatedUserBO.setId(userId);

        if (type == FileTypeEnum.BGIMG.type) {
            updatedUserBO.setBgImg(imgUrl);
        } else {
            updatedUserBO.setFace(imgUrl);
        }
        // 正式更新用户头像和背景图片的地址。
        Users users = userService.updateUsersInfo(updatedUserBO);
        return GraceJSONResult.ok(users); // 把更新之后的用户信息返回给前端便于前端进行更新
    }
}
