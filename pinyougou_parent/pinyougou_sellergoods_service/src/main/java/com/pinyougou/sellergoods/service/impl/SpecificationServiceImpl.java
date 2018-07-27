package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbSpecificationMapper;
import com.pinyougou.mapper.TbSpecificationOptionMapper;
import com.pinyougou.pojo.TbSpecification;
import com.pinyougou.pojo.TbSpecificationExample;
import com.pinyougou.pojo.TbSpecificationOption;
import com.pinyougou.pojo.TbSpecificationOptionExample;
import com.pinyougou.sellergoods.service.SpecificationService;
import entity.PageResult;
import entityGroup.Specification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class SpecificationServiceImpl implements SpecificationService {

    @Autowired
    private TbSpecificationMapper specificationMapper;
    @Autowired
    private TbSpecificationOptionMapper specificationOptionMapper;

    @Override
    public List<TbSpecification> findAll() {

        return specificationMapper.selectByExample(null);
    }
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum,pageSize);
        Page page = (Page)specificationMapper.selectByExample(null);
        return new PageResult(page.getTotal(),page.getResult());
    }
    @Override
    public PageResult search(int pageNum, int pageSize, TbSpecification tbSpecification) {
        PageHelper.startPage(pageNum,pageSize);
        TbSpecificationExample example = new TbSpecificationExample();
        if (StringUtils.isNotEmpty(tbSpecification.getSpecName())) {
            example.createCriteria().andSpecNameLike("%" + tbSpecification.getSpecName() + "%");
        }
        Page page = (Page)specificationMapper.selectByExample(example);
        return new PageResult(page.getTotal(),page.getResult());
    }

    @Override
    public List<Map> findSpecList() {
        return specificationMapper.findSpecList();
    }

    @Override
    public void add(Specification specification) {
        TbSpecification tbSpecification = specification.getTbSpecification();
        specificationMapper.insert(tbSpecification);


        List<TbSpecificationOption> specificationOptionList = specification.getSpecificationOptionList();
        for (TbSpecificationOption option:specificationOptionList){
            option.setSpecId(tbSpecification.getId());
            specificationOptionMapper.insert(option);
        }

    }

    @Override
    public Specification findOneToUpdate(Long id) {
        TbSpecification tbSpecification = specificationMapper.selectByPrimaryKey(id);

        TbSpecificationOptionExample example = new TbSpecificationOptionExample();
        example.createCriteria().andSpecIdEqualTo(id);
//        select * from tb_specification_option where spec_id=?
        List<TbSpecificationOption> specificationOptionList = specificationOptionMapper.selectByExample(example);

        //new 一个组合类
        Specification specification = new Specification();
        specification.setSpecificationOptionList(specificationOptionList);
        specification.setTbSpecification(tbSpecification);

        return  specification;
    }

    @Override
    public void update(Specification specification) {

        TbSpecification tbSpecification = specification.getTbSpecification();
        specificationMapper.updateByPrimaryKey(tbSpecification);

//        关于规格项：先删除再新增
//        delete from tb_specification_option where spec_id=?
        TbSpecificationOptionExample example = new TbSpecificationOptionExample();
        example.createCriteria().andSpecIdEqualTo(tbSpecification.getId());
        specificationOptionMapper.deleteByExample(example);

        List<TbSpecificationOption> specificationOptionList = specification.getSpecificationOptionList();
        for (TbSpecificationOption option:specificationOptionList){
            option.setSpecId(tbSpecification.getId());
            specificationOptionMapper.insert(option);
        }
         //specificationMapper.updateByPrimaryKey(Specification);
    }



    @Override
    public void dele(Long[] ids) {
        for (Long id : ids) {
            specificationMapper.deleteByPrimaryKey(id);
//             真正项目中应该使用逻辑删除  is_delete 0:正常  1：删除
//            delete from tb_specification where id=?
//            delete from tb_specification_option where spec_id=?
            TbSpecificationOptionExample example = new TbSpecificationOptionExample();
            example.createCriteria().andSpecIdEqualTo(id);
            specificationOptionMapper.deleteByExample(example);


        }
    }



}
