package com.pinyougou.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.mapper.ItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.vo.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    //购物车数据在redis中的键名
    private static final String CART_LIST = "CART_LIST";

    @Autowired
    private ItemMapper itemMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public List<Cart> addItemToCartList(List<Cart> cartList, Long itemId, Integer num) {
        //判断商品是否存在与合法
        TbItem item = itemMapper.selectByPrimaryKey(itemId);
        if (item == null) {
            throw new RuntimeException("商品不存在！");
        }
        if (!"1".equals(item.getStatus())) {
            throw new RuntimeException("商品非法，不能购买");
        }
        Cart cart = findCartInCartListBySellerId(cartList, item.getSellerId());
        if(cart == null) {
            //1. 添加的商品对应的商家（cart）不存在在购物车列表（cartList）
            //   创建一个商家（cart）在其里面的orderItemList中添加该订单商品，然后将cart添加到cartList
            if (num > 0) {
                cart = new Cart();
                cart.setSellerId(item.getSellerId());
                cart.setSellerName(item.getSeller());

                //创建订单商品列表
                List<TbOrderItem> orderItemList = new ArrayList<>();

                //创建订单商品
                TbOrderItem orderItem = createOrderItem(item, num);
                orderItemList.add(orderItem);

                //订单商品列表
                cart.setOrderItemList(orderItemList);

                cartList.add(cart);
            } else {
                throw new RuntimeException("购买数量非法");
            }

        } else {
            //2. 添加的商品对应的商家（cart）存在在购物车列表（cartList）
            TbOrderItem orderItem = findOrderItemByItemId(cart.getOrderItemList(), itemId);
            if(orderItem != null) {
                //   2.1. 商品在商家（cart）的订单商品列表（orderItemList）中
                //   2.1.1. 将当前这个订单商品的购买数量叠加，重新计算总价格
                orderItem.setNum(orderItem.getNum()+num);
                orderItem.setTotalFee(orderItem.getPrice()*orderItem.getNum());
                if(orderItem.getNum() < 1) {
                    //   2.1.2. 如果在叠加之后购买的商品数量是小于等于0则需要将该订单商品从orderItemList移除
                    cart.getOrderItemList().remove(orderItem);
                }
                if(cart.getOrderItemList().size() == 0) {
                    //   2.1.3. 在orderItemList移除商品之后如果长度为0则要将该商家(cart)从cartList中移除
                    cartList.remove(cart);
                }
            } else {
                //   2.2. 商品不在商家（cart）的订单商品列表（orderItemList）中；购买数量合法的时候将商品加入该商家（cart）对应的订单商品列表中
                if (num > 0) {
                    orderItem = createOrderItem(item, num);
                    cart.getOrderItemList().add(orderItem);
                } else {
                    throw new RuntimeException("购买数量非法");
                }
            }
        }
        return cartList;
    }

    @Override
    public List<Cart> findCartListByUsername(String username) {
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps(CART_LIST).get(username);
        if (cartList != null) {
            return cartList;
        }
        return new ArrayList<>();
    }

    @Override
    public void saveCartListToRedisByUsername(List<Cart> newCartList, String username) {
        redisTemplate.boundHashOps(CART_LIST).put(username, newCartList);
    }

    @Override
    public List<Cart> mergeCartList(List<Cart> cartList1, List<Cart> cartList2) {
        for (Cart cart : cartList1) {
            for (TbOrderItem orderItem : cart.getOrderItemList()) {
                addItemToCartList(cartList2, orderItem.getItemId(), orderItem.getNum());
            }
        }
        return cartList2;
    }

    /**
     * 根据商品sku id查询在订单商品列表中是否存在订单商品
     * @param orderItemList 订单商品列表
     * @param itemId 商品sku id
     * @return 订单商品
     */
    private TbOrderItem findOrderItemByItemId(List<TbOrderItem> orderItemList, Long itemId) {
        for (TbOrderItem orderItem : orderItemList) {
            if (itemId.equals(orderItem.getItemId())) {
                return orderItem;
            }
        }

        return null;
    }

    /**
     * 根据商品sku 和购买数量创建订单商品
     * @param item 商品sku
     * @param num 购买数量
     * @return 订单商品对象
     */
    private TbOrderItem createOrderItem(TbItem item, Integer num) {
        TbOrderItem orderItem = new TbOrderItem();

        orderItem.setGoodsId(item.getGoodsId());
        orderItem.setItemId(item.getId());
        orderItem.setNum(num);
        orderItem.setPicPath(item.getImage());
        orderItem.setPrice(item.getPrice());
        orderItem.setTitle(item.getTitle());
        //总计 = 单价*购买数量
        orderItem.setTotalFee(orderItem.getPrice()*orderItem.getNum());

        return orderItem;
    }

    /**
     * 根据商家id查询
     *
     * @param cartList 购物车列表
     * @param sellerId 商家id
     * @return 购物车对象cart
     */
    private Cart findCartInCartListBySellerId(List<Cart> cartList, String sellerId) {
        if (cartList != null && cartList.size() > 0) {
            for (Cart cart : cartList) {
                if (sellerId.equals(cart.getSellerId())) {
                    return cart;
                }
            }
        }
        return null;
    }
}
