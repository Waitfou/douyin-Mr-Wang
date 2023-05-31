package com.wangguo.service.impl;

import com.github.pagehelper.PageHelper;
import com.wangguo.base.BaseInfoProperties;
import com.wangguo.bo.CommentBO;
import com.wangguo.mapper.CommentMapper;
import com.wangguo.mapper.CommentMapperCustom;
import com.wangguo.pojo.Comment;
import com.wangguo.service.CommentService;
import com.wangguo.service.VlogService;
import com.wangguo.utils.PagedGridResult;
import com.wangguo.vo.CommentVO;
import org.n3r.idworker.Sid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class CommentServiceImpl extends BaseInfoProperties implements CommentService {
    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private VlogService vlogService;
    @Autowired
    private CommentMapperCustom commentMapperCustom;
    @Autowired
    private Sid sid;
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

        // todo 系统消息：评论/回复
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
            String countStr = redis.getHashValue(REDIS_VLOG_COMMENT_LIKED_COUNTS, commentId);
        }
    }
}
