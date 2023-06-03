package com.wangguo.service.impl;

import com.github.pagehelper.PageHelper;
import com.wangguo.base.BaseInfoProperties;
import com.wangguo.bo.CommentBO;
import com.wangguo.enums.MessageEnum;
import com.wangguo.enums.YesOrNo;
import com.wangguo.mapper.CommentMapper;
import com.wangguo.mapper.CommentMapperCustom;
import com.wangguo.pojo.Comment;
import com.wangguo.pojo.Vlog;
import com.wangguo.service.CommentService;
import com.wangguo.service.MsgService;
import com.wangguo.service.VlogService;
import com.wangguo.utils.PagedGridResult;
import com.wangguo.vo.CommentVO;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.units.qual.C;
import org.n3r.idworker.Sid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CommentServiceImpl extends BaseInfoProperties implements CommentService {
    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private VlogService vlogService;
    @Autowired
    private CommentMapperCustom commentMapperCustom;
    @Autowired
    private Sid sid;
    @Autowired
    private MsgService msgService;
    @Override
    public CommentVO createComment(CommentBO commentBO) {
        String commentId = sid.nextShort();
        Comment comment = new Comment();
        comment.setId(commentId);
        comment.setVlogId(commentBO.getVlogId()); // 评论的哪个视频
        comment.setVlogerId(commentBO.getVlogerId()); // 评论的视频的发布者

        comment.setCommentUserId(commentBO.getCommentUserId()); // 是谁评论的
        comment.setFatherCommentId(commentBO.getFatherCommentId());
        comment.setContent(commentBO.getContent());

        comment.setLikeCounts(0); // 评论的喜欢的个数
        comment.setCreateTime(new Date());

        commentMapper.insert(comment);
        // redis中存放每个视频的评论个数
        redis.increment(REDIS_VLOG_COMMENT_COUNTS + ":" + commentBO.getVlogId(), 1);
        // 把最新的评论包装成VO对象（视图对象），返回给前端使用
        CommentVO commentVO = new CommentVO();
        BeanUtils.copyProperties(comment, commentVO);

        // 系统消息：评论/回复
        Vlog vlog = vlogService.getVlog(commentBO.getVlogId());
        Map msgContent = new HashMap();
        msgContent.put("vlogId", vlog.getId());
        msgContent.put("vlogCover", vlog.getCover());
        msgContent.put("commnentId", commentId);
        msgContent.put("commentContent", commentBO.getContent());
        Integer type = MessageEnum.COMMENT_VLOG.type;
        if (StringUtils.isNotBlank(commentBO.getFatherCommentId()) && !commentBO.getFatherCommentId().equalsIgnoreCase("0")) {
            type = MessageEnum.REPLY_YOU.type;
        }
        msgService.createMsg(commentBO.getCommentUserId(),
                commentBO.getVlogerId(),
                type,
                msgContent);
        return commentVO;
    }

    @Override
    public PagedGridResult queryVlogComments(String vlogId, String userId, Integer page, Integer pageSize) {
        Map<String, Object> map = new HashMap<>();
        map.put("vlogId", vlogId);
        PageHelper.startPage(page, pageSize);
        // 根据视频id获取它的所有评论信息（已经评论的子评论，评论者的相关信息等）
        List<CommentVO> list = commentMapperCustom.getCommentList(map);
        /**
         * 需要向前端展示出每条评论的点赞总数
         * 已经我是否对这些评论点赞等。
         */
        for (CommentVO cv : list){
            String commentId = cv.getCommentId();
            // 当前短视频的某个评论的点赞总数
            String countsStr = redis.getHashValue(REDIS_VLOG_COMMENT_LIKED_COUNTS, commentId);
            Integer counts = 0;
            if (StringUtils.isNotBlank(countsStr)) {
                counts = Integer.valueOf(countsStr);
            }
            cv.setLikeCounts(counts);
            // 判断当前用户是否点赞过该评论
            String doILike = redis.hget(REDIS_USER_LIKE_COMMENT, userId + ":" + commentId);
            if (StringUtils.isNotBlank(doILike) && doILike.equalsIgnoreCase("1")) {
                cv.setIsLike(YesOrNo.YES.type);
            }
        }
        return setterPagedGrid(list, page);
    }

    /**
     * 删除用户自己的评论
     * @param commentUserId 该评论的用户id
     * @param commentId 该评论的id
     * @param vlogId 视频id
     */
    @Override
    public void deleteComment(String commentUserId, String commentId, String vlogId) {
        // pending待办的意思
        Comment pendingDelete = new Comment();
        pendingDelete.setId(commentId);
        System.out.println(commentId);
        System.out.println(commentUserId);
        pendingDelete.setCommentUserId(commentUserId);
        commentMapper.delete(pendingDelete);

        redis.decrement(REDIS_VLOG_COMMENT_COUNTS + ":" + vlogId, 1);
    }

    public Comment getComment(String id) {
        return commentMapper.selectByPrimaryKey(id);
    }
}
