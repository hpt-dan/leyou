package com.leyou.item.service;

import com.leyou.common.pojo.PageResult;
import com.leyou.item.pojo.SpuDTO;

public interface GoodsService {
    PageResult<SpuDTO> querySpuByPage(Integer page, Integer rows, Boolean saleable, String key);

    void saveGoods(SpuDTO spuDTO);
}
