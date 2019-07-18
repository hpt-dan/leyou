package com.leyou.auth.mapper;

import com.leyou.auth.entity.ApplicationInfo;
import com.leyou.common.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


public interface ApplicationInfoMapper extends BaseMapper<ApplicationInfo> {


    List<Long> queryTargetIdList(@Param("id") Long serviceId);

}