package com.leyou.item.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.pojo.PageResult;
import com.leyou.common.utils.BeanHelper;
import com.leyou.item.mapper.SkuMapper;
import com.leyou.item.mapper.SpuDetailMapper;
import com.leyou.item.mapper.SpuMapper;
import com.leyou.item.pojo.*;
import com.leyou.item.service.BrandService;
import com.leyou.item.service.CategoryService;
import com.leyou.item.service.GoodsService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @package: com.leyou.item.service.impl
 * @version: V1.0
 * @author: Administrator
 * @date: 2019/4/21 22:12
 * @description:商品管理模块
 */
@Service
public class GoodsServiceImpl implements GoodsService {


    @Autowired
    private SpuMapper spuMapper;

    @Autowired
    private SpuDetailMapper detailMapper;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BrandService brandService;

    /**
     * 查询商品的分页业务
     * @param page
     * @param rows
     * @param saleable
     * @param key
     * @return
     */
    @Transactional
    @Override
    public PageResult<SpuDTO> querySpuByPage(Integer page, Integer rows, Boolean saleable, String key) {

        // 1 分页
        PageHelper.startPage(page, rows);

        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        //添加过滤条件
        if (StringUtils.isNotBlank(key)){
            criteria.andLike("name","%" + key + "%");
        }

        if(saleable != null){
            criteria.andEqualTo("saleable", saleable);
        }



        example.setOrderByClause("update_time DESC");
        //查询spu
        List<Spu> list = spuMapper.selectByExample(example);
        if(CollectionUtils.isEmpty(list)){
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }

        PageInfo<Spu> info = new PageInfo<>(list);


        // DTO转换
        List<SpuDTO> spuDTOList = BeanHelper.copyWithCollection(list, SpuDTO.class);
        // 5 处理分类名称和品牌名称
        handleCategoryAndBrandName(spuDTOList);

        return new PageResult<>(info.getTotal(), spuDTOList);
    }


    /**
     * 获取商品的分类品牌的名字。
     * @param list
     */

    private void handleCategoryAndBrandName(List<SpuDTO> list) {
        for (SpuDTO spu : list) {
            // 查询分类
            String categoryName = categoryService.queryCategoryByIds(spu.getCategoryIds())
                    .stream()
                    .map(CategoryDTO::getName).collect(Collectors.joining("/"));
            spu.setCategoryName(categoryName);
            // 查询品牌
            BrandDTO brand = brandService.queryById(spu.getBrandId());
            spu.setBrandName(brand.getName());
        }
    }

    /**
     * 保存商品业务
     * @param spuDTO
     */
    @Override
    @Transactional
    public void saveGoods(SpuDTO spuDTO) {
        //获取Spu插到表中
        Spu spu = BeanHelper.copyProperties(spuDTO, Spu.class);

        int count = spuMapper.insertSelective(spu);
        if (count != 1) {
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }
        //获取SpuDetail，并插入表中
        SpuDetailDTO spuDetailDTO = spuDTO.getSpuDetail();
        SpuDetail spuDetail = BeanHelper.copyProperties(spuDetailDTO, SpuDetail.class);
        spuDetail.setSpuId(spu.getId());
        count = detailMapper.insertSelective(spuDetail);
        if (count != 1) {
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }

        //获取Sku,插入到表中
        List<SkuDTO> skuDTOList = spuDTO.getSkus();
        List<Sku> skuList = new ArrayList<>();
        for (SkuDTO skuDTO : skuDTOList) {
            skuDTO.setSpuId(spu.getId());
            skuList.add(BeanHelper.copyProperties(skuDTO, Sku.class));
        }
        // 保存sku
        count = skuMapper.insertList(skuList);
        if (count != skuList.size()) {
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }
    }
}
