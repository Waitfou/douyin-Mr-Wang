package com.wangguo.service;

import com.wangguo.bo.CommentBO;
import com.wangguo.pojo.Comment;
import com.wangguo.utils.PagedGridResult;
import com.wangguo.vo.CommentVO;

public interface CommentService {
    public CommentVO createComment(CommentBO commentBO);

    /**
     * 查询评论列表
     * @param vlogId
     * @param userId
     * @param page
     * @param pageSize
     * @return
     */
    public PagedGridResult queryVlogComments(String vlogId, String userId, Integer page, Integer pageSize);

    public void deleteComment(String commentUserId,
                              String commentId,
                              String vlogId);

    public Comment getComment(String id);
}
