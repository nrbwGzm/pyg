package com.pinyougou.seckill.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.mapper.TbSeckillOrderMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.seckill.service.SeckillService;
import entity.UserIdAndSeckillGoodsId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import utils.IdWorker;

import java.util.List;

@Service
public class SeckillServiceImpl implements SeckillService {

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private TbSeckillOrderMapper seckillOrderMapper;
    @Autowired
    private TbSeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    private IdWorker idWorker;
    @Autowired
    private CreateOrder createOrder;

    @Autowired
    private ThreadPoolTaskExecutor executor;

    @Override
    public List<TbSeckillGoods> findAllSeckillGoodsFromRedis() {
        List<TbSeckillGoods> seckill_goods = redisTemplate.boundHashOps("SECKILL_GOODS").values();
        return seckill_goods;
    }

    @Override
    public TbSeckillGoods findOneSeckillGoodsFromRedis(Long seckillGoodsId) {
        TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("SECKILL_GOODS").get(seckillGoodsId);
        return seckillGoods;
    }

    @Override
    public void saveSeckillOrder(Long seckillGoodsId, String userId) {

        Boolean member = redisTemplate.boundSetOps("SECKILL_PAY_LOG" + seckillGoodsId).isMember(userId);
        if(member){
            throw new RuntimeException("请先支付您已买到的商品!");
        }

        //----------------------------------------
            TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("SECKILL_GOODS").get(seckillGoodsId);
//           需要判断是否有库存
           if(seckillGoods==null || seckillGoods.getStockCount()<=0 ){
             throw new RuntimeException("商品已售罄!");
           }


//           从Redis队列中判断是否有此商品
            Object obj = redisTemplate.boundListOps("SECKILL_GOODS_QUEUE"+seckillGoodsId).leftPop();

           if(obj==null){
               throw new RuntimeException("商品已售罄!");
           }

            Long count_order_queue = redisTemplate.boundValueOps("SECKILL_COUNT_ORDER_QUEUE").size();
           if(count_order_queue+10>seckillGoods.getStockCount()){
               throw new RuntimeException("排队人数较多！");
           }


        redisTemplate.boundValueOps("SECKILL_COUNT_ORDER_QUEUE").increment(0);//排队人数加1



//           操作mysql数据库
//        把需要下单的任务放到Redis中
        redisTemplate.boundListOps("SECKILL_ORDER_QUEUE").leftPush(new UserIdAndSeckillGoodsId(userId,seckillGoodsId));


        executor.execute(createOrder);//执行线程的任务


    }
}
