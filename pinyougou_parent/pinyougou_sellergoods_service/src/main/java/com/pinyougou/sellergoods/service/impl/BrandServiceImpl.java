package com.pinyougou.sellergoods.service.impl;


import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbBrandMapper;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.pojo.TbBrandExample;
import com.pinyougou.sellergoods.service.BrandService;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

@Service
public class BrandServiceImpl implements BrandService {

    @Autowired
    private TbBrandMapper brandMapper;

    @Override
    public List<TbBrand> findAll() {

        return brandMapper.selectByExample(null);
    }
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum,pageSize);
        Page page = (Page)brandMapper.selectByExample(null);
        return new PageResult(page.getTotal(),page.getResult());
    }
    @Override
    public PageResult search(int pageNum, int pageSize, TbBrand tbBrand) {
        PageHelper.startPage(pageNum,pageSize);

        TbBrandExample example = new TbBrandExample();
        TbBrandExample.Criteria criteria = example.createCriteria();
        if (StringUtils.isNotEmpty(tbBrand.getName())){
            criteria.andNameLike("%"+tbBrand.getName()+"%");
        }
        if(StringUtils.isNotEmpty(tbBrand.getFirstChar())){
            criteria.andFirstCharEqualTo(tbBrand.getFirstChar());
        }
        Page page = (Page)brandMapper.selectByExample(example);
        return new PageResult(page.getTotal(),page.getResult());
    }

    @Override
    public List<Map> findBrandList() {
        return brandMapper.findBrandList();
    }

    @Override
    public List<Map> showChart() {
        return  brandMapper.showChart();
    }

    @Override
    public void add(TbBrand tbBrand) {
         brandMapper.insert(tbBrand);
    }

    @Override
    public TbBrand findOneToUpdate(Long id) {
        return brandMapper.selectByPrimaryKey(id);
    }

    @Override
    public void update(TbBrand tbBrand) {
         brandMapper.updateByPrimaryKey(tbBrand);
    }



    @Override
    public void dele(Long[] ids) {
        for (Long id : ids) {
            brandMapper.deleteByPrimaryKey(id);
        }
    }



}
