package com.leyou.item.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.pojo.PageResult;
import com.leyou.common.utils.BeanHelper;
import com.leyou.item.mapper.BrandMapper;
import com.leyou.item.pojo.Brand;
import com.leyou.item.pojo.BrandDTO;
import com.leyou.item.service.BrandService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

/**
 * @package: com.leyou.item.service.impl
 * @version: V1.0
 * @author: Administrator
 * @date: 2019/4/21 22:12
 * @description:
 */
@Service
public class BrandServiceImpl implements BrandService {

    @Autowired
    private BrandMapper brandMapper;

    @Override
    public PageResult<BrandDTO> queryBrandByPage(Integer page, Integer row,
                                                 String key, String sortBy, Boolean desc) {
        //添加分页数据
        PageHelper.startPage(page, row);

        //添加条件查询
        Example example = new Example(Brand.class);
        Example.Criteria criteria = example.createCriteria();
        if (StringUtils.isNotBlank(key)) {
            criteria.orLike("name", "%" + key + "%")
                    .orEqualTo("letter", key);
        }

        //进行排序
        if (StringUtils.isNotBlank(sortBy)) {
            example.setOrderByClause(sortBy + (desc ? " DESC" : " ASC"));
        }

        List<Brand> brands = brandMapper.selectByExample(example);

        if(CollectionUtils.isEmpty(brands)){
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }

        PageInfo<Brand> pageInfo = new PageInfo<>(brands);
        List<BrandDTO> brandDTOS = BeanHelper.copyWithCollection(brands, BrandDTO.class);
        PageResult<BrandDTO> pageResult = new PageResult<>();
        pageResult.setItems(brandDTOS);
        pageResult.setTotal(pageInfo.getTotal());

        return pageResult;
    }

    @Override
    @Transactional
    public void saveBrand(Brand brand, List<Long> ids) {

        //往品牌库中添加数据
        int i = brandMapper.insertSelective(brand);
        if(i != 1){
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }

        //往中间表添加数据
        int i1 = brandMapper.insertCategoryBrand(brand.getId(), ids);
        if(i1 != ids.size()){
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }
    }

    @Override
    @Transactional
    public void updateBrand(Brand brand, List<Long> ids) {

        //查询品牌类的中间表要删除的数量
        int count = brandMapper.queryCountByBrandId(brand.getId());

        //删除品牌类的中间表对应的数据
        int count1 = brandMapper.deleteCategoryBrand(brand.getId());
        System.out.println("count1 = " + count1);
        if(count != count1){
            throw new LyException(ExceptionEnum.DELETE_OPERATION_FAIL);
        }

        //往品牌库中添加数据
        int i = brandMapper.updateByPrimaryKeySelective(brand);
        if(i != 1){
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }

        //往中间表添加数据
        int i1 = brandMapper.insertCategoryBrand(brand.getId(), ids);
        if(i1 != ids.size()){
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }
    }
}
