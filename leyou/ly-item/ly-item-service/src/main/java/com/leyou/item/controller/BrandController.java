package com.leyou.item.controller;

import com.leyou.common.pojo.PageResult;
import com.leyou.item.pojo.Brand;
import com.leyou.item.pojo.BrandDTO;
import com.leyou.item.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @package: com.leyou.item.controller
 * @version: V1.0
 * @author: Administrator
 * @date: 2019/4/21 22:12
 * @description:品牌模块
 */
@RestController
@RequestMapping("brand")
public class BrandController {

    @Autowired
    private BrandService brandService;

    /**
     * 查询品牌
     * @param page
     * @param rows
     * @param key
     * @param sortBy
     * @param desc
     * @return
     */
    @GetMapping("page")
    public ResponseEntity<PageResult<BrandDTO>> queryBrandByPage(@RequestParam(value = "page")Integer page,
                                                                 @RequestParam(value = "rows")Integer rows,
                                                                 @RequestParam(value = "key")String key,
                                                                 @RequestParam(value = "sortBy")String sortBy,
                                                                 @RequestParam(value = "desc")Boolean desc
                                                                 ){

        PageResult<BrandDTO> pageResult = brandService.queryBrandByPage(page, rows, key, sortBy, desc);
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 新增品牌
     * @param brand
     * @param ids
     * @return
     */
    @PostMapping
    public ResponseEntity<Void> saveBrand(Brand brand, @RequestParam("cids") List<Long> ids) {
        brandService.saveBrand(brand, ids);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


    /**
     * 更新品牌信息
     * @param brand
     * @param ids
     * @return
     */
    @PutMapping
    public ResponseEntity<Void> updateBrand(Brand brand, @RequestParam("cids") List<Long> ids){
        brandService.updateBrand(brand, ids);
        return ResponseEntity.ok().build();
    }


    /**
     * 根据类的id的查询品爱信息
     * @param id
     * @return
     */
    @GetMapping("of/category")
    public ResponseEntity<List<BrandDTO>> queryByCategoryId(@RequestParam("id")Long id){
        return ResponseEntity.ok(brandService.queryByCategoryId(id));
    }


}
