package com.leyou.item.service;

import com.leyou.item.pojo.SpecGroupDTO;
import com.leyou.item.pojo.SpecParamDTO;

import java.util.List;

public interface SpecService {

    List<SpecGroupDTO> queryGroupByCategoryId(Long id);

    List<SpecParamDTO> querySpecParams(Long id);


}
