package com.pinyougou.order.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.common.util.IdWorker;
import com.pinyougou.mapper.OrderItemMapper;
import com.pinyougou.mapper.OrderMapper;
import com.pinyougou.mapper.PayLogMapper;
import com.pinyougou.pojo.TbOrder;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojo.TbPayLog;
import com.pinyougou.service.impl.BaseServiceImpl;
import com.pinyougou.vo.Cart;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Transactional
@Service
public class OrderServiceImpl extends BaseServiceImpl<TbOrder> implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private PayLogMapper payLogMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    //购物车数据在redis中的键名
    private static final String CART_LIST = "CART_LIST";

    @Autowired
    private IdWorker idWorker;


    @Override
    public PageInfo<TbOrder> search(Integer pageNum, Integer pageSize, TbOrder order) {
        //设置分页
        PageHelper.startPage(pageNum, pageSize);
        //创建查询对象
        Example example = new Example(TbOrder.class);

        //创建查询条件对象
        Example.Criteria criteria = example.createCriteria();

        //模糊查询
        /**if (StringUtils.isNotBlank(order.getProperty())) {
            criteria.andLike("property", "%" + order.getProperty() + "%");
        }*/

        List<TbOrder> list = orderMapper.selectByExample(example);
        return new PageInfo<>(list);
    }

    @Override
    public String addOrder(TbOrder order) {
        //支付日志id
        String outTradeNo = "";
        //1. 查询当前登录用户的购物车列表
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps(CART_LIST).get(order.getUserId());

        //2. 遍历购物车列表，每个购物车对象（cart）对应一个订单；遍历订单商品一个个保存到数据库表
        TbOrder tbOrder = null;
        //订单id
        String orderId = "";
        //本次支付的订单列表
        String orderIds = "";
        //本次交易要支付的总金额= 所有订单的金额之和
        double totalFee = 0.0;

        for (Cart cart : cartList) {
            tbOrder = new TbOrder();
            orderId = idWorker.nextId()+"";
            tbOrder.setOrderId(orderId);
            tbOrder.setSourceType(order.getSourceType());
            tbOrder.setUserId(order.getUserId());
            //1、未付款，2、已付款，3、未发货，4、已发货，5、交易成功，6、交易关闭,7、待评价'
            tbOrder.setStatus("1");
            tbOrder.setCreateTime(new Date());
            tbOrder.setUpdateTime(tbOrder.getCreateTime());
            tbOrder.setSellerId(cart.getSellerId());
            tbOrder.setReceiver(order.getReceiver());
            tbOrder.setReceiverMobile(order.getReceiverMobile());
            tbOrder.setReceiverAreaName(order.getReceiverAreaName());
            tbOrder.setPaymentType(order.getPaymentType());

            //本笔订单的总金额 = 所有订单商品的总支付金额之和
            double payment = 0.0;

            //保存订单商品列表
            for (TbOrderItem orderItem : cart.getOrderItemList()) {
                orderItem.setOrderId(tbOrder.getOrderId());
                orderItem.setId(idWorker.nextId());
                //累计本笔订单总金额
                payment += orderItem.getTotalFee();

                orderItemMapper.insertSelective(orderItem);
            }


            //本笔订单的总金额 = 所有订单商品的总支付金额之和
            tbOrder.setPayment(payment);

            //累加本次要支付的总金额
            totalFee += payment;

            if (orderIds.length() > 0) {
                orderIds += "," + orderId;
            } else {
                orderIds = orderId;
            }

            //保存订单
            add(tbOrder);
        }
        //3. 根据每个订单的支付总金额创建一个支付日志并保存
        TbPayLog tbPayLog = new TbPayLog();
        //支付类型
        tbPayLog.setPayType(order.getPaymentType());
        if ("1".equals(order.getPaymentType())) {
            //微信付款；未付款
            tbPayLog.setTradeState("0");
        } else {
            //货到付款；默认支付成功
            tbPayLog.setPayTime(new Date());
            //支付成功
            tbPayLog.setTradeState("1");
        }
        tbPayLog.setCreateTime(new Date());
        outTradeNo = idWorker.nextId()+"";
        tbPayLog.setOutTradeNo(outTradeNo);
        tbPayLog.setUserId(order.getUserId());

        //订单列表；逗号分隔订单编号
        tbPayLog.setOrderList(orderIds);

        //支付总金额 = 所有订单的总金额；在一般的电商中如果涉及大金额；应该是整型，单位精确到分
        tbPayLog.setTotalFee((long)(totalFee*100));

        payLogMapper.insertSelective(tbPayLog);

        //4. 删除当前登录用户的购物车列表
        redisTemplate.boundHashOps(CART_LIST).delete(order.getUserId());

        //5. 返回支付日志id
        return outTradeNo;
    }

    @Override
    public TbPayLog findPayLogByOutTradeNo(String outTradeNo) {
        return payLogMapper.selectByPrimaryKey(outTradeNo);
    }

    @Override
    public void updateOrderStatus(String outTradeNo, String transaction_id) {
        //- 根据支付日志id查支付日志；
        TbPayLog payLog = findPayLogByOutTradeNo(outTradeNo);
        //- 获取所有的订单id
        String[] orderIds = payLog.getOrderList().split(",");

        //- 更新支付日志的状态为已支付（1）
        payLog.setTradeState("1");
        payLog.setTransactionId(transaction_id);
        payLog.setPayTime(new Date());

        payLogMapper.updateByPrimaryKeySelective(payLog);

        //- 更新所有订单对应的状态为已付款（2）
        //update tb_order set status=? where order_id in (?,?)

        TbOrder order = new TbOrder();
        order.setStatus("2");
        order.setPaymentTime(new Date());
        order.setUpdateTime(order.getPaymentTime());

        Example example = new Example(TbOrder.class);
        example.createCriteria().andIn("orderId", Arrays.asList(orderIds));

        orderMapper.updateByExampleSelective(order, example);
    }

}
