package com.pinyougou.order.service;

import com.github.pagehelper.PageInfo;
import com.pinyougou.pojo.TbOrder;
import com.pinyougou.pojo.TbPayLog;
import com.pinyougou.service.BaseService;

import java.util.List;

public interface OrderService extends BaseService<TbOrder> {
    /**
     * 根据条件搜索
     * @param pageNum 页号
     * @param pageSize 页面大小
     * @param order 搜索条件
     * @return 分页信息
     */
    PageInfo<TbOrder> search(Integer pageNum, Integer pageSize, TbOrder order);

    /**
     * 将购物车中的订单商品保存成订单相关数据
     * @param order 订单基本信息
     * @return 支付日志id（交易编号）
     */
    String addOrder(TbOrder order);

    /**
     * 根据支付日志id查询支付日志
     * @param outTradeNo 支付日志id
     * @return 支付日志
     */
    TbPayLog findPayLogByOutTradeNo(String outTradeNo);

    /**
     * 根据支付日志id更新支付日志、所有订单的支付状态
     * @param outTradeNo 支付日志id
     * @param transaction_id 微信支付订单号
     */
    void updateOrderStatus(String outTradeNo, String transaction_id);
}
