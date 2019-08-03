package com.pinyougou.cart.service;

import com.pinyougou.vo.Cart;

import java.util.List;

public interface CartService {
    /**
     * 往购物车列表添加商品
     *
     * @param itemId 商品sku id
     * @param num    购买数量
     */
    List<Cart> addItemToCartList(List<Cart> cartList, Long itemId, Integer num);

    /**
     * 读取用户在redis中的购物车列表
     * @param username 用户名
     * @return 购物车列表
     */
    List<Cart> findCartListByUsername(String username);

    /**
     * 将购物车数据存入redis
     * @param newCartList 购物车列表
     * @param username 用户名
     */
    void saveCartListToRedisByUsername(List<Cart> newCartList, String username);

    /**
     * 合并两个购物车列表到一个新的购物车列表并返回
     * @param cartList1 购物车列表1
     * @param cartList2 购物车列表2
     * @return 购物车列表
     */
    List<Cart> mergeCartList(List<Cart> cartList1, List<Cart> cartList2);
}
