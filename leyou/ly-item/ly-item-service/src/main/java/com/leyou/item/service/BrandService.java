package com.leyou.item.service;

import com.leyou.common.pojo.PageResult;
import com.leyou.item.pojo.Brand;
import com.leyou.item.pojo.BrandDTO;

import javax.validation.constraints.Max;
import java.util.List;

/**
 * @package: com.leyou.item.service
 * @version: V1.0
 * @author: Administrator
 * @date: 2019/4/21 22:12
 * @description:品牌的业务模块
 */
public interface BrandService {
    PageResult<BrandDTO> queryBrandByPage(Integer page,Integer row,
                                          String key,String sortBy,Boolean desc);

    void saveBrand(Brand brand, List<Long> ids);

    void updateBrand(Brand brand, List<Long> ids);
}
