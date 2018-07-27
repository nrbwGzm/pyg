package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    private SolrTemplate solrTemplate;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public Map searchFromSolr(Map paramMap) {
        //简单高亮查询
        Map resultMap = new HashMap();
//        分组查询 分类数据   手机  电脑

//        分组查询开始-----

//        设置关键字
        SimpleQuery groupQuery = new SimpleQuery();
        Criteria groupCriteria = new Criteria("item_keywords").is(paramMap.get("keywords"));
        groupQuery.addCriteria(groupCriteria);
//        设置分组属性
        GroupOptions groupOptions = new GroupOptions();
        groupOptions.addGroupByField("item_category");
        groupQuery.setGroupOptions(groupOptions);
//        select item_category from tableName where guanjianzi=? gourp by item_category
//		查询分组结果集
        GroupPage<TbItem> groupPage = solrTemplate.queryForGroupPage(groupQuery, TbItem.class);
//      遍历分组结果集,获取分组的结果
        GroupResult<TbItem> groupResult = groupPage.getGroupResult("item_category");//设置域
        Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();
        List<GroupEntry<TbItem>> content = groupEntries.getContent();//获取分组内容数据
        List<String> categoryList = new ArrayList<String>();    //new一个List<String>来接收分组内容值
        for (GroupEntry<TbItem> tbItemGroupEntry : content) {   //遍历分组内容数据
            categoryList.add(tbItemGroupEntry.getGroupValue());
            //content[""brand": "华为", goodsId": 149187842867968, ...]
            //getGroupValue():获取 "华为",149187842867968, ...
        }

        resultMap.put("categoryList", categoryList);//给Map类型的结果集中添加数据

        //从Redis缓存中取出brandList和specList数据,添加到结果集中
        if (categoryList.size() > 0) {
            List<Map> brandList = (List<Map>) redisTemplate.boundHashOps("brandList").get(categoryList.get(0));
//            brandList:[{id:1,text:''}]
            resultMap.put("brandList", brandList);
            List<Map> specList = (List<Map>) redisTemplate.boundHashOps("specList").get(categoryList.get(0));
//            specList:[{id:1,text:xxx,options:[{optionName:''}]},{id:1,text:xxx,options:[]}]
            resultMap.put("specList", specList);
        }
//        分组查询结束-----
//        ----------------------以下是高亮查询------------------------------

        SimpleHighlightQuery highlightQuery = new SimpleHighlightQuery();

        //创建高亮查询的过滤条件
        //从索引库模糊查询item_title,判断含有输入框传过来的值,进行高亮查询显示
        Criteria criteria = new Criteria("item_keywords").is(paramMap.get("keywords"));
        highlightQuery.addCriteria(criteria);
//      设置高亮属性开始
        HighlightOptions highlightOptions = new HighlightOptions();
        highlightOptions.addField("item_title");//设置域
        highlightOptions.setSimplePrefix("<span style=\"color:red\">");//前缀
        highlightOptions.setSimplePostfix("</span>");//后缀
        highlightQuery.setHighlightOptions(highlightOptions);
//        设置高亮属性结束

//        添加过滤条件 起始----------------------------------

//        添加过滤条件 brand category
        SimpleFilterQuery filterQuery = new SimpleFilterQuery();
        if (paramMap.get("category") != null && !paramMap.get("category").equals("")) {
            filterQuery.addCriteria(new Criteria("item_category").is(paramMap.get("category")));
        }
        if (paramMap.get("brand") != null && !paramMap.get("brand").equals("")) {
            filterQuery.addCriteria(new Criteria("item_brand").is(paramMap.get("brand")));
        }
//        获取前台点击事件传过来的spec.text和option.optionName
        Map<String, String> map = (Map) paramMap.get("spec");
//        循环添加规格及规格选项过滤条件
        for (String key : map.keySet()) {
            filterQuery.addCriteria(new Criteria("item_spec_" + key).is(map.get(key)));
        }
//        添加价格条件过滤
        if (paramMap.get("price") != null && !paramMap.get("price").equals("")) {
//            截取前台传来的price    ng -click = "addParamMap('price','0-500')"
            String[] prices = (paramMap.get("price") + "").split("-");
//            0-500   500-1000  3000-*
//            需要判断最高价格及无穷大
            if (!prices[1].equals("*")){
//                添加过滤条件,item_price在价格区间内   between方法的参数(最小值,最大值,是否包含最小值,是否包含最大值)
                filterQuery.addCriteria(new Criteria("item_price").between(prices[0],prices[1],true,true));
            }else {
//                greaterThanEqual 大于等于
                filterQuery.addCriteria(new Criteria("item_price").greaterThanEqual(prices[0]));
            }
        }
 //        添加过滤条件 结束----------------------------------



 //        页面显示设置 开始----------------------------------

//        显示数据,按价格排序
        if (paramMap.get("priceSort").equals("ASC")) {
            highlightQuery.addSort(new Sort(Sort.Direction.ASC, "item_price"));
        } else {
            highlightQuery.addSort(new Sort(Sort.Direction.DESC, "item_price"));
        }

//        设置分页
        int page = Integer.parseInt(paramMap.get("page") + "");//对象+空串=String,再转为int类型
//        起始位置+每页显示条数=每页要显示的60条数据,从第X(起始位置)条开始显示
        highlightQuery.setOffset((page - 1) * 60);//设置起始位置 0  60   120   180
//        点击2,传过来2,2-1=1,1X60=60,即从第60条数据开始显示,显示60条
        highlightQuery.setRows(60);//设置每页显示条数

//        页面显示设置 结束----------------------------------

//        将所有的过滤条件和显示条件添加进去
        highlightQuery.addFilterQuery(filterQuery);


        //高亮查询,得到TbItem.class类型的数据
        HighlightPage<TbItem> highlightPage = solrTemplate.queryForHighlightPage(highlightQuery, TbItem.class);
//        System.out.println(JSON.toJSONString(highlightPage));

//        返回总页数
        resultMap.put("totalPage", highlightPage.getTotalPages());
//        返回总数,用于页面全部结果显示
        resultMap.put("total", highlightPage.getTotalElements());
        List<TbItem> itemList = highlightPage.getContent();//查询出的内容数据
        for (TbItem tbItem : itemList) {
            if (highlightPage.getHighlights(tbItem) != null && highlightPage.getHighlights(tbItem).size() > 0) {
                HighlightEntry.Highlight highlight = highlightPage.getHighlights(tbItem).get(0);
                List<String> snipplets = highlight.getSnipplets();
                if (snipplets != null && snipplets.size() > 0) {
                    tbItem.setTitle(snipplets.get(0));
                }
            }
        }

        resultMap.put("itemList", itemList);//添加到结果集中

        return resultMap;//返回结果集
    }
}
