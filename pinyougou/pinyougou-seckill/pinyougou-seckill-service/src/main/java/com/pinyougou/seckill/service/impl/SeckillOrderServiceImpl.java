package com.pinyougou.seckill.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.common.util.IdWorker;
import com.pinyougou.common.util.RedisLock;
import com.pinyougou.mapper.SeckillGoodsMapper;
import com.pinyougou.mapper.SeckillOrderMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.seckill.service.SeckillOrderService;
import com.pinyougou.service.impl.BaseServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.List;

@Service
public class SeckillOrderServiceImpl extends BaseServiceImpl<TbSeckillOrder> implements SeckillOrderService {

    //秒杀订单在redis中的键名
    private static final String SECKILL_ORDERS = "SECKILL_ORDERS";
    @Autowired
    private SeckillOrderMapper seckillOrderMapper;

    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private IdWorker idWorker;

    @Override
    public PageInfo<TbSeckillOrder> search(Integer pageNum, Integer pageSize, TbSeckillOrder seckillOrder) {
        //设置分页
        PageHelper.startPage(pageNum, pageSize);
        //创建查询对象
        Example example = new Example(TbSeckillOrder.class);

        //创建查询条件对象
        Example.Criteria criteria = example.createCriteria();

        //模糊查询
        /**if (StringUtils.isNotBlank(seckillOrder.getProperty())) {
         criteria.andLike("property", "%" + seckillOrder.getProperty() + "%");
         }*/

        List<TbSeckillOrder> list = seckillOrderMapper.selectByExample(example);
        return new PageInfo<>(list);
    }

    @Override
    public String submitOrder(Long seckillGoodsId, String userId) throws Exception {
        //- 添加分布式锁
        RedisLock redisLock = new RedisLock(redisTemplate);
        if (redisLock.lock(seckillGoodsId.toString())) {
            //- 根据秒杀商品id查询redis中的秒杀商品
            TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps(SeckillGoodsServiceImpl.SECKILL_GOODS).get(seckillGoodsId);
            //- 判断商品是否存在，库存大于0
            if (seckillGoods == null) {
                throw new RuntimeException("商品不存在。");
            }
            if (seckillGoods.getStockCount() <= 0) {
                throw new RuntimeException("商品已经卖完了！");
            }
            //- 将秒杀商品的库存减1
            seckillGoods.setStockCount(seckillGoods.getStockCount() - 1);
            if (seckillGoods.getStockCount() > 0) {
                //  - 如果库存还是大于0则更新redis中的秒杀商品
                redisTemplate.boundHashOps(SeckillGoodsServiceImpl.SECKILL_GOODS).put(seckillGoodsId, seckillGoods);
            } else {
                //  - 如果库存小于等于0则将redis中秒杀商品删除，并更新mysql中对应的秒杀商品
                seckillGoodsMapper.updateByPrimaryKeySelective(seckillGoods);

                redisTemplate.boundHashOps(SeckillGoodsServiceImpl.SECKILL_GOODS).delete(seckillGoodsId);
            }
            //- 释放分布式锁
            redisLock.unlock(seckillGoodsId.toString());

            //- 生成秒杀订单存入redis
            TbSeckillOrder tbSeckillOrder = new TbSeckillOrder();
            String orderId = idWorker.nextId() + "";
            tbSeckillOrder.setId(orderId);
            tbSeckillOrder.setCreateTime(new Date());
            tbSeckillOrder.setMoney(seckillGoods.getCostPrice().doubleValue());
            tbSeckillOrder.setSeckillId(seckillGoodsId);
            tbSeckillOrder.setSellerId(seckillGoods.getSellerId());
            //未支付
            tbSeckillOrder.setStatus("0");
            tbSeckillOrder.setUserId(userId);

            //保存
            redisTemplate.boundHashOps(SECKILL_ORDERS).put(orderId, tbSeckillOrder);

            return orderId;
        }
        //- 返回订单id
        return null;
    }

    @Override
    public TbSeckillOrder getSeckillOrderInRedisByOrderId(String outTradeNo) {
        return (TbSeckillOrder) redisTemplate.boundHashOps(SECKILL_ORDERS).get(outTradeNo);
    }

    @Override
    public void saveSeckillOrderInRedisToDb(String outTradeNo, String transactionId) {
        //获取订单
        TbSeckillOrder seckillOrder = getSeckillOrderInRedisByOrderId(outTradeNo);

        //修改订单信息
        seckillOrder.setStatus("1");
        seckillOrder.setPayTime(new Date());

        //保存到数据库
        seckillOrderMapper.insertSelective(seckillOrder);

        //删除redis中的订单
        redisTemplate.boundHashOps(SECKILL_ORDERS).delete(outTradeNo);
    }

    @Override
    public void deleteSeckillOrderByOutTradeNo(String outTradeNo) throws Exception {
        //- 根据订单号查询redis中的订单
        TbSeckillOrder seckillOrder = getSeckillOrderInRedisByOrderId(outTradeNo);
        //添加分布式锁
        RedisLock redisLock = new RedisLock(redisTemplate);
        if (redisLock.lock(seckillOrder.getSeckillId().toString())) {
            //- 根据订单中商品id从redis中查询秒杀商品；更新库存（加1）
            TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps(SeckillGoodsServiceImpl.SECKILL_GOODS).get(seckillOrder.getSeckillId());
            if(seckillGoods == null) {
                //  - 如果商品不存在在redis则从Mysql中查询秒杀商品
                seckillGoods = seckillGoodsMapper.selectByPrimaryKey(seckillOrder.getSeckillId());
            }
            //  - 对商品库存加1
            seckillGoods.setStockCount(seckillGoods.getStockCount()+1);

            //  - 存回redis中秒杀商品列表
            redisTemplate.boundHashOps(SeckillGoodsServiceImpl.SECKILL_GOODS).put(seckillGoods.getId(), seckillGoods);

            //释放分布式锁
            redisLock.unlock(seckillGoods.getId().toString());

            //- 删除redis中订单
            redisTemplate.boundHashOps(SECKILL_ORDERS).delete(outTradeNo);
        }
    }

}
