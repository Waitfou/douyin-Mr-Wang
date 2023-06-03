package com.wangguo.mapper;

import com.wangguo.vo.CommentVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository //该注解表示装饰的类是存储库，用于封装存储，检索，和搜索行为的机制，该机制模仿对象的集合，它是@Component注解的一种特殊功能，允许通过类路径扫描自动检测实现类
public interface CommentMapperCustom {
    /**
     * 获取评论列表
     * @param map
     * @return
     */
    public List<CommentVO> getCommentList(@Param("paramMap") Map<String, Object> map);
}
