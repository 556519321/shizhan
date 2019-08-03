package com.pinyougou.seckill.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.mapper.SeckillGoodsMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.seckill.service.SeckillGoodsService;
import com.pinyougou.service.impl.BaseServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.List;

@Service
public class SeckillGoodsServiceImpl extends BaseServiceImpl<TbSeckillGoods> implements SeckillGoodsService {

    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    // 保存到redis中的秒杀商品列表对应的键名
    public static final String SECKILL_GOODS = "SECKILL_GOODS";

    @Override
    public PageInfo<TbSeckillGoods> search(Integer pageNum, Integer pageSize, TbSeckillGoods seckillGoods) {
        //设置分页
        PageHelper.startPage(pageNum, pageSize);
        //创建查询对象
        Example example = new Example(TbSeckillGoods.class);

        //创建查询条件对象
        Example.Criteria criteria = example.createCriteria();

        //模糊查询
        /**if (StringUtils.isNotBlank(seckillGoods.getProperty())) {
            criteria.andLike("property", "%" + seckillGoods.getProperty() + "%");
        }*/

        List<TbSeckillGoods> list = seckillGoodsMapper.selectByExample(example);
        return new PageInfo<>(list);
    }

    @Override
    public List<TbSeckillGoods> findList() {
        List<TbSeckillGoods> seckillGoodsList = null;

        //从redis中查询秒杀商品列表
        seckillGoodsList = redisTemplate.boundHashOps(SECKILL_GOODS).values();

        if(seckillGoodsList == null || seckillGoodsList.size() == 0) {

            //-- 库存量大于0，已经审核，在秒杀期间（开始时间小于等于当前时间，结束时间大于当前时间），根据开始时间升序排序
            //select * from tb_seckill_goods where stock_count > 0 and status == '1' and start_time<=? and ?<end_time order by start_time

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

            //根据开始时间排序
            example.orderBy("startTime");

            seckillGoodsList = seckillGoodsMapper.selectByExample(example);


            //将秒杀商品存入redis
            if (seckillGoodsList != null && seckillGoodsList.size() > 0) {
                for (TbSeckillGoods tbSeckillGoods : seckillGoodsList) {
                    redisTemplate.boundHashOps(SECKILL_GOODS).put(tbSeckillGoods.getId(), tbSeckillGoods);
                }
            }
        } else {
            System.out.println("秒杀商品来自缓存...");
        }

        return seckillGoodsList;
    }

    @Override
    public TbSeckillGoods findOneInRedis(Long id) {
        return (TbSeckillGoods) redisTemplate.boundHashOps(SECKILL_GOODS).get(id);
    }

}
