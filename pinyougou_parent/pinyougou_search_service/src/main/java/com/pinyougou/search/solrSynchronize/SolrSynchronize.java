package com.pinyougou.search.solrSynchronize;

import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.List;

public class SolrSynchronize implements MessageListener{

    @Autowired
    private TbItemMapper itemMapper;

    @Autowired
    private SolrTemplate solrTemplate;
    @Override
    public void onMessage(Message message) {

        TextMessage textMessage = (TextMessage) message;
        try {
            String goodsId = textMessage.getText();
            TbItemExample example = new TbItemExample();
            example.createCriteria().andGoodsIdEqualTo(Long.parseLong(goodsId));//goodsId传递的时候是String类型的,转成Long
            List<TbItem> items = itemMapper.selectByExample(example);
            solrTemplate.saveBeans(items);

            solrTemplate.commit();
            System.out.println("SolrSynchronize is success");


        } catch (Exception e) {

          e.printStackTrace();
        }

    }


}
