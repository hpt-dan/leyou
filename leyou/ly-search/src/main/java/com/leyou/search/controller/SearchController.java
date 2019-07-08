package com.leyou.search.controller;

import com.leyou.common.pojo.PageResult;
import com.leyou.search.dto.GoodsDTO;
import com.leyou.search.dto.SearchRequest;
import com.leyou.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * @package: com.leyou.search.controller
 * @version: V1.0
 * @author: Administrator
 * @date: 2019/4/21 22:12
 * @description:商品查询模块
 */
@RestController
public class SearchController {

    @Autowired
    private SearchService searchService;

    /**
     * 查询商品spu
     * @param searchRequest
     * @return
     */
    @PostMapping("page")
    public ResponseEntity<PageResult<GoodsDTO>> pageQuery(@RequestBody SearchRequest searchRequest){
        return ResponseEntity.ok(searchService.pageQuery(searchRequest));
    }


    /**
     * 查询过滤的品牌，与类
     * @param searchRequest
     * @return
     */
    @PostMapping("filter")
    public ResponseEntity<Map<String, List<?>>> queryFilters(@RequestBody SearchRequest searchRequest) {
        return ResponseEntity.ok(searchService.queryFilters(searchRequest));
    }
}
