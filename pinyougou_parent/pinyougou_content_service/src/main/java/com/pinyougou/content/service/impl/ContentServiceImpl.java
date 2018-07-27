package com.pinyougou.content.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.content.service.ContentService;
import com.pinyougou.mapper.TbContentMapper;
import com.pinyougou.pojo.TbContent;
import com.pinyougou.pojo.TbContentExample;
import com.pinyougou.pojo.TbContentExample.Criteria;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service
public class ContentServiceImpl implements ContentService {

    @Autowired
    private TbContentMapper contentMapper;

    /**
     * 查询全部
     */
    @Override
    public List<TbContent> findAll() {
        return contentMapper.selectByExample(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<TbContent> page = (Page<TbContent>) contentMapper.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 增加
     */
    @Override
    public void add(TbContent content) {
        //		同步更新redis,每次添加数据,清空redis缓存,让redis去重新获取
        redisTemplate.boundHashOps("contentList").delete(content.getCategoryId());
        contentMapper.insert(content);
    }

    /**
     * 修改
     * redis要考虑的一种情况:广告的类型从轮播类型,变更成了今日推荐
     */
    @Override
    public void update(TbContent content){
//		同步更新redis
        redisTemplate.boundHashOps("contentList").delete(content.getCategoryId());

        Long categoryId = contentMapper.selectByPrimaryKey(content.getId()).getCategoryId();
        // tbContent  修改之前的数据 类型还是保持之前的类型

        contentMapper.updateByPrimaryKey(content);
//这里categoryId数值的类型是包装类Long,而不是小long,如果数值大于127,它就走地址了,无法进行判断
        if(content.getCategoryId().longValue()!=categoryId.longValue()){  //判断类型是否更改
            redisTemplate.boundHashOps("contentList").delete(categoryId);
        }
    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public TbContent findOne(Long id) {
        return contentMapper.selectByPrimaryKey(id);
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            //先通过id获取tbContent,再通过tbContent获取分类id,进行删除
            TbContent tbContent = contentMapper.selectByPrimaryKey(id);
            redisTemplate.boundHashOps("contentList").delete(tbContent.getCategoryId());
            //注意:上两行代码必须在下面这行代码的上面,不然都已经删除了,就无法获取id了
            contentMapper.deleteByPrimaryKey(id);
        }
    }

    @Override
    public PageResult findPage(TbContent content, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        TbContentExample example = new TbContentExample();
        Criteria criteria = example.createCriteria();

        if (content != null) {
            if (content.getTitle() != null && content.getTitle().length() > 0) {
                criteria.andTitleLike("%" + content.getTitle() + "%");
            }
            if (content.getUrl() != null && content.getUrl().length() > 0) {
                criteria.andUrlLike("%" + content.getUrl() + "%");
            }
            if (content.getPic() != null && content.getPic().length() > 0) {
                criteria.andPicLike("%" + content.getPic() + "%");
            }
            if (content.getStatus() != null && content.getStatus().length() > 0) {
                criteria.andStatusLike("%" + content.getStatus() + "%");
            }
        }
        Page<TbContent> page = (Page<TbContent>) contentMapper.selectByExample(example);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Autowired
//    用reids缓存来加载图片
    private RedisTemplate redisTemplate;

    @Override
    public List<TbContent> findByCategoryId(Long categoryId) {
        //		redisTemplate.boundHashOps("contentList").put(categoryId,contentList);
        List<TbContent> list = (List<TbContent>) redisTemplate.boundHashOps("contentList").get(categoryId);
        if (list == null || list.size() == 0) { //当前没有缓存,数据从数据库获取
            System.out.println("当前没有缓存,数据从mysql数据库获取");
            //select * from tb_content where categoryId=?
            TbContentExample example = new TbContentExample();
            example.createCriteria().andCategoryIdEqualTo(categoryId).andStatusEqualTo("1");
            example.setOrderByClause("sort_order");//以这个表中的此字段来排序;
            list = contentMapper.selectByExample(example);
            redisTemplate.boundHashOps("contentList").put(categoryId,list);
        } else {
            System.out.println("当前有缓存,数据从redis获取");

        }
        return list;
    }

}
