package com.leyou.item.mapper;

import com.leyou.common.mapper.BaseMapper;
import com.leyou.item.pojo.Category;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;


public interface CategoryMapper  extends BaseMapper<Category> {


    @Select("SELECT tb.id,tb.`name` FROM tb_category tb INNER JOIN tb_category_brand tc ON tb.id = tc.category_id WHERE tc.brand_id = #{bid};")
    List<Category> queryByBrandId(@Param("bid")Long bid);


}
