package com.pinyougou.search.solrSynchronize;

import com.pinyougou.mapper.TbItemMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.SimpleQuery;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

public class SolrSynchronizeDelete implements MessageListener {

    @Autowired
    private TbItemMapper itemMapper;

    @Autowired
    private SolrTemplate solrTemplate;

    @Override
    public void onMessage(Message message) {

        TextMessage textMessage = (TextMessage) message;
        try {
            String goodsId = textMessage.getText();
//            这种方法是从数据库中查询到ID再删除
//            TbItemExample example = new TbItemExample();
//            example.createCriteria().andGoodsIdEqualTo(Long.parseLong(goodsId));
//            List<TbItem> items = itemMapper.selectByExample(example);
//
//            for (TbItem item : items) {
//                solrTemplate.deleteById(item.getId()+"");
//            }
//            而这种方法直接从solr索引库查询 item_goodsid为goodsId的数据,再进行删除,效率更高
            SimpleQuery solrQuery = new SimpleQuery("item_goodsid:" + goodsId);
            solrTemplate.delete(solrQuery);

            solrTemplate.commit();
            System.out.println("SolrSynchronize is success");

        } catch (Exception e) {

            e.printStackTrace();
        }

    }

}
