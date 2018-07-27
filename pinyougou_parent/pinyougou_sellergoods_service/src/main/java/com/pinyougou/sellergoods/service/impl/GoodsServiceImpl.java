package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.*;
import com.pinyougou.pojo.*;
import com.pinyougou.pojo.TbGoodsExample.Criteria;
import com.pinyougou.sellergoods.service.GoodsService;
import entity.PageResult;
import entityGroup.Goods;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import java.util.*;

/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service
public class GoodsServiceImpl implements GoodsService {

    @Autowired
    private TbGoodsMapper goodsMapper;
    @Autowired
    private TbGoodsDescMapper goodsDescMapper;
    @Autowired
    private TbItemMapper itemMapper;
    @Autowired
    private TbItemCatMapper itemCatMapper;
    @Autowired
    private TbBrandMapper brandMapper;
    @Autowired
    private TbSellerMapper sellerMapper;

    /**
     * 查询全部
     */
    @Override
    public List<TbGoods> findAll() {
        return goodsMapper.selectByExample(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<TbGoods> page = (Page<TbGoods>) goodsMapper.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 增加
     */
    @Override
    public void add(Goods goods) {
        //从组合类中获取 tbGoods属性类
        TbGoods tbGoods = goods.getTbGoods();
        tbGoods.setAuditStatus("0");    //设置商品状态
        tbGoods.setIsMarketable("0");
        goodsMapper.insert(tbGoods);
        //从组合类中获取 tbGoodsDesc属性类
        TbGoodsDesc tbGoodsDesc = goods.getTbGoodsDesc();
        tbGoodsDesc.setGoodsId(tbGoods.getId());//设置商品所属商家id
        goodsDescMapper.insert(tbGoodsDesc);

        if (tbGoods.getIsEnableSpec().equals("1")) {    //是否启用规格
            List<TbItem> itemList = goods.getItemList();

            for (TbItem tbItem : itemList) {

                String title = tbGoods.getGoodsName();
                Map<String, String> map = JSON.parseObject(tbItem.getSpec(), Map.class);
                for (String key : map.keySet()) {
                    title += " " + key + map.get(key);
                }
                //设置标题	spu+" "+spec.Name+spec.Value+spec.Name+spec.Value+...
                tbItem.setTitle(title);
                //			 title = "小米6X 网络移动3G	机身内存16G	手机屏幕尺寸4.5寸	像素	1366*768"
                tbItem = createTbItem(tbItem, tbGoods, tbGoodsDesc);

                itemMapper.insert(tbItem);
            }
        } else {
//			不启用规格 也需要保存新增的除规格外的商品信息
            TbItem tbItem = new TbItem();
            tbItem.setTitle(tbGoods.getGoodsName());
            tbItem = createTbItem(tbItem, tbGoods, tbGoodsDesc);
            tbItem.setNum(9999);
            tbItem.setPrice(tbGoods.getPrice());
            tbItem.setSpec("{}");
            tbItem.setIsDefault("1");
            itemMapper.insert(tbItem);
        }
    }

    //抽取出创建商品方法,用于保存
    private TbItem createTbItem(TbItem tbItem, TbGoods tbGoods, TbGoodsDesc tbGoodsDesc) {
        tbItem.setSellPoint(tbGoods.getCaption());//			 sell_point 取副标题
//			 image 默认是spu商品图片的第一个 前提是有图片

//			[{"color":"红色","url":"http://192.168.25.133/group1/M00/00/01/wKgZhVmHINKADo__AAjlKdWCzvg874.jpg"},
//			{"color":"黑色","url":"http://192.168.25.133/group1/M00/00/01/wKgZhVmHINyAQAXHAAgawLS1G5Y136.jpg"}]
        //获取图片
        List<Map> maps = JSON.parseArray(tbGoodsDesc.getItemImages(), Map.class);
        if (maps != null && maps.size() > 0) {
            tbItem.setImage(maps.get(0).get("url") + "");
        }
//			 categoryId 第三级id
        tbItem.setCategoryid(tbGoods.getCategory3Id());
//			 `create_time`datetime NOT NULL COMMENT '创建时间',
        tbItem.setCreateTime(new Date());
//  			 `update_time`datetime NOT NULL COMMENT '更新时间',
        tbItem.setUpdateTime(new Date());
//			  goods_id	商品id
        tbItem.setGoodsId(tbGoods.getId());
//			  seller_id	商家id
        tbItem.setSellerId(tbGoods.getSellerId());
//			  `category`第三级分类名称
        tbItem.setCategory(itemCatMapper.selectByPrimaryKey(tbGoods.getCategory3Id()).getName());

//			  `brand`varchar(100) DEFAULT NULL, 品牌名称
        tbItem.setBrand(brandMapper.selectByPrimaryKey(tbGoods.getBrandId()).getName());
//			  `seller`varchar(200) DEFAULT NULL, 商家名称
        tbItem.setSeller(sellerMapper.selectByPrimaryKey(tbGoods.getSellerId()).getNickName());

        return tbItem;
    }

    /**
     * 修改
     */
    @Override
    public void update(TbGoods goods) {
        goodsMapper.updateByPrimaryKey(goods);
    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public Goods findOne(Long id) {
        Goods goods = new Goods();
        TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);

//      设置三级分类名称
        Map catMap = new HashMap();
        catMap.put("category1", itemCatMapper.selectByPrimaryKey(tbGoods.getCategory1Id()).getName());
        catMap.put("category2", itemCatMapper.selectByPrimaryKey(tbGoods.getCategory2Id()).getName());
        catMap.put("category3", itemCatMapper.selectByPrimaryKey(tbGoods.getCategory3Id()).getName());
        goods.setCatMap(catMap);

        goods.setTbGoods(tbGoods);
        //tbGoods的id和TbGoodsDesc的id一样
        TbGoodsDesc tbGoodsDesc = goodsDescMapper.selectByPrimaryKey(id);
        goods.setTbGoodsDesc(tbGoodsDesc);
        TbItemExample tbItemExample = new TbItemExample();
//      tb_item表中的goods_id字段与tb_goods表的主键id相同,为主外键关系
        tbItemExample.createCriteria().andGoodsIdEqualTo(id);
        List<TbItem> tbItemList = itemMapper.selectByExample(tbItemExample);
        goods.setItemList(tbItemList);

        return goods;
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            goodsMapper.deleteByPrimaryKey(id);
        }
    }

    @Override
    public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        TbGoodsExample example = new TbGoodsExample();
        Criteria criteria = example.createCriteria();

        if (goods != null) {
            if (goods.getSellerId() != null && goods.getSellerId().length() > 0) {
                criteria.andSellerIdEqualTo(goods.getSellerId());
            }
            if (goods.getGoodsName() != null && goods.getGoodsName().length() > 0) {
                criteria.andGoodsNameLike("%" + goods.getGoodsName() + "%");
            }
            if (goods.getAuditStatus() != null && goods.getAuditStatus().length() > 0) {
                criteria.andAuditStatusEqualTo(goods.getAuditStatus());
            }
            if (goods.getIsMarketable() != null && goods.getIsMarketable().length() > 0) {
                criteria.andIsMarketableLike("%" + goods.getIsMarketable() + "%");
            }
            if (goods.getCaption() != null && goods.getCaption().length() > 0) {
                criteria.andCaptionLike("%" + goods.getCaption() + "%");
            }
            if (goods.getSmallPic() != null && goods.getSmallPic().length() > 0) {
                criteria.andSmallPicLike("%" + goods.getSmallPic() + "%");
            }
            if (goods.getIsEnableSpec() != null && goods.getIsEnableSpec().length() > 0) {
                criteria.andIsEnableSpecLike("%" + goods.getIsEnableSpec() + "%");
            }
            if (goods.getIsDelete() != null && goods.getIsDelete().length() > 0) {
                criteria.andIsDeleteLike("%" + goods.getIsDelete() + "%");
            }

        }

        Page<TbGoods> page = (Page<TbGoods>) goodsMapper.selectByExample(example);
        return new PageResult(page.getTotal(), page.getResult());
    }

    //商品状态: 0 待审核 1 审核通过 2 审核未通过
    @Override
    public void updateAuditStatus(Long[] ids, String auditStatus) {
//	    update tb_goods set audit_Status=? where id=?
        for (Long id : ids) {
            TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
            tbGoods.setAuditStatus(auditStatus);
            goodsMapper.updateByPrimaryKey(tbGoods);
        }

    }

    @Autowired
    private JmsTemplate jmsTemplate;

	@Autowired
	@Qualifier("queue_market_solr_spu") //@Qualifier注解:区分注入相同接口的不同实现类
	private Destination queue_market_solr_spu;
	@Autowired
	@Qualifier("queue_offmarket_solr_spu")  //@Qualifier注解:区分注入相同接口的不同实现类
	private Destination queue_offmarket_solr_spu;
	@Override
	public void updateMarketable(Long[] ids, String market) {
//		market 1  上架
		for (Long id : ids) {
			if(market.equals("1")){ //如果前台传过来的market为1,就将商品上架,否则下架
				//		MQ中放消息 spu的id
				jmsTemplate.send(queue_market_solr_spu, new MessageCreator() {
					@Override
					public Message createMessage(Session session) throws JMSException {
						return session.createTextMessage(id+"");
					}
				});
				System.out.println("market success!!!!");
			}else{
				//		market 2  下架
				jmsTemplate.send(queue_offmarket_solr_spu, new MessageCreator() {
					@Override
					public Message createMessage(Session session) throws JMSException {
						return session.createTextMessage(id+"");
					}
				});
				System.out.println("market success!!!!");
			}


			TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
			tbGoods.setIsMarketable(market);
			goodsMapper.updateByPrimaryKey(tbGoods);
		}
	}


    @Override
    public List<Goods> findAllGoods() {
        List<Goods> list = new ArrayList<Goods>();
        List<TbGoods> tbGoods1 = goodsMapper.selectByExample(null);
        for (TbGoods goods : tbGoods1) {
            list.add(findOne(goods.getId()));
        }
        return list;
    }



}
