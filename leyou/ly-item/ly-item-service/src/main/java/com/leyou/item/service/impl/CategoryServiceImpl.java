package com.leyou.item.service.impl;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.item.mapper.CategoryMapper;
import com.leyou.item.pojo.Category;
import com.leyou.item.pojo.CategoryDTO;
import com.leyou.item.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @package: com.leyou.item.service.impl
 * @version: V1.0
 * @author: Administrator
 * @date: 2019/4/21 22:12
 * @description:
 */
@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;


    @Override
    public List<CategoryDTO> queryListByParent(Long pid) {

        Category category = new Category();
        category.setParentId(pid);

        List<Category> list = categoryMapper.select(category);

        // 判断结果
        if(CollectionUtils.isEmpty(list)){
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOND);
        }

        return BeanHelper.copyWithCollection(list, CategoryDTO.class);
    }


    /**
     * 根据品牌的id查询类业务
     * @param bid
     * @return
     */
    @Override
    public List<CategoryDTO> queryListByBrandId(Long bid) {
        List<Category> categories = categoryMapper.queryByBrandId(bid);

        if(CollectionUtils.isEmpty(categories)){
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }

        List<CategoryDTO> categoryDTOS = BeanHelper.copyWithCollection(categories, CategoryDTO.class);
        return categoryDTOS;
    }


    /**
     * 根据类得id集合，查询类得名字
     * @param ids
     * @return
     */
    @Override
    public List<CategoryDTO> queryCategoryByIds(List<Long> ids){
        List<Category> list = categoryMapper.selectByIdList(ids);
        if (CollectionUtils.isEmpty(list)) {
            // 没找到，返回404
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
        return BeanHelper.copyWithCollection(list, CategoryDTO.class);
    }

}
