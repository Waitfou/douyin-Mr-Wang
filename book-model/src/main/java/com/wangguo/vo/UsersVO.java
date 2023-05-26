package com.wangguo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;


/**
 * POJO是和数据库表直接对应的，因此不能在里面加Token，因此可以创建此类达到目的
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UsersVO {
    private String id;
    private String mobile;
    private String nickname;
    private String idouyinNum;
    private String face;
    private Integer sex;
    private Date birthday;
    private String country;
    private String province;
    private String city;
    private String district;
    private String description;
    private String bgImg;
    private Integer canIdouyinNumBeUpdated;
    private Date createdTime;
    private Date updatedTime;

    private String userToken;       // 用户token，传递给前端

    private Integer myFollowsCounts; // 我关注的数量
    private Integer myFansCounts; // 我的粉丝的数量
    //    private Integer myLikedVlogCounts; // 所有我点赞的视频的个数总和
    private Integer totalLikeMeCounts; // 所有点赞我的视频的总和
}
