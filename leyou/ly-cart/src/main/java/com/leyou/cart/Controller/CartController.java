package com.leyou.cart.Controller;

import com.leyou.cart.entity.Cart;
import com.leyou.cart.service.CartService;
import com.sun.org.apache.regexp.internal.RE;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.PublicKey;
import java.util.List;

/**
 * @package: com.leyou.cart.Controller
 * @version: V1.0
 * @author: Administrator
 * @date: 2019/4/21 22:12
 * @description:购物车模块
 */
@RestController
public class CartController {

    @Autowired
    private CartService cartService;

    /**
     * 登录后的购物车添加商品
     * @param cart
     * @return
     */
    @PostMapping
    public ResponseEntity<Void> addCart(@RequestBody Cart cart) {
        cartService.addCart(cart);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


    /**
     * 购物车数据的展示
     * @return
     */
    @GetMapping("list")
    public ResponseEntity<List<Cart>> queryCartList(){
       return ResponseEntity.ok(cartService.queryCartList());
    }


    /**
     * 修改购物车中商品的个数
     * @param skuId
     * @param num
     * @return
     */
    @PutMapping
    public ResponseEntity<Void> updateNum(@RequestParam("id")Long skuId,
                                          @RequestParam("num")Integer num){

        cartService.updateNum(skuId, num);
        return ResponseEntity.ok().build();

    }

    /**
     * 删除购物车中的商品
     * @param skuId
     * @return
     */
    @DeleteMapping("{skuId}")
    public ResponseEntity<Void> deleteCart(@PathVariable("skuId")Long skuId){

        cartService.deleteCart(skuId);
        return ResponseEntity.ok().build();
    }

    /**
     * 将离线的购物车中的商品，添加到登录的购物车中
     * @param cartList
     * @return
     */
    @PostMapping("list")
    public ResponseEntity<Void> addCartList(@RequestBody List<Cart> cartList){

        cartService.addCartList(cartList);
        return ResponseEntity.ok().build();
    }
}
