package com.wangguo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 用于展示我已经关注人的视图对象
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class VlogerVO {
    // 主要展示如下4项信息
    private String vlogerId;
    private String nickname;
    private String face;
    private boolean isFollowed = true;
}
