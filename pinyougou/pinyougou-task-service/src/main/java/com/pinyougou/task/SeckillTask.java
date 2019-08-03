package com.pinyougou.task;

import com.pinyougou.mapper.SeckillGoodsMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Component
public class SeckillTask {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    /**
     * 更新秒杀商品
     */
    @Scheduled(cron = "0/2 * * * * ?")
    public void refreshSeckillGoods() {
        //- 获取当前在首页的那些秒杀商品id数组
        Set seckillIdsSet = redisTemplate.boundHashOps("SECKILL_GOODS").keys();
        List idsList = new ArrayList(seckillIdsSet);

        /**
         * --不在redis中的那些库存大于0，已审核，开始时间小于等于当前时间，结束时间大于当前时间的秒杀商品
         * select * from tb_seckill_goods where status='1' and stock_count>0 and start_time <=? and end_time>? and id not in(?,秒杀商品id,?)
         */
        //- 查询符合条件的数据
        Example example = new Example(TbSeckillGoods.class);
        Example.Criteria criteria = example.createCriteria();

        //已审核
        criteria.andEqualTo("status", "1");

        //库存大于0
        criteria.andGreaterThan("stockCount", 0);

        //开始时间小于等于当前时间
        criteria.andLessThanOrEqualTo("startTime", new Date());

        //结束时间大于当前时间
        criteria.andGreaterThan("endTime", new Date());

        if (idsList != null && idsList.size() > 0) {
            criteria.andNotIn("id", idsList);
        }

        List<TbSeckillGoods> seckillGoodsList = seckillGoodsMapper.selectByExample(example);

        //- 逐个遍历并设置到redis中
        if (seckillGoodsList != null && seckillGoodsList.size() > 0) {
            for (TbSeckillGoods tbSeckillGoods : seckillGoodsList) {
                redisTemplate.boundHashOps("SECKILL_GOODS").put(tbSeckillGoods.getId(), tbSeckillGoods);

            }
            System.out.println("往缓存中存入了 " + seckillGoodsList.size() + " 个秒杀商品...");
        }
    }

    /**
     * 同步缓存中的秒杀商品，从redis移除并保存到mysql
     */
    @Scheduled(cron = "0/3 * * * * ?")
    public void deleteSeckillGoods() {
        //- 获取redis中的秒杀商品列表
        List<TbSeckillGoods> seckillGoodsList = redisTemplate.boundHashOps("SECKILL_GOODS").values();

        //- 逐个遍历商品与当前时间对比；结束小于等于当前时间的秒杀商品需要从redis中移除并更新到mysql数据库中
        if (seckillGoodsList != null && seckillGoodsList.size() > 0) {

            for (TbSeckillGoods tbSeckillGoods : seckillGoodsList) {
                if (tbSeckillGoods.getEndTime().getTime() <= System.currentTimeMillis()) {
                    //更新到mysql
                    seckillGoodsMapper.updateByPrimaryKeySelective(tbSeckillGoods);
                    //从redis移除
                    redisTemplate.boundHashOps("SECKILL_GOODS").delete(tbSeckillGoods.getId());

                    System.out.println("从缓存中移除了id为：" + tbSeckillGoods.getId() + " 的秒杀商品...");
                }
            }
        }
    }
}
