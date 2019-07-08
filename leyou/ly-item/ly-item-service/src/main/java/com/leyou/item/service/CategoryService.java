package com.leyou.item.service;

import com.leyou.item.pojo.CategoryDTO;

import java.util.List;

public interface CategoryService {
    List<CategoryDTO> queryListByParent(Long pid);


    List<CategoryDTO> queryListByBrandId(Long bid);

    List<CategoryDTO> queryCategoryByIds(List<Long> ids);
}
