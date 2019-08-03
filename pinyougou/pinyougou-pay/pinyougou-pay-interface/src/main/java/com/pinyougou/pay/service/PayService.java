package com.pinyougou.pay.service;

import java.util.Map;

public interface PayService {
    /**
     * 调用支付接口实现获取支付二维码
     * @param outTradeNo 交易号
     * @param total_fee 支付金额
     * @return 下单结果（微信那边统一下单的结果；二维码链接地址；交易号；支付总金额）
     */
    Map<String, Object> createNative(String outTradeNo, String total_fee);

    /**
     * 根据交易号查询支付微信中的订单
     * @param outTradeNo 交易号
     * @return 订单信息i
     */
    Map<String, String> queryPayStatus(String outTradeNo);

    /**
     * 根据订单号关闭支付系统中的订单
     * @param outTradeNo 订单号
     * @return 操作结果
     */
    Map<String, String> closeOrder(String outTradeNo);
}
