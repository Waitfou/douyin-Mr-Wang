package com.wangguo.mapper;

import com.wangguo.vo.IndexVlogVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface VlogMapperCustom {
    /**
     *
     * @param map
     * @return
     */
    public List<IndexVlogVO> getIndexVlogList(@Param("paramMap") Map<String, Object> map);

    /**
     * 根据map中的条件获取视频Vlog的细节信息
     * @param map
     * @return
     */
    public List<IndexVlogVO> getVlogDetailById(@Param("paramMap") Map<String, Object> map);

    /**
     * 根据map中的条件获取我点赞过的视频
     * @param map
     * @return
     */
    public List<IndexVlogVO> getMyLikedVlogList(@Param("paramMap")Map<String, Object> map);

    public List<IndexVlogVO> getMyFollowVlogList(@Param("paramMap")Map<String, Object> map);
    public List<IndexVlogVO> getMyFriendVlogList(@Param("paramMap")Map<String, Object> map);
}
