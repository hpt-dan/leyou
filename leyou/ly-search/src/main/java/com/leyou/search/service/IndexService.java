package com.leyou.search.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.leyou.common.utils.BeanHelper;
import com.leyou.common.utils.JsonUtils;
import com.leyou.item.client.ItemClient;
import com.leyou.item.pojo.*;
import com.leyou.search.pojo.Goods;
import com.leyou.search.repository.GoodsRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @package: com.leyou.search.service
 * @version: V1.0
 * @author: Administrator
 * @date: 2019/4/21 22:12
 * @description:
 */
@Service
public class IndexService {

    @Autowired
    private ItemClient itemClient;

    @Autowired
    private GoodsRepository goodsRepository;

    /**
     * 数据转换的逻辑
     *
     * @param spuDTO
     * @return
     */
    public Goods buildGoods(SpuDTO spuDTO) {

        //spu封装到goods
        Goods goods = BeanHelper.copyProperties(spuDTO, Goods.class);
        goods.setCategoryId(spuDTO.getCid3());
        //将创建时间封装到goods
        goods.setCreateTime(spuDTO.getCreateTime().getTime());

        //all封装到goods(标题(name)，分类，品牌)
        String names = this.itemClient.queryByIds(spuDTO.getCategoryIds())
                .stream()
                .map(CategoryDTO::getName).collect(Collectors.joining(" "));

        goods.setAll(spuDTO.getName()+" "+names);

        //将sku封装到goods
        List<SkuDTO> skuDTOS = this.itemClient.querySkuBySpuId(spuDTO.getId());

        List<Map<String, Object>> skuList = new ArrayList<>();
        Set<Long> prices = new HashSet<>();
        skuDTOS.forEach(skuDTO -> {
            prices.add(skuDTO.getPrice());
            Map<String, Object> skuMap = new HashMap<>();
            skuMap.put("id", skuDTO.getId());
            skuMap.put("image", skuDTO.getImages().split(",")[0]);
            skuMap.put("price", skuDTO.getPrice());
            skuMap.put("title", skuDTO.getTitle());
            skuList.add(skuMap);
        });
        //赋值sku
        goods.setSkus(JsonUtils.toString(skuList));

        //赋值sku的价格
        goods.setPrice(prices);

        //spec封装到goods
        List<SpecParamDTO> specParamDTOS = this.itemClient.querySpecParams(null, spuDTO.getCid3(), true);


        SpuDetailDTO spuDetailDTO = this.itemClient.querySpuDetailById(spuDTO.getId());
        //查询通用规格
        Map<Long,Object> genericMap = JsonUtils.nativeRead(spuDetailDTO.getGenericSpec(), new TypeReference<Map<Long, Object>>() {
        });

        //查询特有规格
        Map<Long,List<String>> specailMap = JsonUtils.nativeRead(spuDetailDTO.getSpecialSpec(), new TypeReference<Map<Long, List<String>>>() {
        });

        Map<String, Object> specMap = new HashMap<>();

        specParamDTOS.forEach(specParam->{

            //规格参数的id
            Long id = specParam.getId();
            //规格参数的名称
            String name = specParam.getName();


            Object value;
            if (specParam.getGeneric()){//通用的从，通用规格属性中取值
                value = genericMap.get(id);
            }else{//特有的从特有规格参数中取值

                value = specailMap.get(id);
            }

            // 判断是否是数值类型
            if(specParam.getNumeric()){
                // 是数字类型，分段
                value = chooseSegment(value, specParam);
            }

            specMap.put(name,value);
        });

        goods.setSpecs(specMap);
        return goods;
    }


    /**
     * 将数值转化为字符串
     *
     * @param value
     * @param p
     * @return
     */
    private String chooseSegment(Object value, SpecParamDTO p) {
        if (value == null || StringUtils.isBlank(value.toString())) {
            return "其它";
        }
        double val = parseDouble(value.toString());
        String result = "其它";
        // 保存数值段
        for (String segment : p.getSegments().split(",")) {
            String[] segs = segment.split("-");
            // 获取数值范围
            double begin = parseDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if (segs.length == 2) {
                end = parseDouble(segs[1]);
            }
            // 判断是否在范围内
            if (val >= begin && val < end) {
                if (segs.length == 1) {
                    result = segs[0] + p.getUnit() + "以上";
                } else if (begin == 0) {
                    result = segs[1] + p.getUnit() + "以下";
                } else {
                    result = segment + p.getUnit();
                }
                break;
            }
        }
        return result;
    }

    private double parseDouble(String str) {
        try {
            return Double.parseDouble(str);
        } catch (Exception e) {
            return 0;
        }
    }


    /**
     * 根据spuID创建对用goods的索引到索引库中
     * @param id
     */
    public void createIndex(Long id) {

        SpuDTO spuDTO = this.itemClient.querySpuById(id);
        Goods goods = buildGoods(spuDTO);
        goodsRepository.save(goods);

    }


    /**
     * 根据spuId从索引库中删除对应的索引
     * @param id
     */
    public void deleteById(Long id) {
        goodsRepository.deleteById(id);
    }
}
