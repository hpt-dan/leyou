package com.leyou.item.controller;

import com.leyou.item.pojo.CategoryDTO;
import com.leyou.item.pojo.Item;
import com.leyou.item.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping("of/parent")
    public ResponseEntity<List<CategoryDTO>>  queryByParentId(@RequestParam(value = "pid",defaultValue = "0")Long pid){
        List<CategoryDTO> categoryDTOS = categoryService.queryListByParent(pid);
        return ResponseEntity.ok(categoryDTOS);
    }

    @GetMapping("of/brand")
    public ResponseEntity<List<CategoryDTO>> queryListByBrandId(@RequestParam("id")Long bid){

        List<CategoryDTO> categoryDTOS = categoryService.queryListByBrandId(bid);
        return ResponseEntity.ok(categoryDTOS);
    }
}