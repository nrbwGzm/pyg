package com.pinyougou.seckill.service;

import com.pinyougou.pojo.TbSeckillGoods;

import java.util.List;

public interface SeckillService {
    //从缓存中拿到所有秒杀商品
    List<TbSeckillGoods> findAllSeckillGoodsFromRedis();
    //通过秒杀商品ID从缓存中拿到该秒杀商品
    TbSeckillGoods findOneSeckillGoodsFromRedis(Long seckillGoodsId);
    //通过秒杀商品ID,用户ID 保存秒杀订单
    void saveSeckillOrder(Long seckillGoodsId, String userId);
}
