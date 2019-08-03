package com.pinyougou.item.activemq.listener;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbItemCat;
import com.pinyougou.sellergoods.service.GoodsService;
import com.pinyougou.sellergoods.service.ItemCatService;
import com.pinyougou.vo.Goods;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.listener.adapter.AbstractAdaptableMessageListener;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import java.io.FileWriter;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 接收商品spu id数组遍历根据每个spu id查询商品vo商品3级分类中文名称
 * 利用freemarker生成静态页面到指定路径
 */
public class TopicMessageListener extends AbstractAdaptableMessageListener {
    //注入freemarker配置对象
    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;

    @Reference
    private GoodsService goodsService;

    @Reference
    private ItemCatService itemCatService;

    //静态页面放置的路径
    @Value("${ITEM_HTML_PATH}")
    private String ITEM_HTML_PATH;

    @Override
    public void onMessage(Message message, Session session) throws JMSException {
        if (message instanceof ObjectMessage) {
            ObjectMessage objectMessage = (ObjectMessage) message;
            //商品spu id数组
            Long[] goodsIds = (Long[]) objectMessage.getObject();
            try {
                if (goodsIds != null && goodsIds.length > 0) {
                    for (Long goodsId : goodsIds) {
                        genHtml(goodsId);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void genHtml(Long goodsId) throws Exception {
        Configuration configuration = freeMarkerConfigurer.getConfiguration();
        //模板
        Template template = configuration.getTemplate("item.ftl");
        //数据
        Map<String, Object> dataModel = new HashMap<>();

        //根据商品spu id查询商品基本、描述、sku列表(已启用并按照默认字段降序排序)
        Goods goods = goodsService.findGoodsByIdAndStatus(goodsId, "1");

        //goods 商品基本信息
        dataModel.put("goods", goods.getGoods());
        //goodsDesc 商品描述信息
        dataModel.put("goodsDesc", goods.getGoodsDesc());
        //itemList 商品sku列表；（已启用；需要根据是否默认排序，将默认的sku排在列表中第一个）
        dataModel.put("itemList", goods.getItemList());
        //itemCat1 商品第1级商品分类中文名称
        TbItemCat itemCat1 = itemCatService.findOne(goods.getGoods().getCategory1Id());
        dataModel.put("itemCat1", itemCat1.getName());
        //itemCat2 商品第2级商品分类中文名称
        TbItemCat itemCat2 = itemCatService.findOne(goods.getGoods().getCategory2Id());
        dataModel.put("itemCat2", itemCat2.getName());
        //itemCat3 商品第3级商品分类中文名称
        TbItemCat itemCat3 = itemCatService.findOne(goods.getGoods().getCategory3Id());
        dataModel.put("itemCat3", itemCat3.getName());

        //输出路径
        String fileName = ITEM_HTML_PATH + goodsId + ".html";
        FileWriter fileWriter = new FileWriter(fileName);

        template.process(dataModel, fileWriter);
        fileWriter.close();
    }

}
