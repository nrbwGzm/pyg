package com.pinyougou.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.seckill.service.SeckillService;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/seckill")
public class SeckillController {

    @Reference
    private SeckillService seckillService;

    @RequestMapping("/findSeckillList")
    public List<TbSeckillGoods> findSeckillList(){
       return seckillService.findAllSeckillGoodsFromRedis();
    }

    @RequestMapping("/findOne/{id}")
    public TbSeckillGoods findOne(@PathVariable("id") Long seckillGoodsId ){
       return seckillService.findOneSeckillGoodsFromRedis(seckillGoodsId);
    }
    
    @RequestMapping("/saveSeckillOrder/{id}")
    public Result saveSeckillOrder(@PathVariable("id") Long seckillGoodsId ){
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        if(userId.equals("anonymousUser")){
            return new Result(false,"请先登录");
        }
        try {
            seckillService.saveSeckillOrder(seckillGoodsId,userId);
            return new Result(true,"");
        } catch (RuntimeException e) {
            e.printStackTrace();
            return new Result(false,e.getMessage());
        }catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"下单失败");
        }
    }
}
