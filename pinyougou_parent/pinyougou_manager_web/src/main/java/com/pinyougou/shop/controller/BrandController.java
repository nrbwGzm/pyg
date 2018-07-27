package com.pinyougou.shop.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.sellergoods.service.BrandService;
import entity.PageResult;
import entity.Result;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/brand")
public class BrandController {

    @Reference
    private BrandService brandService;

    @RequestMapping("/findBrandList")
    public List<Map> findBrandList() {

        return brandService.findBrandList();
    }
    @RequestMapping("/findAll")
    public List<TbBrand> findAll() {

        return brandService.findAll();
    }

    @RequestMapping("/findPage/{pageNum}/{pageSize}")
    public PageResult findPage(@PathVariable("pageNum") int pageNum, @PathVariable("pageSize") int pageSize) {
        return brandService.findPage(pageNum, pageSize);
    }

    @RequestMapping("/search/{pageNum}/{pageSize}")
    //RequestBody注解的作用：
    //        把请求正文全部都获取出来。
    public PageResult search(@PathVariable("pageNum") int pageNum, @PathVariable("pageSize") int pageSize, @RequestBody TbBrand tbBrand) {
        return brandService.search(pageNum, pageSize, tbBrand);
    }

    @RequestMapping("/add")
    public Result add(@RequestBody TbBrand tbBrand) {
        try {
            brandService.add(tbBrand);
            return new Result(true, "添加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "添加失败");
        }

    }

    @RequestMapping("/update")
    public Result update(@RequestBody TbBrand tbBrand) {
        try {
            brandService.update(tbBrand);
            return new Result(true, "修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "修改失败");
        }
    }

    //根据id修改
    @RequestMapping("/findOneToUpdate/{id}")
    public TbBrand findOneToUpdate(@PathVariable("id") Long id) {
        return brandService.findOneToUpdate(id);
    }

    //删除
    @RequestMapping("/dele/{ids}")
    public Result update(@PathVariable("ids") Long[] ids) {

        try {
            brandService.dele(ids);
            return new Result(true, "删除成功");
//            {success:true,message:"添加成功"}
        } catch (Exception e) {
            e.printStackTrace();
//            {success:false,message:"添加失败"}
            return new Result(false, "删除失败");
        }
    }

}