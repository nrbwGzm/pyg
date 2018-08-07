package com.pinyougou.seckill.service.impl;

import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.mapper.TbSeckillOrderMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillOrder;
import entity.UserIdAndSeckillGoodsId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import utils.IdWorker;

import java.util.Date;

@Component
public class CreateOrder implements Runnable {


    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private TbSeckillOrderMapper seckillOrderMapper;
    @Autowired
    private TbSeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    private IdWorker idWorker;

    @Override
    public void run() {

//        从Redis中获取需要下单的任务
        //-----------------------------
        UserIdAndSeckillGoodsId userIdAndSeckillGoodsId = (UserIdAndSeckillGoodsId) redisTemplate.boundListOps("SECKILL_ORDER_QUEUE").leftPop();

        Long seckillGoodsId = userIdAndSeckillGoodsId.getSeckillGoodsId();
        String userId = userIdAndSeckillGoodsId.getUserId();


        TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("SECKILL_GOODS").get(seckillGoodsId);

        TbSeckillOrder seckillOrder = new TbSeckillOrder();
        seckillOrder.setUserId(userId);
        seckillOrder.setStatus("0");
        seckillOrder.setSellerId(seckillGoods.getSellerId());
        seckillOrder.setSeckillId(seckillGoodsId);
        seckillOrder.setMoney(seckillGoods.getCostPrice());
        seckillOrder.setId(idWorker.nextId());
        seckillOrder.setCreateTime(new Date());
        seckillOrderMapper.insert(seckillOrder);

//            向Redis中存放一个预支付信息
        redisTemplate.boundSetOps("SECKILL_PAY_LOG"+seckillGoodsId).add(userId);

        seckillGoods.setStockCount(seckillGoods.getStockCount()-1);  //减库存

        if(seckillGoods.getStockCount()==0){ // 商品销售售罄
//                此商品数据同步到mysql中
            seckillGoodsMapper.updateByPrimaryKey(seckillGoods);
//                把此商品从Redis中移除
            redisTemplate.boundHashOps("SECKILL_GOODS").delete(seckillGoodsId);
        }else{
            redisTemplate.boundHashOps("SECKILL_GOODS").put(seckillGoodsId,seckillGoods);
        }

       redisTemplate.boundValueOps("SECKILL_COUNT_ORDER_QUEUE").increment(-1);//排队人数减一


    }
}
