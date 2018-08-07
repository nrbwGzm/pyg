package entity;

import java.io.Serializable;

//秒杀订单类，保存用户ID和秒杀商品ID
public class UserIdAndSeckillGoodsId implements Serializable{


    private String userId;

    private Long seckillGoodsId;

    public UserIdAndSeckillGoodsId(String userId, Long seckillGoodsId) {
        this.userId = userId;
        this.seckillGoodsId = seckillGoodsId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Long getSeckillGoodsId() {
        return seckillGoodsId;
    }

    public void setSeckillGoodsId(Long seckillGoodsId) {
        this.seckillGoodsId = seckillGoodsId;
    }
}
