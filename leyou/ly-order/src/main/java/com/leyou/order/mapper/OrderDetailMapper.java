package com.leyou.order.mapper;

import com.leyou.common.mapper.BaseMapper;
import com.leyou.order.entity.OrderDetail;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OrderDetailMapper extends BaseMapper<OrderDetail>{

    /**
     * 批量新增
     * @param details 商品详情的集合
     * @return 新增成功的行
     */
    int insertDetailList(@Param("details") List<OrderDetail> details);
}