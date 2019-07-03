package com.leyou.item.mapper;

import com.leyou.item.pojo.Brand;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface BrandMapper extends Mapper<Brand> {

    /**
     * 往类和品牌的中间表添加数据
     * @param bid
     * @param ids
     * @return
     */
    int insertCategoryBrand(@Param("bid") Long bid, @Param("ids") List<Long> ids);


    /**
     * 查询tb_category_brand中brand_id为某一个参数时的行数
     * @param bid
     * @return
     */
    @Select("SELECT COUNT(*) FROM tb_category_brand WHERE brand_id = #{bid}")
    int queryCountByBrandId(@Param("bid") Long bid);


    /**
     * 根据品牌的id删除tb_category_brand的数据
     * @param bid
     * @return
     */
    @Delete("DELETE from tb_category_brand WHERE brand_id = #{bid}")
    int deleteCategoryBrand(@Param("bid")Long bid);
}
