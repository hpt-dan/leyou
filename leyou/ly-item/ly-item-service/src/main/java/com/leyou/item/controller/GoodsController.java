package com.leyou.item.controller;

import com.leyou.common.pojo.PageResult;
import com.leyou.item.pojo.SpuDTO;
import com.leyou.item.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @package: com.leyou.item.controller
 * @version: V1.0
 * @author: Administrator
 * @date: 2019/4/21 22:12
 * @description:
 */
@RestController
public class GoodsController {

    @Autowired
    private GoodsService goodsService;

    /**
     * 查询所以的商品
     * @param key
     * @param saleable
     * @param page
     * @param rows
     * @return
     */
    @GetMapping("spu/page")
    public ResponseEntity<PageResult<SpuDTO>> querySpuByPage(
            @RequestParam(value = "key",required = false)String key,
            @RequestParam(value = "saleable",required = false)Boolean saleable,
            @RequestParam(value = "page",defaultValue = "1")Integer page,
            @RequestParam(value = "rows",defaultValue = "5")Integer rows
    ){
        return ResponseEntity.ok(goodsService.querySpuByPage(page, rows, saleable, key));
    }




    /**
     * 插入商品
     * @param spuDTO
     * @return
     */
    @PostMapping("goods")
    public ResponseEntity<Void> saveGoods (@RequestBody SpuDTO spuDTO){

           goodsService.saveGoods(spuDTO);
        return ResponseEntity.ok().build();
    }
}
