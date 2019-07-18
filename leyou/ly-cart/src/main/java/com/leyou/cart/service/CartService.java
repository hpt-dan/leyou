package com.leyou.cart.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.leyou.cart.entity.Cart;
import com.leyou.cart.interceptors.UserInterceptor;
import com.leyou.common.auth.entity.UserInfo;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @package: com.leyou.cart.service
 * @version: V1.0
 * @author: Administrator
 * @date: 2019/4/21 22:12
 * @description:
 */
@Service
public class CartService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static String KEY_PREFIX = "ly:cart:user:id:";


    /**
     * 添加到购物车
     * @param cart
     */
    public void addCart(Cart cart) {

        UserInfo userInfo = UserInterceptor.getUserInfo();
        String id =KEY_PREFIX + (userInfo.getId().longValue());

        //获取购物车的数据
        BoundHashOperations<String, String, String> ops = redisTemplate.boundHashOps(id);
        if(ops.hasKey(cart.getSkuId().toString())){
            //如果该用户中存在该商品，修改该商品的数量,并保存到redis中
            String carJson = ops.get(cart.getSkuId().toString().toString());
            Cart cart1 = JsonUtils.nativeRead(carJson, new TypeReference<Cart>() {
            });

            cart1.setNum(cart1.getNum() + cart.getNum());
            ops.put(cart1.getSkuId().toString(),JsonUtils.toString(cart1));
        }else {
            //用户中不存在该商品，新增该商品,并保存到redis中
            ops.put(cart.getSkuId().toString(), JsonUtils.toString(cart));
        }
    }


    /**
     * 购物车数据的查询
     * @return
     */
    public List<Cart> queryCartList() {
        UserInfo userInfo = UserInterceptor.getUserInfo();
        String id =KEY_PREFIX + (userInfo.getId().longValue());

        //获取购物车的数据
        BoundHashOperations<String, String, String> ops = redisTemplate.boundHashOps(id);
        List<String> values = ops.values();
        if(values != null && values.size() != 0){
            List<Cart> carts = new ArrayList<>();

            for (String value : values) {
                carts.add(JsonUtils.nativeRead(value, new TypeReference<Cart>() {
                }));
            }
            return carts;
        }

        throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
    }

    /**
     * 修改购物车商品的数量
     * @param skuId
     * @param num
     */
    public void updateNum(Long skuId, Integer num) {
        UserInfo userInfo = UserInterceptor.getUserInfo();
        String id =KEY_PREFIX + (userInfo.getId().longValue());

        //获取购物车的数据
        BoundHashOperations<String, String, String> ops = redisTemplate.boundHashOps(id);


        if(!ops.hasKey(skuId.toString())){
            return;
        }
        String cartJson = ops.get(skuId.toString());
        Cart cart = JsonUtils.nativeRead(cartJson, new TypeReference<Cart>() {
        });
        cart.setNum(num);

        ops.put(cart.getSkuId().toString(), JsonUtils.toString(cart));

    }

    /**
     * 删除购物车中的商品
     * @param skuId
     */
    public void deleteCart(Long skuId) {

        UserInfo userInfo = UserInterceptor.getUserInfo();
        String id =KEY_PREFIX + (userInfo.getId().longValue());

        //获取购物车的数据
        BoundHashOperations<String, String, String> ops = redisTemplate.boundHashOps(id);

        if (!ops.hasKey(skuId.toString())){
            return;
        }

        ops.delete(skuId.toString());

    }


    /**
     * 批量添加cart中的商品
     * @param cartList
     */
    public void addCartList(List<Cart> cartList) {

        for (Cart cart : cartList) {
            addCart(cart);
        }

    }
}
