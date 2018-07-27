package com.pinyougou.sellergoods.service;

import com.pinyougou.pojo.TbSpecification;
import entity.PageResult;
import entityGroup.Specification;

import java.util.List;
import java.util.Map;

public interface SpecificationService {

    public List<TbSpecification> findAll();

    PageResult findPage(int pageNum, int pageSize);

    void add(Specification tbBrand);

    Specification findOneToUpdate(Long id);

    void update(Specification tbBrand);

    void dele(Long[] ids);

    PageResult search(int pageNum, int pageSize, TbSpecification tbBrand);

    List<Map> findSpecList();

}
