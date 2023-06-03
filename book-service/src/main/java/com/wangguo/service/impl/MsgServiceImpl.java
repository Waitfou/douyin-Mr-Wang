package com.wangguo.service.impl;

import com.wangguo.base.BaseInfoProperties;
import com.wangguo.enums.MessageEnum;
import com.wangguo.mo.MessageMO;
import com.wangguo.pojo.Users;
import com.wangguo.repository.MessageRepository;
import com.wangguo.service.MsgService;
import com.wangguo.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MsgServiceImpl extends BaseInfoProperties implements MsgService {
    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserService userService;
    public void createMsg(String fromUserId,
                          String toUserId,
                          Integer type,
                          Map msgContent) {
        // 获取发送消息的用户信息
        Users fromUser = userService.getUser(fromUserId);
        // 创建消息实体，存放信息
        MessageMO messageMO = new MessageMO();
        messageMO.setFromNickName(fromUserId);
        messageMO.setFromNickName(fromUser.getNickname());
        messageMO.setFromFace(fromUser.getFace());
        messageMO.setToUserId(toUserId);
        messageMO.setMsgType(type);
        if (msgContent != null) {
            messageMO.setMsgContent(msgContent);
        }
        messageMO.setCreateTime(new Date());
        messageRepository.save(messageMO);
    }

    /**
     * 根据用户ID查询用户相关的消息
     * @param toUserId 消息的接收者的id，查询我的消息列表的时候就是我的id
     * @param page
     * @param pageSize
     * @return
     */
    @Override
    public List<MessageMO> queryList(String toUserId, Integer page, Integer pageSize) {
        // Pageable是Spring data库中定义的一个接口
        Pageable pageable = PageRequest.of(page,
                pageSize,
                Sort.Direction.DESC,
                "createTime");
        List<MessageMO> list = messageRepository.findAllByToUserIdEqualsOrderByCreateTimeDesc(toUserId, pageable);

        for (MessageMO msg : list) {
            // 如果是关注消息，则需要查询我之前有没有关注过他，用于在前端标记“回粉”/“互关”
            if (msg.getMsgType() != null && msg.getMsgType() == MessageEnum.FOLLOW_YOU.type) {
                Map map = msg.getMsgContent();
                if (map == null) {
                    map = new HashMap();
                }
                String relationship = redis.get(REDIS_FANS_AND_VLOGGER_RELATIONSHIP + ":" + msg.getToUserId() + ":" + msg.getFromUserId());
                if (StringUtils.isNotBlank(relationship) && relationship.equalsIgnoreCase("1")) {
                    map.put("isFriend", true);
                } else {
                    map.put("isFriend", false);
                }
                msg.setMsgContent(map);
            }
        }
        return list;
    }
}
