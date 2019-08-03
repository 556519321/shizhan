package com.pinyougou.search.activemq.listener;

import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.listener.adapter.AbstractAdaptableMessageListener;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.util.List;
import java.util.Map;

/**
 * 接收item_es_queue队列中sku商品列表json格式字符串并转换为列表并保存到es
 */
public class ItemImportMessageListener extends AbstractAdaptableMessageListener {

    @Autowired
    private ItemSearchService itemSearchService;

    @Override
    public void onMessage(Message message, Session session) throws JMSException {
        //转换消息为列表
        if (message instanceof TextMessage) {
            TextMessage textMessage = (TextMessage) message;

            List<TbItem> itemList = JSON.parseArray(textMessage.getText(), TbItem.class);

            for (TbItem tbItem : itemList) {
                Map specMap = JSON.parseObject(tbItem.getSpec(), Map.class);
                tbItem.setSpecMap(specMap);
            }

            itemSearchService.importItemList(itemList);
        }
    }
}
