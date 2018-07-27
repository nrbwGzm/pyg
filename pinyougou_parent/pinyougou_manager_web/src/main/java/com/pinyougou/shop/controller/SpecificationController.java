package com.pinyougou.shop.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbSpecification;
import com.pinyougou.sellergoods.service.SpecificationService;
import entity.PageResult;
import entity.Result;
import entityGroup.Specification;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/specification")
public class SpecificationController {

    @Reference
    private SpecificationService specificationService;

    @RequestMapping("/findAll")
    public List<TbSpecification> findAll() {

        return specificationService.findAll();
    }
    @RequestMapping("/findSpecList")
    public List<Map> findSpecList() {
        return specificationService.findSpecList();
    }

    @RequestMapping("/findPage/{pageNum}/{pageSize}")
    public PageResult findPage(@PathVariable("pageNum") int pageNum, @PathVariable("pageSize") int pageSize) {
        return specificationService.findPage(pageNum, pageSize);
    }

    @RequestMapping("/search/{pageNum}/{pageSize}")
    //RequestBody注解的作用：
    //        把请求正文全部都获取出来。
    public PageResult search(@PathVariable("pageNum") int pageNum, @PathVariable("pageSize") int pageSize, @RequestBody TbSpecification tbSpecification) {
        return specificationService.search(pageNum, pageSize, tbSpecification);
    }

    @RequestMapping("/add")
    public Result add(@RequestBody Specification tbSpecification) {
        try {
            specificationService.add(tbSpecification);
            return new Result(true, "添加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "添加失败");
        }

    }

    @RequestMapping("/update")
    public Result update(@RequestBody Specification tbSpecification) {
        try {
            specificationService.update(tbSpecification);
            return new Result(true, "修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "修改失败");
        }
    }

    //根据id修改
    @RequestMapping("/findOneToUpdate/{id}")
    public Specification findOneToUpdate(@PathVariable("id") Long id) {
        return specificationService.findOneToUpdate(id);
    }

    //删除
    @RequestMapping("/dele/{ids}")
    public Result update(@PathVariable("ids") Long[] ids) {

        try {
            specificationService.dele(ids);
            return new Result(true, "删除成功");
//            {success:true,message:"添加成功"}
        } catch (Exception e) {
            e.printStackTrace();
//            {success:false,message:"添加失败"}
            return new Result(false, "删除失败");
        }
    }

}