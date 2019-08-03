package com.pinyougou.item.activemq.listener;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.listener.adapter.AbstractAdaptableMessageListener;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import java.io.File;

public class TopicDeleteMessageListener extends AbstractAdaptableMessageListener {

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
                        String fielname = ITEM_HTML_PATH + goodsId + ".html";
                        File file = new File(fielname);
                        if(file.exists()){
                            file.delete();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
