package com.wangguo.mapper;

import com.wangguo.vo.CommentVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface CommentMapperCustom {
    /**
     * 获取评论列表
     * @param map
     * @return
     */
    public List<CommentVO> getCommentList(@Param("paramMap") Map<String, Object> map);
}
