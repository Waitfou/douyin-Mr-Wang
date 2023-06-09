package com.wangguo.service.impl;

import com.github.pagehelper.PageHelper;
import com.wangguo.base.BaseInfoProperties;
import com.wangguo.base.RabbitMQConfig;
import com.wangguo.enums.MessageEnum;
import com.wangguo.enums.YesOrNo;
import com.wangguo.mapper.FansMapper;
import com.wangguo.mapper.FansMapperCustom;
import com.wangguo.mo.MessageMO;
import com.wangguo.pojo.Fans;
import com.wangguo.service.FansService;
import com.wangguo.service.MsgService;
import com.wangguo.utils.JsonUtils;
import com.wangguo.utils.PagedGridResult;
import com.wangguo.vo.FansVO;
import com.wangguo.vo.VlogerVO;
import io.github.classgraph.json.JSONUtils;
import org.apache.commons.lang3.StringUtils;
import org.n3r.idworker.Sid;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FansServiceImpl extends BaseInfoProperties implements FansService {
    /**
     * 关注
     * @param myId 我的id
     * @param vlogerId 我要关注的人的id
     */

    @Autowired
    private Sid sid;

    @Autowired
    private FansMapper fansMapper;

    @Autowired
    private MsgService msgService;
    @Autowired
    private FansMapperCustom fansMapperCustom;

    // 简化同步 RabbitMQ 访问（发送和接收消息）的帮助程序类。
    @Autowired
    public RabbitTemplate rabbitTemplate;
    @Transactional // 关注的行为必须声明好事务，因为关注的时候可能只是关注了，但是没有把朋友关系更新，那么此时就要回退
    @Override
    public void doFollow(String myId, String vlogerId) {
        String fid = sid.nextShort(); // 获取全局ID的下一个。
        Fans fans = new Fans();
        fans.setId(fid);
        fans.setFanId(myId);
        fans.setVlogerId(vlogerId);

        // 如果对方关注我，那么我们要互相成为朋友关系
        Fans vloger = queryFansRelationship(vlogerId, myId);
        if (vloger != null) {
            // 如果对方也关注了我，那么就把我和他之间的关系更新为朋友关系
            fans.setIsFanFriendOfMine(YesOrNo.YES.type);
            vloger.setIsFanFriendOfMine(YesOrNo.YES.type);
            // 更改我要关注的博主和我之间的关系为朋友关系
            fansMapper.updateByPrimaryKeySelective(vloger);
        } else {
            fans.setIsFanFriendOfMine(YesOrNo.NO.type);
        }
        fansMapper.insert(fans);

        // 关注消息入库, 关注消息的消息实体为null
//        msgService.createMsg(myId, vlogerId, MessageEnum.FOLLOW_YOU.type, null);
        MessageMO messageMO = new MessageMO();
        messageMO.setFromUserId(myId);
        messageMO.setToUserId(vlogerId);

        // 优化：使用mq异步解耦，防止对非重要消息入库失败之后对已经保存的重要消息进行回滚
        // 这里是创建生产者发送消息
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_MSG, "sys.msg." + MessageEnum.FOLLOW_YOU.enValue, JsonUtils.objectToJson(messageMO));
    }

    public Fans queryFansRelationship(String fanId, String vlogerId) {
        Example example = new Example(Fans.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("vlogerId", vlogerId);
        criteria.andEqualTo("fanId", fanId);

        List list = fansMapper.selectByExample(example);
        Fans fan = null;
        if (list != null && list.size() > 0 && !list.isEmpty()) {
            fan = (Fans)list.get(0);
        }
        return fan;
    }

    /**
     * 取消关注
     * @param myId 我的id
     * @param vlogerId 我要取消关注的人的id
     */
    @Transactional
    @Override
    public void doCancel(String myId, String vlogerId) {
        // 判断我和视频博主之间的关系
        Fans fan = queryFansRelationship(myId, vlogerId);
        if (fan != null && fan.getIsFanFriendOfMine() == YesOrNo.YES.type) { // 如果是朋友关系
            Fans pendingFan = queryFansRelationship(vlogerId, myId);
            // 把对方和我的关系变成非朋友关系
            pendingFan.setIsFanFriendOfMine(YesOrNo.NO.type);
            // 把关系的更新同步到后台数据库中
            fansMapper.updateByPrimaryKeySelective(pendingFan);
        }
        // 删除我和对方关联的记录，但是对方和我关联的记录不删除，注意两者的区别
        fansMapper.delete(fan);
    }

    /**
     * 用户进行某个视频博主的页面的时候查询，我是否关注了他
     * @param myId
     * @param vlogerId
     * @return
     */
    @Override
    public boolean queryDoIFollowVloger(String myId, String vlogerId) {
        Fans vloger = queryFansRelationship(myId, vlogerId);
        return vloger != null;
    }

    @Override
    public boolean queryDoIFollowVloger(String myId) {
        return false;
    }


    /**
     * 查询我的关注列表
     * @param myId 我的id
     * @param page
     * @param pageSize
     * @return
     */
    @Override
    public PagedGridResult queryMyFollows(String myId, Integer page, Integer pageSize) {
        Map<String, Object> map = new HashMap<>();
        /**
         * 该map中放查询条件，也就是where中的内容
         */
        map.put("myId", myId);
        PageHelper.startPage(page, pageSize);
        // 根据自定义的查询，查询我关注的博主
        List<VlogerVO> list = fansMapperCustom.queryMyFollows(map);
        return setterPagedGrid(list, page);
    }

    /**
     * 查询我的粉丝列表
     * @param myId
     * @param page
     * @param pageSize
     * @return
     */
    @Override
    public PagedGridResult queryMyFans(String myId, Integer page, Integer pageSize) {
        Map<String, Object> map = new HashMap<>();
        map.put("myId", myId);
        PageHelper.startPage(page, pageSize);
        List<FansVO> list = fansMapperCustom.queryMyFans(map);
        /**
         * 我们要查询如果我们是朋友关系（这个也可以在数据库中查询，但是效率较慢）因此延迟到这里去redis中查询。如果是朋友关系，那么标志为互关。
         */
        for (FansVO f : list) {
            String relationShip = redis.get(REDIS_FANS_AND_VLOGGER_RELATIONSHIP + ":" +myId + ":" + f.getFanId());
            if (StringUtils.isNotBlank(relationShip) && relationShip.equalsIgnoreCase("1")) {
                f.setFriend(true);
            }
        }
        return setterPagedGrid(list, page);
    }
}
