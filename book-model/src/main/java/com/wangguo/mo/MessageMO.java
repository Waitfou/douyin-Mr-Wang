package com.wangguo.mo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;
import java.util.Map;

/**
 * 消息实体
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Document("message") // 主要包括:关注消息/回复消息/点赞消息等
public class MessageMO {   // MO就是mongodbObject的简写
    @Id // 此注解主要用于非关系型数据中
    private String id; //消息主键id
    @Field("fromUserId") //代表MongoDB的一个字段 （field类似于mysql中的列，但是很灵活）
    private String fromUserId; // 消息来自的用户id
    @Field("fromNickName")
    private String fromNickName; // 消息来自的用户昵称
    @Field("fromFace")
    private String fromFace;
    @Field("toUserId")
    private String toUserId; // 消息发送到某对象的用户id
    @Field("msgType")
    private Integer msgType; // 消息类型 枚举
    @Field("msgContent")
    private Map msgContent;// 消息内容
    @Field("createTime")
    private Date createTime; // 消息创建时间
}
