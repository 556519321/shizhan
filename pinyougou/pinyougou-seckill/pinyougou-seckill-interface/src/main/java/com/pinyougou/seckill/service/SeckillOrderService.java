package com.pinyougou.seckill.service;

import com.github.pagehelper.PageInfo;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.service.BaseService;

import java.util.List;

public interface SeckillOrderService extends BaseService<TbSeckillOrder> {
    /**
     * 根据条件搜索
     * @param pageNum 页号
     * @param pageSize 页面大小
     * @param seckillOrder 搜索条件
     * @return 分页信息
     */
    PageInfo<TbSeckillOrder> search(Integer pageNum, Integer pageSize, TbSeckillOrder seckillOrder);

    /**
     * 生成秒杀订单存入到redis
     * @param seckillGoodsId 秒杀商品id
     * @param userId 用户id
     * @return 订单id
     */
    String submitOrder(Long seckillGoodsId, String userId) throws InterruptedException, Exception;

    /**
     * 根据订单号到redis查询订单
     * @param outTradeNo 订单号
     * @return 秒杀订单
     */
    TbSeckillOrder getSeckillOrderInRedisByOrderId(String outTradeNo);

    /**
     * 支付完成后更新redis中的订单到mysql
     * @param outTradeNo 订单号
     * @param transactionId 微信支付订单号
     */
    void saveSeckillOrderInRedisToDb(String outTradeNo, String transactionId);

    /**
     * 根据订单号删除redis中的订单
     * @param outTradeNo 订单号
     */
    void deleteSeckillOrderByOutTradeNo(String outTradeNo) throws InterruptedException, Exception;
}
