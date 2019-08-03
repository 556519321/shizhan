package com.pinyougou.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageInfo;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.sellergoods.service.GoodsService;
import com.pinyougou.vo.Goods;
import com.pinyougou.vo.Result;
import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.jms.*;
import java.util.List;

@RequestMapping("/goods")
@RestController
public class GoodsController {

    @Reference
    private GoodsService goodsService;

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private Destination itemEsQueue;

    @Autowired
    private Destination itemEsDeleteQueue;
    @Autowired
    private Destination itemTopic;
    @Autowired
    private Destination itemDeleteTopic;

    /**
     * 新增
     * @param goods 商品vo；包含：商品基本、描述、SKU列表
     * @return 操作结果
     */
    @PostMapping("/add")
    public Result add(@RequestBody Goods goods){
        try {
            //获取当前登录用户就是商家
            String sellerId = SecurityContextHolder.getContext().getAuthentication().getName();
            goods.getGoods().setSellerId(sellerId);
            goodsService.addGoods(goods);

            return Result.ok("新增成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.fail("新增失败");
    }

    /**
     * 根据主键查询
     * @param id 主键；spu id
     * @return 商品vo：基本、描述、sku列表
     */
    @GetMapping("/findOne/{id}")
    public Goods findOne(@PathVariable Long id){
        return goodsService.findGoodsById(id);
    }

    /**
     * 修改
     * @param goods 商品vo；包含：商品基本、描述、SKU列表
     * @return 操作结果
     */
    @PostMapping("/update")
    public Result update(@RequestBody Goods goods){
        try {
            //判断商家是否合法
            //查询原来该商品的商家
            TbGoods oldGoods = goodsService.findOne(goods.getGoods().getId());
            //当前商家
            String sellerId = SecurityContextHolder.getContext().getAuthentication().getName();
            if (!sellerId.equals(oldGoods.getSellerId())) {
                return Result.fail("操作非法！");
            }
            goodsService.updateGoods(goods);
            return Result.ok("修改成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.fail("修改失败");
    }

    /**
     * 根据主键数组批量删除
     * @param ids 主键数组
     * @return 实体
     */
    @GetMapping("/delete")
    public Result delete(Long[] ids){
        try {
            //goodsService.deleteByIds(ids);
            goodsService.deleteGoodsByIds(ids);
            //同步搜索系统数据
            //itemSearchService.deleteItemByIds(ids);
            sendMQMsg(itemEsDeleteQueue, ids);
            //发送商品被删除的MQ消息到主题
            sendMQMsg(itemDeleteTopic, ids);
            return Result.ok("删除成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.fail("删除失败");
    }

    /**
     * 发送消息到activeMQ的某个模式
     * @param destination 模式（队列或主题）
     * @param ids 商品spu id数组
     */
    private void sendMQMsg(Destination destination, Long[] ids) {
        jmsTemplate.send(destination, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                ObjectMessage objectMessage = session.createObjectMessage();
                objectMessage.setObject(ids);

                return objectMessage;
            }
        });
    }

    /**
     * 根据条件搜索
     * @param pageNum 页号
     * @param pageSize 页面大小
     * @param goods 搜索条件
     * @return 分页信息
     */
    @PostMapping("/search")
    public PageInfo<TbGoods> search(@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
                             @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
                           @RequestBody TbGoods goods) {
        return goodsService.search(pageNum, pageSize, goods);
    }

    /**
     * 批量更新商品spu的审核状态
     * @param ids 商品spu id数组
     * @param status 要更新的商品审核状态
     * @return 操作结果
     */
    @GetMapping("/updateStatus")
    public Result updateStatus(Long[] ids, String status){
        try {
            goodsService.updateStatus(status, ids);
            if ("2".equals(status)) {
                //根据spu id数组获取到对应的sku商品列表；select * from tb_item where status='1' and goods_id in(?,?...)
                List<TbItem> itemList = goodsService.findItemListByGoodsIds(ids);
                //同步搜索系统的商品数据
                //itemSearchService.importItemList(itemList);
                jmsTemplate.send(itemEsQueue, new MessageCreator() {
                    @Override
                    public Message createMessage(Session session) throws JMSException {
                        TextMessage textMessage = session.createTextMessage();
                        textMessage.setText(JSON.toJSONString(itemList));
                        return textMessage;
                    }
                });
                //发送商品被审核通过的MQ消息到主题
                sendMQMsg(itemTopic, ids);
            }
            return Result.ok("更新状态成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.fail("更新状态失败");
    }

}
