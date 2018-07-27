package com.pinyougou.freemarker.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.sellergoods.service.GoodsService;
import entityGroup.Goods;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/item")
public class ItemFreemarkerController {
    @Autowired
    private FreeMarkerConfigurer freemarkerConfig;
    @Reference
    private GoodsService goodsService;


//    启动sellergoods_service和freemarker_web服务器,浏览器输入下列网址直接生成静态页面
//    http://localhost:8085/item/generatorToHtml/要生成的item(商品)的goodsId

    @RequestMapping("/generatorToHtml/{goodsId}")
    public String generatorToHtml(@PathVariable("goodsId") Long goodsId) {
        try {
//       创建一个 Configuration 对象，直接 new 一个对象。构造方法的参数就是 freemarker 的版本号
            Configuration configuration = freemarkerConfig.getConfiguration();
//            通过创建模板对象
            Template template = configuration.getTemplate("item.ftl");
//          准备数据集(将goodsService.findOne的返回值修改为Goods组合类)
//            获得组合类
            Goods goods = goodsService.findOne(goodsId);
//            获得组合类中的属性 List<TbItem> itemList
            List<TbItem> itemList = goods.getItemList();
            for (TbItem tbItem : itemList) {
//          创建一个 Writer 对象，一般创建一 FileWriter 对象，指定生成的路径及文件名。
                FileWriter writerOut = new FileWriter(new File("D:\\pyg\\html\\" + tbItem.getId() + ".html"));
//               1、三级分类  2、spu 3、当前正在循环的sku
                Map map = new HashMap();
                map.put("goods", goods);
                map.put("item", tbItem);
//              调用模板对象的 process 方法输出文件(参数1:数据模型,参数2:输出流)4
//                将map中的数据与模板中的Freemarker指令一一对应
                template.process(map, writerOut);
//              关闭流
                writerOut.close();
            }
            return "success";//返回字符串,显示成功或者失败
        } catch (Exception e) {
            e.printStackTrace();
            return "fail";  //fail 失败
        }
    }

    @RequestMapping("/generatorToHtmlAll")
    public String generatorToHtmlAll(){
        try {
//       创建一个 Configuration 对象，直接 new 一个对象。构造方法的参数就是 freemarker 的版本号
            Configuration configuration = freemarkerConfig.getConfiguration();
            Template template =configuration.getTemplate("item.ftl");


            List<Goods> list = goodsService.findAllGoods();
            for (Goods goods : list) {

//          准备数据集
//            Goods goods = goodsService.findOne(goodsId);

                List<TbItem> itemList = goods.getItemList();
                for (TbItem tbItem : itemList) {
//          创建一个 Writer 对象，一般创建一 FileWriter 对象，指定生成的文件名。
                    FileWriter writer = new FileWriter(new File("D:\\pyg\\html\\" + tbItem.getId() + ".html"));
//               1、三级分类  2、spu 3、当前正在循环的sku
//              调用模板对象的 process 方法输出文件。
                    Map map = new HashMap();
                    map.put("goods",goods);
                    map.put("item",tbItem);
                    template.process(map,writer);
//              关闭流
                    writer.close();
                }
            }

            return "success";
        } catch (Exception e) {
            e.printStackTrace();
            return "fail";
        }

    }
}
