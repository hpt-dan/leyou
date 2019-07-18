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
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.leyou.common.constants.MQConstants.Exchange.ITEM_EXCHANGE_NAME;
import static com.leyou.common.constants.MQConstants.RoutingKey.ITEM_DOWN_KEY;
import static com.leyou.common.constants.MQConstants.RoutingKey.ITEM_UP_KEY;

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


    /**
     * spu的id查询SpuDetailDTO
     * @param id
     * @return
     */
    @Override
    public SpuDetailDTO querySpuDetailById(Long id) {

        SpuDetail spuDetail = detailMapper.selectByPrimaryKey(id);
        if (spuDetail == null) {
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        return BeanHelper.copyProperties(spuDetail, SpuDetailDTO.class);
    }


    /**
     * 根据spuId查询sku集合
     * @param id
     * @return
     */
    @Override
    public List<SkuDTO> querySkuListBySpuId(Long id) {
        Sku s = new Sku();
        s.setSpuId(id);
        List<Sku> list = skuMapper.select(s);
        if (CollectionUtils.isEmpty(list)) {
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        return BeanHelper.copyWithCollection(list, SkuDTO.class);
    }

    /**
     * 更改商品
     * @param spuDTO
     */
    @Override
    public void updateGoods(SpuDTO spuDTO) {

        //1：删除sku，然后加入新的sku
        //2:直接修改spu

        Long spuId = spuDTO.getId();
        if (spuId == null) {
            // 请求参数有误
            throw new LyException(ExceptionEnum.INVALID_PARAM_ERROR);
        }
        // 1.删除sku
        Sku sku = new Sku();
        sku.setSpuId(spuId);
        // 查询数量
        int size = skuMapper.selectCount(sku);
        if(size > 0) {
            // 删除
            int count = skuMapper.delete(sku);
            if(count != size){
                throw new LyException(ExceptionEnum.UPDATE_OPERATION_FAIL);
            }
        }

        // 2.更新spu
        Spu spu = BeanHelper.copyProperties(spuDTO, Spu.class);
        int count = spuMapper.updateByPrimaryKeySelective(spu);
        if (count != 1) {
            throw new LyException(ExceptionEnum.UPDATE_OPERATION_FAIL);
        }

        // 3.更新spuDetail
        SpuDetail spuDetail = BeanHelper.copyProperties(spuDTO.getSpuDetail(), SpuDetail.class);
        spuDetail.setSpuId(spuId);
        count = detailMapper.updateByPrimaryKeySelective(spuDetail);
        if (count != 1) {
            throw new LyException(ExceptionEnum.UPDATE_OPERATION_FAIL);
        }

        // 4.新增sku
        List<SkuDTO> dtoList = spuDTO.getSkus();
        // 处理dto
        List<Sku> skuList = dtoList.stream().map(dto -> {
            dto.setEnable(false);
            // 添加spu的id
            dto.setSpuId(spuId);
            return BeanHelper.copyProperties(dto, Sku.class);
        }).collect(Collectors.toList());
        count = skuMapper.insertList(skuList);
        if (count != skuList.size()) {
            throw new LyException(ExceptionEnum.UPDATE_OPERATION_FAIL);
        }
    }


    @Autowired
    private AmqpTemplate amqpTemplate;
    /**
     * 商品的上下架
     * @param id
     * @param saleable
     */
    @Override
    public void updateSaleable(Long id, Boolean saleable) {

        //更新spu
        Spu spu = new Spu();
        spu.setId(id);
        spu.setSaleable(saleable);
        int count = spuMapper.updateByPrimaryKeySelective(spu);
        if (count != 1) {
            throw new LyException(ExceptionEnum.UPDATE_OPERATION_FAIL);
        }

        //根据商品的上下架，发送信息给消息队列
        String key = saleable ? ITEM_UP_KEY : ITEM_DOWN_KEY;
        amqpTemplate.convertAndSend(ITEM_EXCHANGE_NAME, key, id);
    }


    /**
     * 根据spu的id查询spu,包含spuDtail,skus
     * @param id
     * @return
     */
    @Override
    public SpuDTO querySpuById(Long id) {
        Spu spu = spuMapper.selectByPrimaryKey(id);
        SpuDTO spuDTO = BeanHelper.copyProperties(spu, SpuDTO.class);

        //在spu中封装spuDtail
        spuDTO.setSpuDetail(querySpuDetailById(id));

        spuDTO.setSkus(querySkuListBySpuId(id));

        return spuDTO;
    }


    /**
     * 根据sku的id集合查询SkuDTO。
     * @param ids
     * @return
     */
    @Override
    public List<SkuDTO> querySkuByIds(List<Long> ids) {

        List<Sku> skus = skuMapper.selectByIdList(ids);
        return BeanHelper.copyWithCollection(skus, SkuDTO.class);

    }

    /**
     * 下单后减去商品更改商品的库存
     * @param cartMap
     */
    @Override
    public void minusStock(Map<Long, Integer> cartMap) {

        cartMap.entrySet().forEach(cart->{
            Long skuId = cart.getKey();
            Integer num  = cart.getValue();
            Sku sku = skuMapper.selectByPrimaryKey(skuId);
            sku.setId(skuId);

            Integer storeNum = sku.getStock() - num;
            if(storeNum < 0){
                //库存不足
                throw new LyException(ExceptionEnum.STOCK_NOT_ENOUGH);
            }
            sku.setStock(storeNum);

            this.skuMapper.updateByPrimaryKeySelective(sku);
        });
    }
}
