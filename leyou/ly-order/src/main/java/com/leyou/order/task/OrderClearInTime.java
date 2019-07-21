package com.leyou.order.task;

import com.leyou.item.client.ItemClient;
import com.leyou.order.entity.Order;
import com.leyou.order.entity.OrderDetail;
import com.leyou.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @package: com.leyou.order.task
 * @version: V1.0
 * @author: Administrator
 * @date: 2019/4/21 22:12
 * @description:订单清理
 */
@Component
@Slf4j
public class OrderClearInTime {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ItemClient itemClient;

    @Autowired
    private RedissonClient redissonClient;

    /**
     * 定期清除没有支付的订单
     * @throws InterruptedException
     */
    @Scheduled(fixedDelay = 60000)
    public void clearOrder() throws InterruptedException {
// 创建锁对象，并制定锁的名称
        RLock lock = redissonClient.getLock("taskLock");
        // 获取锁,设置自动失效时间为30s
        boolean isLock = lock.tryLock();
        // 判断是否获取锁
        if (!isLock) {
            // 获取失败
            log.info("获取锁失败，停止定时任务");
            return;
        }
        try {
            // 执行业务
            log.info("获取锁成功，执行定时任务。");
            // 模拟任务耗时
            //TODO 把清理订单的业务，写到这里


            List<Order> orders = orderService.queryOrderByState(1);

            //查看是否过期，然后进行订单删除。
            List<Order> outtimeOrders = new ArrayList<>();

            for (Order order : orders) {
                if(System.currentTimeMillis()-order.getCreateTime().getTime() > (1000)){
                    outtimeOrders.add(order);
                }
            }

            //修改订单的状态
            if(outtimeOrders.size() != 0){
                orderService.updateStatus(outtimeOrders);
            }

            List<Long> orderIdS = outtimeOrders.stream().map(Order::getOrderId).collect(Collectors.toList());
            //对库存进行修改
            List<OrderDetail> orderDetailList = orderService.queryOrderDetailByOrderIds(orderIdS);
            Map<Long, Integer> skusMap = new HashMap<>();
            for (OrderDetail orderDetail : orderDetailList) {
                Long skuId = orderDetail.getSkuId();
                if (skusMap.containsKey(skuId)) {
                    Integer num = orderDetail.getNum();
                    skusMap.put(skuId, num + orderDetail.getNum());
                }else {
                    skusMap.put(skuId,orderDetail.getNum());
                }
            }
            itemClient.plusStock(skusMap);




        } catch (Exception e) {
            log.error("任务执行异常", e);
        } finally {
            // 释放锁
            lock.unlock();
            log.info("任务执行完毕，释放锁");
        }

    }
}
