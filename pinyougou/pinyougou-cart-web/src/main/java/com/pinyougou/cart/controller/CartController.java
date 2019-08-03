package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.common.util.CookieUtils;
import com.pinyougou.vo.Cart;
import com.pinyougou.vo.Result;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequestMapping("/cart")
@RestController
public class CartController {

    //品优购购物车列表在浏览器cookie中的名称
    private static final String COOKIE_CART_LIST = "PYG_CART_LIST";
    //品优购购物车列表在浏览器cookie中的数据最大生存时间；1天
    private static final int COOKIE_CART_LIST_MAX_AGE = 3600*24;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private HttpServletResponse response;

    @Reference
    private CartService cartService;

    /**
     * 加入购物车、购物车购买数量的增加、删除
     * 未登录；将购物车数据存入cookie
     * 已登录：将购物车数据存入redis
     *
     * @param itemId 商品sku id
     * @param num    购买数量
     * @return 操作结果
     */
    @GetMapping("/addItemToCartList")
    @CrossOrigin(origins = "http://item.pinyougou.com", allowCredentials = "true")
    public Result addItemToCartList(Long itemId, Integer num) {
        try {
/*
            //允许详情系统跨域获取资源
            response.setHeader("Access-Control-Allow-Origin", "http://item.pinyougou.com");
            //允许接受请求方的cookie信息
            response.setHeader("Access-Control-Allow-Credentials", "true");
*/

            String username = SecurityContextHolder.getContext().getAuthentication().getName();

            //获取原购物车数据
            List<Cart> cartList = findCartList();

            List<Cart> newCartList = cartService.addItemToCartList(cartList, itemId, num);
            //判断用户是否登录
            if ("anonymousUser".equals(username)) {
                //未登录；将购物车数据存入cookie
                String cartListJsonStr = JSON.toJSONString(newCartList);
                if (StringUtils.isNotBlank(cartListJsonStr)) {
                   CookieUtils.setCookie(request, response, COOKIE_CART_LIST, cartListJsonStr, COOKIE_CART_LIST_MAX_AGE, true);
                }

            } else {
                //已登录；将购物车数据存入redis
                cartService.saveCartListToRedisByUsername(newCartList, username);
            }
            return Result.ok("加入购物车成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.fail("加入购物车失败");
    }

    /**
     * 查询登录、未登录情况下购物车列表
     *
     * @return 购物车列表
     */
    @GetMapping("/findCartList")
    public List<Cart> findCartList() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        //未登录；从cookie获取购物车数据
        List<Cart> cookieCartList = new ArrayList<>();

        String cartListJsonStr = CookieUtils.getCookieValue(request, COOKIE_CART_LIST, true);
        if (StringUtils.isNotBlank(cartListJsonStr)) {
            cookieCartList = JSON.parseArray(cartListJsonStr, Cart.class);
        }

        //判断用户是否登录
        if ("anonymousUser".equals(username)) {
            return cookieCartList;
        } else {
            //已登录；从redis获取购物车数据
            List<Cart> redisCartList = cartService.findCartListByUsername(username);

           if(cookieCartList.size() > 0){
               //合并新购物车
               redisCartList = cartService.mergeCartList(cookieCartList, redisCartList);

               //保存到redis
               cartService.saveCartListToRedisByUsername(redisCartList, username);

               //删除cookie购物车
               CookieUtils.deleteCookie(request, response, COOKIE_CART_LIST);
           }

            return redisCartList;
        }
    }


    /**
     * 获取用户信息
     *
     * @return 用户信息
     */
    @GetMapping("/getUsername")
    public Map<String, Object> getUsername() {
        Map<String, Object> map = new HashMap<>();
        //如果未登录则返回的是anonymousUser
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        map.put("username", username);

        return map;
    }
}
