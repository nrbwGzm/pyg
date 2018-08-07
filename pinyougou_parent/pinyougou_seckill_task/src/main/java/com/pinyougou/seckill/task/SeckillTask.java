package com.pinyougou.seckill.task;

import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillGoodsExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class SeckillTask {

    @Autowired
    private TbSeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Scheduled(cron = "0/30 * * * * ?") //每30s执行一次这段代码，Scheduled：计划，cron：计划任务表达式，秒 分 时
    public void synchronizeSeckillGoodsToRedis() {   //同步秒杀商品到Redis缓存中

//        1、查询需要秒杀的商品 （审核通过、时间范围内、有库存）

        TbSeckillGoodsExample example = new TbSeckillGoodsExample();
        example.createCriteria().andStatusEqualTo("1")    //状态为1
                .andStartTimeLessThanOrEqualTo(new Date())      //秒杀开始时间小于或等于当前时间
                .andEndTimeGreaterThanOrEqualTo(new Date())     //秒杀结束时间大于或等于当前时间
                .andStockCountGreaterThan(0);             //库存数量大于0

        List<TbSeckillGoods> seckillGoodsList = seckillGoodsMapper.selectByExample(example);

//        2、把查询的商品放入Redis
        for (TbSeckillGoods tbSeckillGoods : seckillGoodsList) {
            redisTemplate.boundHashOps("SECKILL_GOODS").put(tbSeckillGoods.getId(), tbSeckillGoods);//存放秒杀商品

            for (int i = 0; i < tbSeckillGoods.getStockCount(); i++) {
                redisTemplate.boundListOps("SECKILL_GOODS_QUEUE" + tbSeckillGoods.getId()).leftPush(tbSeckillGoods.getId());
            }
        }

        System.out.println("synchronizeSeckillGoodsToRedis Finnish！！");

    }
}
