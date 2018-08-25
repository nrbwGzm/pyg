package com.pinyougou.sellergoods.service;

import com.pinyougou.pojo.TbBrand;
import entity.PageResult;

import java.util.List;
import java.util.Map;

public interface BrandService {

    public List<TbBrand> findAll();

    PageResult findPage(int pageNum, int pageSize);

    void add(TbBrand tbBrand);

    void update(TbBrand tbBrand);

    TbBrand findOneToUpdate(Long id);

    void dele(Long[] ids);

    PageResult search(int pageNum, int pageSize, TbBrand tbBrand);

    List<Map> findBrandList();

    List<Map> showChart();

}
