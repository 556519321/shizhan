package com.pinyougou.seckill.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pay.service.PayService;
import com.pinyougou.pojo.TbPayLog;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.seckill.service.SeckillOrderService;
import com.pinyougou.vo.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RequestMapping("/pay")
@RestController
public class PayController {

    @Reference
    private SeckillOrderService orderService;

    @Reference
    private PayService payService;

    /**
     * 根据交易号 统一下单 返回支付二维码链接地址
     * @param outTradeNo 交易号（支付日志id）
     * @return 微信那边统一下单的结果；二维码链接地址；交易号；支付总金额
     */
    @GetMapping("/createNative")
    public Map<String, Object> createNative(String outTradeNo){

        //查询支付日志
        TbSeckillOrder seckillOrder = orderService.getSeckillOrderInRedisByOrderId(outTradeNo);

        if (seckillOrder != null) {
            //总金额
            String total_fee = (long)(seckillOrder.getMoney()*100) + "";

            //统一下单获取支付二维码
            return payService.createNative(outTradeNo, total_fee);
        }

        return new HashMap<>();
    }

    /**
     * 根据交易号查询支付订单对应的支付状态
     * @param outTradeNo 交易号
     * @return 操作结果
     */
    @GetMapping("/queryPayStatus")
    public Result queryPayStatus(String outTradeNo){
        Result result = Result.fail("查询支付状态失败！");
        try {
            //1分钟内查询支付状态
            int count = 0;
            while (true) {
                //到支付系统中查询支付状态
                Map<String, String> map = payService.queryPayStatus(outTradeNo);

                if (map == null) {
                    break;
                }

                if ("SUCCESS".equals(map.get("trade_state"))) {
                    //支付成功需要保存redis中的订单到数据库
                    orderService.saveSeckillOrderInRedisToDb(outTradeNo, map.get("transaction_id"));

                    result = Result.ok("查询支付状态成功！");
                    break;
                }
                count++;

                if(count > 20){
                    result = Result.fail("支付超时");

                    //关闭微信支付系统的订单
                    Map<String, String> resultMap = payService.closeOrder(outTradeNo);
                    if ("ORDERPAID".equals(resultMap.get("err_code"))) {
                        //说明订单已经被支付
                        //支付成功需要保存redis中的订单到数据库
                        orderService.saveSeckillOrderInRedisToDb(outTradeNo, map.get("transaction_id"));

                        result = Result.ok("查询支付状态成功！");
                        break;
                    }

                    //将库存加1
                    orderService.deleteSeckillOrderByOutTradeNo(outTradeNo);

                    break;
                }

                //每隔3秒
                Thread.sleep(3000L);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
}
