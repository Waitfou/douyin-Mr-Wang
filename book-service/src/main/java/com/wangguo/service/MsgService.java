package com.wangguo.service;

import com.wangguo.mo.MessageMO;

import java.util.List;
import java.util.Map;

public interface MsgService {
    /**
     * 创建消息
     * @param fromUserId 消息来源于
     * @param toUserID 消息发送给
     * @param type 消息类型
     * @param msgContent 消息内容
     */
    public void createMsg(String fromUserId,
                          String toUserID,
                          Integer type,
                          Map msgContent);
    /**
     * 查询消息列表
     */
    public List<MessageMO> queryList(String toUserId,
                                     Integer page,
                                     Integer pageSize);
}
