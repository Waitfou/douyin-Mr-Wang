package com.wangguo.service.impl;

import com.wangguo.base.BaseInfoProperties;
import com.wangguo.mo.MessageMO;
import com.wangguo.repository.MessageRepository;
import com.wangguo.service.MsgService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class MsgServiceImpl extends BaseInfoProperties implements MsgService {
    @Autowired
    private MessageRepository messageRepository;

    public void createMsg(String fromUserId,
                          String toUserId,
                          Integer type,
                          Map msgContent) {

    }

    @Override
    public List<MessageMO> queryList(String toUserId, Integer page, Integer pageSize) {
        return null;
    }
}
