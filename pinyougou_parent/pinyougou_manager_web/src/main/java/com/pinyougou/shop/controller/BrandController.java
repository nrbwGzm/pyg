package com.pinyougou.shop.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.sellergoods.service.BrandService;
import entity.PageResult;
import entity.Result;
//导出Excel所用POI包
import org.apache.poi.hssf.usermodel.*;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//导出Excel所用POI包
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.util.List;
import java.util.Map;

/*
POI：操作office软件  我们一般用低版本，因为兼容性更高（高版本的office软件能读取低版本的文件，反之不可）
操作关键字：
(由大到小排序，单元格组成行，行组成工作表，工作表组成工作簿)
低版本：HSSF
(1)HSSFWorkbook	    工作簿
(2)HSSFSheet		工作表
(3)HSSFrow			行
(4)HSSFCell			单元格
 */

@RestController
@RequestMapping("/brand")
public class BrandController {

    @Reference
    private BrandService brandService;

    @RequestMapping("/showChart")
    public List<Map> showChart(){
        return brandService.showChart();
    }


    @RequestMapping("/exportXls")
    public  void exportXls(HttpServletRequest request, HttpServletResponse response)  throws Exception{
//        1、从request请求的session域中获取servlet容器，获取真实路径，拼接模板所在相对路径 = 模板路径
        String templatePath = request.getSession().getServletContext().getRealPath("") + "/template/brand.xls";
        //new一个 HSSFWorkbook工作簿，定义它的模板（new一个FileInputStream输入流通过templatePath获取模板）
        HSSFWorkbook  book = new HSSFWorkbook(new FileInputStream(templatePath));
        //2、获取需要导出的数据
        List<TbBrand> brandList = brandService.findAll();
        HSSFSheet sheetAt = book.getSheetAt(0);//获取当前工作簿的工作表
        //定义单元格的样式
        HSSFCellStyle cellStyle = book.getSheetAt(1).getRow(0).getCell(0).getCellStyle();

        //定义初始行角标
        int rowIndex=2;
        //循环遍历规格，每行创建不同角标的单元格。将规格的各属性放入不同的单元格中，行角标自增，遍历不到next下一个规格，退出循环
        for (TbBrand tbBrand : brandList) {
            HSSFRow row = sheetAt.createRow(rowIndex); //根据当前行角标创建一行
            HSSFCell idCell = row.createCell(0);//根据行创建单元格
            idCell.setCellValue(tbBrand.getId());//设置单元格的值
            idCell.setCellStyle(cellStyle);//设置单元格的格式

            HSSFCell nameCell = row.createCell(1);
            nameCell.setCellValue(tbBrand.getName());
            nameCell.setCellStyle(cellStyle);

            HSSFCell firstCharCell = row.createCell(2);
            firstCharCell.setCellValue(tbBrand.getFirstChar());
            firstCharCell.setCellStyle(cellStyle);
            rowIndex++;//角标自增
        }

//        从response请求响应获取输出流
        ServletOutputStream outputStream = response.getOutputStream();
//        两个头  1、文件的打开方式 in-line  attachment  2、文件的mime类型（常见的文件类型可以省略 xls）
        response.setHeader("content-disposition","attachment;fileName=brand.xls");
        //将输入流写入输出流
        book.write(outputStream);
    }

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