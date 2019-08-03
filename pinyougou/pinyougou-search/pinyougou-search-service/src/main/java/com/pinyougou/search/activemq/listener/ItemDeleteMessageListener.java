package com.pinyougou.search.activemq.listener;

import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.listener.adapter.AbstractAdaptableMessageListener;

import javax.jms.*;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 接收item_es_queue队列中spu id数组并删除es中对应的数据
 */
public class ItemDeleteMessageListener extends AbstractAdaptableMessageListener {

    @Autowired
    private ItemSearchService itemSearchService;

    @Override
    public void onMessage(Message message, Session session) throws JMSException {
        //转换消息为列表
        if (message instanceof ObjectMessage) {
            ObjectMessage objectMessage = (ObjectMessage) message;

            Long[] goodsIds = (Long[]) objectMessage.getObject();

            itemSearchService.deleteItemByIds(goodsIds);
        }
    }
}
