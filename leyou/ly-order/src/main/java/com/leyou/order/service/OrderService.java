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
import com.leyou.order.utils.PayHelper;
import com.leyou.user.client.UserClient;
import com.leyou.user.dto.AddressDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @package: com.leyou.order.service
 * @version: V1.0
 * @author: Administrator
 * @date: 2019/4/21 22:12
 * @description:
 */
@Service
@Slf4j
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


    public Long createOrder(OrderDTO orderDTO) {

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

        return orderId;

    }

    /**
     * 根据订单id查询订单
     * @param orderId
     * @return
     */
    public Order queryOrderById(Long orderId) {

        Order order = orderMapper.selectByPrimaryKey(orderId);
        if(null == order){
            throw new LyException(ExceptionEnum.ORDER_NOT_FOND);
        }
        return order;
    }


    @Autowired
    private PayHelper payHelper;
    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 生成二维码的URL
     * @param orderId
     * @return
     */
    public String createPayUrl(Long orderId) {
        // 先看是否已经生成过：
        String key = "ly.pay.url." + orderId;
        String cacheUrl = redisTemplate.opsForValue().get(key);
        if (StringUtils.isNoneBlank(cacheUrl)) {
            return cacheUrl;
        }
        // 查询订单
        Order order = orderMapper.selectByPrimaryKey(orderId);
        if(order == null){
            throw new LyException(ExceptionEnum.ORDER_NOT_FOUND);
        }
        // 判断订单状态
        Integer status = order.getStatus();
        if(!status.equals(OrderStatusEnum.INIT.value())){
            // 订单已经支付过了，订单状态异常
            throw new LyException(ExceptionEnum.INVALID_ORDER_STATUS);
        }
        // 支付金额，测试时写1
        Long actualPay = /*order.getActualPay()*/ 1L;

        // 商品描述
        String desc = "【乐优商城】商品信息";
        String url = payHelper.createOrder(orderId, actualPay, desc);

        // 存入redis，设置有效期为 2小时
        redisTemplate.opsForValue().set(key, url, 2, TimeUnit.HOURS);
        return url;
    }


    /**
     * 支付成功，威胁你服务回调的函数
     * @param result
     */
    @Transactional
    public void handleNotify(Map<String, String> result) {
        // 1 签名校验
        try {
            payHelper.isValidSign(result);
        }catch (Exception e){
            log.error("【微信回调】微信签名有误！, result: {}",result, e);
            throw new LyException(ExceptionEnum.INVALID_NOTIFY_SIGN, e);
        }
        // 2、业务校验
        payHelper.checkResultCode(result);

        // 3 校验金额数据
        String totalFeeStr = result.get("total_fee");
        String tradeNo = result.get("out_trade_no");
        if(StringUtils.isEmpty(totalFeeStr) || StringUtils.isEmpty(tradeNo)){
            // 回调参数中必须包含订单编号和订单金额
            throw new LyException(ExceptionEnum.INVALID_NOTIFY_PARAM);
        }
        // 3.1 获取结果中的金额
        long totalFee = Long.valueOf(totalFeeStr);
        // 3.2 获取订单
        Long orderId = Long.valueOf(tradeNo);
        Order order = orderMapper.selectByPrimaryKey(orderId);
        // 3.3.判断订单的状态，保证幂等
        if(!order.getStatus().equals(OrderStatusEnum.INIT.value())){
            // 订单已经支付，返回成功
            return;
        }
        // 3.4.判断金额是否一致
        if(totalFee != /*order.getActualPay()*/ 1){
            // 金额不符
            throw new LyException(ExceptionEnum.INVALID_NOTIFY_PARAM);
        }

        // 4 修改订单状态
        Order orderStatus = new Order();
        orderStatus.setStatus(OrderStatusEnum.PAY_UP.value());
        orderStatus.setOrderId(orderId);
        orderStatus.setPayTime(new Date());
        int count = orderMapper.updateByPrimaryKeySelective(orderStatus);
        if(count != 1){
            log.error("【微信回调】更新订单状态失败，订单id：{}", orderId);
            throw new LyException(ExceptionEnum.UPDATE_OPERATION_FAIL);
        }
        log.info("【微信回调】, 订单支付成功! 订单编号:{}", orderId);
    }


    /**
     * 查询用户支付状态
     * @param orderId
     * @return
     */
    public Integer queryPayStatus(Long orderId) {
        Order order = orderMapper.selectByPrimaryKey(orderId);
        if(order == null){
            throw new LyException(ExceptionEnum.ORDER_NOT_FOUND);
        }
        return order.getStatus();
    }


    /**
     * 根据订单未支付状态，查询订单
     * @param status
     * @return
     */
    public List<Order> queryOrderByState(Integer status){
        Order order = new Order();
        order.setStatus(status);

        List<Order> orders = orderMapper.select(order);
        if(orders == null){
            throw new LyException(ExceptionEnum.ORDER_NOT_FOND);
        }
        return orders;
    }


    /**
     * 更改订单的状态
     * @param outtimeOrders
     */
    public void updateStatus(List<Order> outtimeOrders) {
        for (Order outtimeOrder : outtimeOrders) {
            outtimeOrder.setStatus(5);
            orderMapper.updateByPrimaryKeySelective(outtimeOrder);
        }
    }

    /**
     * 根据订单的id集合查询对应的OrderDetail集合
     * @param orderIdS
     * @return
     */
    public List<OrderDetail> queryOrderDetailByOrderIds(List<Long> orderIdS) {

        List<OrderDetail> orderDetailList = new ArrayList<>();
        for (Long orderId : orderIdS) {
            OrderDetail record = new OrderDetail();
            record.setOrderId(orderId);
            orderDetailList.addAll(orderDetailMapper.select(record));
        }

        return orderDetailList;
    }
}