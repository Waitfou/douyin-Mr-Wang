package com.wangguo.service.impl;

import com.github.pagehelper.PageHelper;
import com.sun.org.apache.bcel.internal.generic.IF_ACMPEQ;
import com.wangguo.base.BaseInfoProperties;
import com.wangguo.bo.VlogBO;
import com.wangguo.enums.MessageEnum;
import com.wangguo.enums.YesOrNo;
import com.wangguo.grace.result.GraceJSONResult;
import com.wangguo.mapper.MyLikedVlogMapper;
import com.wangguo.mapper.VlogMapper;
import com.wangguo.mapper.VlogMapperCustom;
import com.wangguo.pojo.MyLikedVlog;
import com.wangguo.pojo.Vlog;
import com.wangguo.service.FansService;
import com.wangguo.service.MsgService;
import com.wangguo.service.VlogService;
import com.wangguo.utils.PagedGridResult;
import com.wangguo.vo.IndexVlogVO;
import org.apache.commons.lang3.StringUtils;
import org.n3r.idworker.Sid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 对Vlog视频相关的操作
 */
@Service
public class VlogServiceImpl extends BaseInfoProperties implements VlogService {
    @Autowired
    private VlogMapper vlogMapper;
    @Autowired
    private VlogMapperCustom vlogMapperCustom;
    @Autowired
    private MyLikedVlogMapper myLikedVlogMapper;
    @Autowired
    private MsgService msgService;
    @Autowired
    private FansService fansService;
    @Autowired
    private Sid sid; //全局主键

    /**
     * 通过前端传来的业务对象完成创建vlog
     * @param vlogBO vlog业务对象
     */
    @Override
    public void createVlog(VlogBO vlogBO) {
        String vid = sid.nextShort();
        Vlog vlog = new Vlog();
        BeanUtils.copyProperties(vlogBO, vlog); // 第一个参数是源source，第二个参数是target
        vlog.setId(vid);

        vlog.setLikeCounts(0);
        vlog.setCommentsCounts(0);
        /**
         * 以下的参数是在后端设置，当然也可以在数据库中设置默认值
         */
        vlog.setIsPrivate(YesOrNo.NO.type);
        vlog.setCreatedTime(new Date());
        vlog.setUpdatedTime(new Date());
        // Mapper需要传入的就是pojo对象。
        vlogMapper.insert(vlog);
    }

    @Override
    public PagedGridResult getIndexVlogList(String userId, String search, Integer page, Integer pageSize) {
        /**
         * 该map集合中放查询条件
         */
        PageHelper.startPage(page, pageSize); // 设置页面号和页面大小
        Map<String, Object> map = new HashMap<>();
        if (StringUtils.isNotBlank(search)) {
            map.put("search", search);
        }
        List<IndexVlogVO> list = vlogMapperCustom.getIndexVlogList(map);

        // 遍历查询到的每一个结果
        for (IndexVlogVO v : list) {
            String vlogerId = v.getVlogerId();
            String vlogId = v.getVlogId();
            if (StringUtils.isNotBlank(userId)) {
                boolean doIFollowVloger = fansService.queryDoIFollowVloger(userId, vlogerId);
                v.setDoIFollowVloger(doIFollowVloger);
                // 判断当前对象是否点赞了该视频
                v.setDoILikeThisVlog(doILikeVlog(userId, vlogId));
            }
            // 获取当前视频被点赞的总数
            v.setLikeCounts(getVlogBeLikedCounts(vlogId));
        }
        return setterPagedGrid(list, page);
    }

    /**
     * 根据视频主键获取vlog信息
     * @param userId 用户id
     * @param vlogId 该用户对应的vlog的id
     * @return
     */
    @Override
    public IndexVlogVO getVlogDetailById(String userId, String vlogId) {
        Map<String, Object> map = new HashMap<>();
        map.put("vlogId", vlogId);

        List<IndexVlogVO> list = vlogMapperCustom.getVlogDetailById(map);

        if (list != null && list.size() > 0 && !list.isEmpty()) {
            IndexVlogVO vlogVO = list.get(0);
            return vlogVO;
//            return setterVO(vlogVO, userId);
        }
        return null;
    }

    private IndexVlogVO setterVO(IndexVlogVO v, String userId) {
        String vlogerId = v.getVlogerId();
        String vlogId = v.getVlogId();

        if (StringUtils.isNotBlank(userId)) {
            // 查询我是否关注了这个博主
            boolean doIFollowVloger = fansService.queryDoIFollowVloger(userId, vlogerId);
            v.setDoIFollowVloger(doIFollowVloger);

            // 判断当前用户是否点赞过这个视频
            v.setDoILikeThisVlog(doILikeVlog(userId, vlogId));
        }
        v.setLikeCounts(getVlogBeLikedCounts(vlogId));

        return v;
    }

    /**
     * 查看我是否喜欢这个视频
     * @param myId
     * @param vlogId
     * @return
     */
    private boolean doILikeVlog(String myId, String vlogId) {
        String doILike = redis.get(REDIS_USER_LIKE_VLOG + ":" + myId + ":" + vlogId);
        boolean isLike = false;
        if (StringUtils.isNotBlank(doILike) && doILike.equalsIgnoreCase("1")) {
            isLike = true;
        }
        return isLike;
    }

    @Transactional
    @Override
    public void changeToPrivateOrPublic(String userId, String vlogId, Integer yesOrNo) {
        Example example = new Example(Vlog.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("id", vlogId); // 视频的id
        criteria.andEqualTo("vlogerId", userId); // 视频作者的id

        Vlog pendingVlog = new Vlog();
        pendingVlog.setIsPrivate(yesOrNo);

        // 把example对应的这个数据的值中进行修改，修改策略为只修改pendingVlog中不为null的
        vlogMapper.updateByExampleSelective(pendingVlog, example);
    }

    /**
     * 查询我的公开/私密视频
     * @param userId 用户id
     * @param page 第几页
     * @param pageSize 页的大小
     * @param yesOrNo 是否私密（也就是对应的公开/私密）
     * @return 返回对应的视频列表
     */
    @Override
    public PagedGridResult queryMyVlogList(String userId, Integer page, Integer pageSize, Integer yesOrNo) {
        Example example = new Example(Vlog.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("vlogerId", userId);
        criteria.andEqualTo("isPrivate", yesOrNo);

        PageHelper.startPage(page, pageSize); // 对查询的结果进行分页,page表示第几页，pageSize表示这页的个数，前端通过滑动更新page然后查询出结果。
        // 根据where条件中中的vlogerId的值和isPrivate的值在vlog表中查询符合条件的结果
        List<Vlog> list = vlogMapper.selectByExample(example);

        return setterPagedGrid(list, page);

    }

    @Transactional
    @Override
    public void userLikeVlog(String userId, String vlogId) {
        String rid = sid.nextShort();
        MyLikedVlog likedVlog = new MyLikedVlog();
        likedVlog.setId(rid);
        likedVlog.setVlogId(vlogId); // 被喜欢的视频
        likedVlog.setUserId(userId); // 喜欢的用户
        myLikedVlogMapper.insert(likedVlog);

        //系统消息：点赞短视频
        Vlog vlog = this.getVlog(vlogId);
        Map msgContent = new HashMap();
        msgContent.put("vlogId", vlogId);
        msgContent.put("vlogCover", vlog.getCover());
        msgService.createMsg(userId,
                            vlog.getVlogerId(),
                            MessageEnum.LIKE_VLOG.type,
                            msgContent);
    }
    @Override
    public Vlog getVlog(String id) {
        return vlogMapper.selectByPrimaryKey(id);
    }

    /**
     * 用户取消点赞视频
     * @param userId
     * @param vlogId
     */
    @Transactional
    @Override
    public void userUnLikeVlog(String userId, String vlogId) {
        MyLikedVlog likedVlog = new MyLikedVlog();
        // 创建一个要取消点赞的视频信息的实体
        likedVlog.setVlogId(vlogId);
        likedVlog.setUserId(userId);
        myLikedVlogMapper.delete(likedVlog); // 根据传入的POJO信息删除对应的喜欢视频的信息
    }

    /**
     * 获得某个视频被喜欢的数量
     * @param vlogId
     * @return
     */
    @Override
    public Integer getVlogBeLikedCounts(String vlogId) {
//        System.out.println(vlogId);
        String countsStr = redis.get(REDIS_VLOG_BE_LIKED_COUNTS + ":" + vlogId);
        // 如果为空，就返回0
        if (StringUtils.isBlank(countsStr)) {
            countsStr = "0";
        }
        return Integer.valueOf(countsStr);
    }

    /**
     * 查询不需要控制并发
     * @param userId 用户id
     * @param page
     * @param pageSize
     * @return
     */
    @Override
    public PagedGridResult getMyLikedVlogList(String userId, Integer page, Integer pageSize) {
        PageHelper.startPage(page, pageSize);
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        // 根据用户id查询喜欢的视频列表
        List<IndexVlogVO> list = vlogMapperCustom.getMyLikedVlogList(map);
        return setterPagedGrid(list, page);
    }
//
//    @Override
//    public PagedGridResult getMyLikedVlogList(String userId, Integer page, Integer pageSize) {
//        return null;
//    }
//

    /**
     * 查询用户关注的视频博主发布的短视频列表
     * @param myId
     * @param page
     * @param pageSize
     * @return
     */
    @Override
    public PagedGridResult getMyFollowVlogList(String myId, Integer page, Integer pageSize) {
        PageHelper.startPage(page, pageSize);
        Map<String, Object> map = new HashMap<>();
        map.put("myId", myId);
        // 查询出我关注的用户发布的所有视频
        List<IndexVlogVO> list = vlogMapperCustom.getMyFollowVlogList(map);
        // 告诉前端这些我与视频的信息，我是否关注（在这里必定关注了），是否点赞了视频，获取视频的点赞总数
        for (IndexVlogVO v : list) {
            String vlogerId = v.getVlogerId();
            String vlogId = v.getVlogId();
            if (StringUtils.isNotBlank(myId)) {
                // 用户必定关注了该博主
                v.setDoIFollowVloger(true);
                // 判断当前用户是否点赞了该视频，便于前端更新视频上的信息
                v.setDoILikeThisVlog(doILikeVlog(myId, vlogId));
            }
            // 获取当前视频被点赞的总数
            v.setLikeCounts(getVlogBeLikedCounts(vlogId));
        }
        return setterPagedGrid(list, page);
    }

    @Override
    public PagedGridResult getMyFriendVlogList(String myId, Integer page, Integer pageSize) {
        PageHelper.startPage(page, pageSize);
        Map<String, Object> map = new HashMap<>();
        map.put("myId", myId);
        List<IndexVlogVO> list = vlogMapperCustom.getMyFriendVlogList(map);
        for (IndexVlogVO v : list) {
            String vlogerId = v.getVlogerId();
            String vlogId = v.getVlogId();

            if (StringUtils.isNotBlank(myId)) {
                v.setDoIFollowVloger(true); // 一定是关注的，因为是朋友嘛，那么一定互相关注了的。

                //判断用户是否点赞过视频，给设置给视图对象
                v.setDoILikeThisVlog(doILikeVlog(myId, vlogId));
            }
            // 获得当前视频被点赞过的总数，并设置给视图对象
            v.setLikeCounts(getVlogBeLikedCounts(vlogId));
        }
        return setterPagedGrid(list, page);
    }
//
//    @Override
//    public Vlog getVlog(String id) {
//        return null;
//    }
    @Transactional
    @Override
    public void flushCounts(String vlogId, Integer counts) {
        Vlog vlog = new Vlog();
        vlog.setId(vlogId);
        vlog.setLikeCounts(counts);
        // 通过设置的主键更新
        vlogMapper.updateByPrimaryKeySelective(vlog);
    }
}
