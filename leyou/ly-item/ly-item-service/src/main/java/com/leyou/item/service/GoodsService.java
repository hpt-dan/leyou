package com.leyou.item.service;

import com.leyou.common.pojo.PageResult;
import com.leyou.item.pojo.SkuDTO;
import com.leyou.item.pojo.SpuDTO;
import com.leyou.item.pojo.SpuDetailDTO;

import java.util.List;
import java.util.Map;

public interface GoodsService {
    PageResult<SpuDTO> querySpuByPage(Integer page, Integer rows, Boolean saleable, String key);

    void saveGoods(SpuDTO spuDTO);

    SpuDetailDTO querySpuDetailById(Long id);

    List<SkuDTO> querySkuListBySpuId(Long id);

    void updateGoods(SpuDTO spu);

    void updateSaleable(Long id, Boolean saleable);

    SpuDTO querySpuById(Long id);

    List<SkuDTO> querySkuByIds(List<Long> ids);

    void minusStock(Map<Long, Integer> cartMap);

}
