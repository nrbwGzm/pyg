package com.pinyougou.dataInit;

import com.alibaba.fastjson.JSON;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.SolrDataQuery;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
//既要分词检索,又要操作数据库,所以需要同时加载两个配置文件 applicationContext-dao.xml  applicationContext-solr.xml
// classpath* : 当前项目及所依赖的项目下的resources文件夹
@ContextConfiguration(locations = "classpath*:spring/applicationContext*.xml")
//ContextConfiguration:环境配置
public class SolrManager {

    @Autowired
    private SolrTemplate solrTemplate;

    @Test
    public  void testAdd(){
        TbItem item = new TbItem();
        item.setId(1l);
        item.setTitle("测试标题");
        item.setGoodsId(12121l);
        item.setSeller("大老王");
        item.setBrand("小米");
        //new BigDecimal 小数
        item.setPrice(new BigDecimal(2999.99));
        item.setImage("hasgdaf");
        item.setCategory("手机");
        //saveOrUpdate 即可以新增保存,也可以修改
        solrTemplate.saveBean(item);
        solrTemplate.commit();
    }

    @Test
    public void testdelete(){
//        solrTemplate.deleteById("4");
//        List<String> list = new ArrayList<String>();
//        list.add("2");
//        list.add("3");
//        solrTemplate.deleteById(list);

        SolrDataQuery query = new SimpleQuery("*:*");//查询所有
        solrTemplate.delete(query);//删除所有

        solrTemplate.commit();

    }

    @Autowired
    private TbItemMapper itemMapper;
    @Test
    public void initSolrData(){
        List<TbItem> items = itemMapper.selectMarketItems();

        for (TbItem item : items) {
            item.setSpecMap(JSON.parseObject(item.getSpec(), Map.class));
        }
        solrTemplate.saveBeans(items);
        solrTemplate.commit();
    }
}


