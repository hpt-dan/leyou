package com.leyou.item.controller;

import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecGroupDTO;
import com.leyou.item.pojo.SpecParamDTO;
import com.leyou.item.service.SpecService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @package: com.leyou.item.controller
 * @version: V1.0
 * @author: Administrator
 * @date: 2019/4/21 22:12
 * @description:获取规格组
 */
@RestController
@RequestMapping("spec")
public class SpecController {


    @Autowired
    private SpecService specService;


    /**
     * 获取规格组参数
     * @param id
     * @return
     */
    @GetMapping("groups/of/category")
    public ResponseEntity<List<SpecGroupDTO>> queryGroupByCategory(@RequestParam("id")Long id){
        List<SpecGroupDTO> specGroupDTOS = specService.queryGroupByCategoryId(id);
        return ResponseEntity.ok(specGroupDTOS);
    }

    /**
     * 获取规格参数,根据规格参数组的id和类的id
     * @param gid
     * @return
     */
    @GetMapping("params")
    public ResponseEntity<List<SpecParamDTO>> querySpecParams(@RequestParam(value = "gid",required = false) Long gid,
                                                              @RequestParam(value = "cid",required = false) Long cid
    ) {

        List<SpecParamDTO> specParamDTOS = specService.querySpecParams(gid, cid);
        return ResponseEntity.ok(specParamDTOS);
    }


}
