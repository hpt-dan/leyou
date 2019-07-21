package com.leyou.item.controller;

import com.leyou.common.pojo.PageResult;
import com.leyou.item.pojo.SkuDTO;
import com.leyou.item.pojo.SpuDTO;
import com.leyou.item.pojo.SpuDetailDTO;
import com.leyou.item.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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


    /**
     * spu的id查询SpuDetailDTO
     * @param id
     * @return
     */
    @GetMapping("spu/detail")
    public ResponseEntity<SpuDetailDTO> querySpuDetailById(@RequestParam("id") Long id) {
        return ResponseEntity.ok(goodsService.querySpuDetailById(id));
    }

    /**
     * 根据spuID查询sku
     * @param id spuID
     * @return sku的集合
     */
    @GetMapping("sku/of/spu")
    public ResponseEntity<List<SkuDTO>> querySkuBySpuId(@RequestParam("id") Long id) {
        return ResponseEntity.ok(this.goodsService.querySkuListBySpuId(id));
    }

    /**
     * 修改商品
     * @param spu
     * @return
     */
    @PutMapping("goods")
    public ResponseEntity<Void> updateGoods(@RequestBody SpuDTO spu) {
        goodsService.updateGoods(spu);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


    /**
     * 更改商品的上架与下架
     * @param id
     * @param saleable
     * @return
     */
    @PutMapping("/spu/saleable")
    public ResponseEntity<Void> updateSpuSaleable(@RequestParam("id") Long id, @RequestParam("saleable") Boolean saleable) {
        goodsService.updateSaleable(id, saleable);
        return ResponseEntity.ok().build();
    }

    /**
     * 根据spu的id查询spu,包含spuDtail,skus
     * @param id
     * @return
     */
    @GetMapping("spu/{id}")
    public ResponseEntity<SpuDTO> querySpuById(@PathVariable("id")Long id){

        return ResponseEntity.ok(goodsService.querySpuById(id));
    }


    /**
     * 无状态购物车的查询
     * @param ids
     * @return
     */
    @GetMapping("sku/list")
    public ResponseEntity<List<SkuDTO>> querySkuByIds(@RequestParam("ids") List<Long> ids){

        return ResponseEntity.ok(goodsService.querySkuByIds(ids));
    }


    /**
     * 减库存
     * @param cartMap 商品id及数量的map  两个参数，skuId，要减的数量
     */
    @PutMapping("/stock/minus")
    public ResponseEntity<Void> minusStock(@RequestBody Map<Long, Integer> cartMap){
        goodsService.minusStock(cartMap);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


    /**
     * 加库存
     * @param cartMap 商品id及数量的map  两个参数，skuId，要加的数量
     */
    @PutMapping("/stock/plus")
    public ResponseEntity<Void> plusStock(@RequestBody Map<Long, Integer> cartMap) {
        goodsService.plusStock(cartMap);
        return ResponseEntity.ok().build();
    }


}
