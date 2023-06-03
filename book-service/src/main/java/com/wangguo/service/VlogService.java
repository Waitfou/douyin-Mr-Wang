package com.wangguo.service;

import com.wangguo.bo.VlogBO;
import com.wangguo.pojo.Vlog;
import com.wangguo.utils.PagedGridResult;
import com.wangguo.vo.IndexVlogVO;

public interface VlogService {

    /**
     * 新增vlog视频
     */
    public void createVlog(VlogBO vlogBO);


    /**
     * 查询首页/搜索的vlog列表
     */
    public PagedGridResult getIndexVlogList(String userId,
                                            String search,
                                            Integer page,
                                            Integer pageSize);

    /**
     * 根据视频主键查询vlog详细描述
     */
    public IndexVlogVO getVlogDetailById(String userId, String vlogId);

    //    @Override
    //    public void changeToPrivateOrPublic(String userId, String vlogId, Integer yesOrNo) {
    //
    //    }
    //
    //    @Override
    //    public PagedGridResult queryMyVlogList(String userId, Integer page, Integer pageSize, Integer yesOrNo) {
    //        return null;
    //    }
    //
    //    @Override
    //    public void userLikeVlog(String userId, String vlogId) {
    //        String rid = sid.nextShort();
    //        MyLikedVlog likedVlog = new MyLikedVlog();
    //        likedVlog.setId(rid);
    //        likedVlog.setVlogId(vlogId);
    //        likedVlog.setUserId(userId);
    //        myLikedVlogMapper.insert(likedVlog);
    //        // 系统消息：点赞短视频
    //        Vlog vlog = this.getVlog(vlogId);
    //        Map msgContent = new HashMap();
    //        msgContent.put("vlogId", vlogId);
    //        msgContent.put("vlogCover", vlog.getCover());
    //
    //    }
    //
    //    /**
    //     * 用户点赞视频
    //     * @param userId
    //     * @param vlogId
    //     */
    //    @Override
    //    public void userUnLikeVlog(String userId, String vlogId) {
    //
    //    }
    //
    public Integer getVlogBeLikedCounts(String vlogId);
    /**
     * 查询用户点赞过的视频
     */
    public PagedGridResult getMyLikedVlogList(String userId,
                                              Integer page,
                                              Integer pageSize);
    /**
     * 用户把视频改为公开/私密的视频
     */
    public void changeToPrivateOrPublic(String userId,
                                        String vlogId,
                                        Integer yesOrNo);

    /**
     * 查询用的公开/私密的视频列表
     */
    public PagedGridResult queryMyVlogList(String userId,
                                           Integer page,
                                           Integer pageSize,
                                           Integer yesOrNo);

    /**
     * 用户点赞/喜欢视频
     */
    public void userLikeVlog(String userId, String vlogId);

    /**
     * 用户取消点赞/喜欢视频
     */
    public void userUnLikeVlog(String userId, String vlogId);
//
//    /**
//     * 获得用户点赞视频的总数
//     */
//    public Integer getVlogBeLikedCounts(String vlogId);
//
//    /**
//     * 查询用户点赞过的短视频
//     */
//    public PagedGridResult getMyLikedVlogList(String userId,
//                                              Integer page,
//                                              Integer pageSize);
//
    /**
     * 查询用户关注的博主发布的短视频列表
     */
    public PagedGridResult getMyFollowVlogList(String myId,
                                               Integer page,
                                               Integer pageSize);

    /**
     * 查询朋友发布的短视频列表
     */
    public PagedGridResult getMyFriendVlogList(String myId,
                                               Integer page,
                                               Integer pageSize);

    /**
     * 根据主键查询vlog
     */
    public Vlog getVlog(String id);
}
