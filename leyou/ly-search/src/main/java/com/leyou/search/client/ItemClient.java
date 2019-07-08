package com.leyou.search.client;


import com.leyou.common.pojo.PageResult;
import com.leyou.item.pojo.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient("item-service")
public interface ItemClient {

    /**
     * 分页查询spu
     * @param page
     * @param rows
     * @param key
     * @param saleable
     * @return
     */
    @GetMapping("spu/page")
    PageResult<SpuDTO> querySpuByPage(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "rows", defaultValue = "5") Integer rows,
            @RequestParam(value = "key", required = false) String key,
            @RequestParam(value = "saleable", required = false) Boolean saleable);

    /**
     * 根据类的id列表，查询类对象集合
     * @param idList
     * @return
     */
    @GetMapping("category/list")
    List<CategoryDTO> queryByIds(@RequestParam("ids") List<Long> idList);


    /**
     * 根据spu的id查询sku
     * @param spuId
     * @return
     */
    @GetMapping("sku/of/spu")
    List<SkuDTO> querySkuBySpuId(@RequestParam("id") Long spuId);


    /**
     * 查询可搜索的参数列表
     * @param gid
     * @param cid
     * @param searching
     * @return
     */
    @GetMapping("spec/params")
    List<SpecParamDTO> querySpecParams(
            @RequestParam(value = "gid",required = false) Long gid,
            @RequestParam(value = "cid",required = false)Long cid,
            @RequestParam(value = "searching",required = false)Boolean searching);


    @GetMapping("spu/detail")
    SpuDetailDTO querySpuDetailById(@RequestParam("id")Long spuId);


    /**
     * 根据id的集合查询商品分类
     * @param idList 商品分类的id集合
     * @return 分类集合
     */
    @GetMapping("brand/list")
    List<BrandDTO> queryBrandByIds(@RequestParam("ids") List<Long> idList);

}
