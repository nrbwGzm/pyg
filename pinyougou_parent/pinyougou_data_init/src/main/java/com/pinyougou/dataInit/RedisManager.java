package com.pinyougou.dataInit;

import com.alibaba.fastjson.JSON;
import com.pinyougou.mapper.TbItemCatMapper;
import com.pinyougou.mapper.TbSpecificationOptionMapper;
import com.pinyougou.mapper.TbTypeTemplateMapper;
import com.pinyougou.pojo.TbItemCat;
import com.pinyougou.pojo.TbSpecificationOption;
import com.pinyougou.pojo.TbSpecificationOptionExample;
import com.pinyougou.pojo.TbTypeTemplate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath*:spring/applicationContext*.xml")
public class RedisManager {

//    初始化分类和品牌   分类和规格的数据

    @Autowired
    private TbItemCatMapper itemCatMapper;

    @Autowired
    private TbTypeTemplateMapper tbTypeTemplateMapper;
    @Autowired
    private TbSpecificationOptionMapper specificationOptionMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void initRedis(){
//        查询所有分类  缓存的是所有分类和品牌 所有分类和规格
//          分类和品牌规格中间通过规格
//        查询所有分类
        List<TbItemCat> tbItemCats = itemCatMapper.selectByExample(null);//条件为null直接查询所有
        for (TbItemCat tbItemCat : tbItemCats) {
            //通过分类得到模板id,通过id查询到模板,在通过模板得到规格
            TbTypeTemplate tbTypeTemplate = tbTypeTemplateMapper.selectByPrimaryKey(tbItemCat.getTypeId());
//           [{"id":30,"text":"劲霸男装"},{"id":26,"text":"海澜之家"}]
            List<Map> brandList = JSON.parseArray(tbTypeTemplate.getBrandIds(), Map.class);
//            缓存的是所有分类和品牌
            redisTemplate.boundHashOps("brandList").put(tbItemCat.getName(),brandList);

//            [{"id":26,"text":"尺码"},{"id":35,"text":"腰围"}]
            String specIds = tbTypeTemplate.getSpecIds();
            List<Map> specList = JSON.parseArray(specIds, Map.class);
            for (Map map : specList) {
                TbSpecificationOptionExample example = new TbSpecificationOptionExample();
                example.createCriteria().andSpecIdEqualTo(Long.parseLong(map.get("id")+""));
                List<TbSpecificationOption> specificationOptionList = specificationOptionMapper.selectByExample(example);// select * from specificationOption where specid=?
                map.put("options",specificationOptionList);
            }
//            缓存的是所有分类和规格
            redisTemplate.boundHashOps("specList").put(tbItemCat.getName(),specList);

        }
        System.out.println("success");
    }
}
