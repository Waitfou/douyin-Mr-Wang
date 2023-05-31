package com.wangguo.mapper;

import com.wangguo.vo.FansVO;
import com.wangguo.vo.VlogerVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * 自定义粉丝数据库的查询
 */
@Repository
public interface FansMapperCustom {
    public List<VlogerVO> queryMyFollows(@Param("paramMap") Map<String, Object> map);

    public List<FansVO> queryMyFans(@Param("paramMap") Map<String, Object> map);
}
