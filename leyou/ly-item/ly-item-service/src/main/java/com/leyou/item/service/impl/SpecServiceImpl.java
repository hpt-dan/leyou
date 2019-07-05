package com.leyou.item.service.impl;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.item.mapper.SpecGroupMapper;
import com.leyou.item.mapper.SpecParamMapper;
import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecGroupDTO;
import com.leyou.item.pojo.SpecParam;
import com.leyou.item.pojo.SpecParamDTO;
import com.leyou.item.service.SpecService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @package: com.leyou.item.service.impl
 * @version: V1.0
 * @author: Administrator
 * @date: 2019/4/21 22:12
 * @description:查询规格组业务
 */
@Service
public class SpecServiceImpl implements SpecService {

    @Autowired
    private SpecGroupMapper specGroupMapper;

    @Autowired
    private SpecParamMapper specParamMapper;


    /**
     * 获取规格参数组业务
     * @param id
     * @return
     */
    @Override
    public List<SpecGroupDTO> queryGroupByCategoryId(Long id) {

        SpecGroup specGroup = new SpecGroup();
        specGroup.setCid(id);


        List<SpecGroup> groupList = specGroupMapper.select(specGroup);
        if (CollectionUtils.isEmpty(groupList)) {
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }

        List<SpecGroupDTO> specGroupDTOS = BeanHelper.copyWithCollection(groupList, SpecGroupDTO.class);
        return specGroupDTOS;
    }


    /**
     * 获取规格参数业务
     * @param gid
     * @return
     */
    @Override
    public List<SpecParamDTO> querySpecParams(Long gid,Long cid) {

        SpecParam specParam = new SpecParam();
        specParam.setGroupId(gid);
        specParam.setCid(cid);

        List<SpecParam> list = specParamMapper.select(specParam);

        if (CollectionUtils.isEmpty(list)) {
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        return BeanHelper.copyWithCollection(list, SpecParamDTO.class);
    }




}
