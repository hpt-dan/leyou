package com.leyou.item.pojo;

import lombok.Data;

import java.util.List;


@Data
public class SpecGroupDTO {
    private Long id;

    private Long cid;

    private String name;

    private List<SpecParamDTO> params;
}