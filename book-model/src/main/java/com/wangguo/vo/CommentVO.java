package com.wangguo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CommentVO { // 返回给前端的内容
    private String id;
    private String commentId; // 评论的id
    private String vlogerId;  // 该视频发布者的id
    private String fatherCommentId;
    private String vlogId; // 该视频的id
    private String commentUserId; // 该评论是谁评论的
    private String commentUserNickname; // 评论者的昵称
    private String commentUserFace; // 评论者的头像
    private String content; // 评论的内容
    private Integer likeCounts; // 该评论的点赞个数
    private String replyedUserNickname; // 对评论进行回复的人的昵称
    private Date createTime; // 评论的创建时间
    private Integer isLike = 0; // 我是否喜欢该视频
}
