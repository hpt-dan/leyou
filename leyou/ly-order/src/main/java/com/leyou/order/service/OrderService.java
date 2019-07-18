package com.leyou.order.service;

import com.leyou.common.auth.entity.UserInfo;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.common.utils.IdWorker;
import com.leyou.item.client.ItemClient;
import com.leyou.item.pojo.SkuDTO;
import com.leyou.order.dto.CartDTO;
import com.leyou.order.dto.OrderDTO;
import com.leyou.order.entity.Order;
import com.leyou.order.entity.OrderDetail;
import com.leyou.order.entity.OrderLogistics;
import com.leyou.order.entity.OrderStatusEnum;
import com.leyou.order.interceptor.UserInterceptor;
import com.leyou.order.mapper.OrderDetailMapper;
import com.leyou.order.mapper.OrderLogisticsMapper;
import com.leyou.order.mapper.OrderMapper;
import com.leyou.user.client.UserClient;
import com.leyou.user.dto.AddressDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @package: com.leyou.order.service
 * @version: V1.0
 * @author: Administrator
 * @date: 2019/4/21 22:12
 * @description:
 */
@Service
public class OrderService {


    @Autowired
    private ItemClient itemClient;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private UserClient userClient;


    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private OrderLogisticsMapper orderLogisticsMapper;


    public void createOrder(OrderDTO orderDTO) {

        long orderId = idWorker.nextId();

        //封装订单详情
        List<CartDTO> carts = orderDTO.getCarts();

        long totalFee = 0;

        //获取skuId的集合
        List<Long> skuIds = carts.stream()
                .map(CartDTO::getSkuId)
                .collect(Collectors.toList());

        //把list集合通过stream留转换为map Map<SkuId,num>
        Map<Long, Integer> cartsMap = carts.stream()
                .collect(Collectors.toMap(CartDTO::getSkuId, CartDTO::getNum));


        List<SkuDTO> skuDTOS = itemClient.querySkuByIds(skuIds);
        List<OrderDetail> list = new ArrayList<>();
        for (SkuDTO skuDTO : skuDTOS) {
            OrderDetail orderDetail = BeanHelper.copyProperties(skuDTO, OrderDetail.class);
            orderDetail.setId(null);
            orderDetail.setOrderId(orderId);
            orderDetail.setSkuId(skuDTO.getId());
            Integer num = cartsMap.get(skuDTO.getId());
            orderDetail.setNum(num);
            orderDetail.setImage(StringUtils.substringBefore(skuDTO.getImages(), ","));
            list.add(orderDetail);
        }

        //封装订单
        Order order = BeanHelper.copyProperties(orderDTO, Order.class);
        order.setOrderId(orderId);
        order.setTotalFee(totalFee);
        order.setActualFee(totalFee);
        order.setPostFee(0L);

        UserInfo userInfo = UserInterceptor.getUserInfo();
        order.setUserId(userInfo.getId());
        //设置订单状态
        order.setStatus(OrderStatusEnum.INIT.value());

        //封装物流信息
        AddressDTO addressDTO = userClient.queryAddressById(orderDTO.getAddressId());
        OrderLogistics orderLogistics = BeanHelper.copyProperties(addressDTO, OrderLogistics.class);
        orderLogistics.setOrderId(orderId);

        //保存order订单
        int count = orderMapper.insertSelective(order);

        if (1 != count) {
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }

        //保存orderDetail 订单详情
        count = orderDetailMapper.insertList(list);

        if (count != list.size()) {
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }

        //保存物流，
        count = orderLogisticsMapper.insertSelective(orderLogistics);
        if (1 != count) {
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }

        //修改库存
        itemClient.minusStock(cartsMap);

    }
}
