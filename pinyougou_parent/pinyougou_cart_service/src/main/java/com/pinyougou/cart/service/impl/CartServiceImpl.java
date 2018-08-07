package com.pinyougou.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbOrderItem;
import entityGroup.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

        /*名词说明:
         *       tbItem          :   sku类,根据商品详情页传过来的itemId (skuId)通过itemMapper获取到的tbItem(sku类)
         *       orderItem       :   订单类,一个sku商品对应一个订单,含有 num 购买数量属性
         *       orderItemList   :   商家购物车列表,内含多个orderItem
         *       Cart            ;   商家购物车类,内含属性orderItemList,sellerId(商家ID),sellerName(商家名称,用来显示)
         *       CartList        :   用户购物车列表,内含多个Cart
         *
         * */
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private TbItemMapper itemMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    //从redis中寻找购物车,登录状态 传过来的是当前用户的username
    // 未登录状态 传过来的是匿名用户的username : anonymousUser
    @Override
    public List<Cart> findCartListFromRedis(String username) {

        //根据sessionId从redis中获取购物车(String类型的)  redis中,key: username  value:cartList
        String cartListString = (String) redisTemplate.boundValueOps(username).get();

        //如果本地redis中的购物车为空,设置个空数组样式的字符串
        if (cartListString == null) {
            cartListString = "[]";
        }
        //将String类型的cartListString ,转换为由Cart实体类对象组成的List类型
        List<Cart> cartList = JSON.parseArray(cartListString, Cart.class);
        //一个商家的多个订单为一个Cart购物车类,List<Cart>就是所有商家的购物车组成的真正的购物车,购物车列表
        return cartList;//cartList:真正的购物车
    }

/*添加购物车逻辑：
   1、根据itemId  查询TbItem
   2、判断 TbItem 所属的商家  CartList 是否有购物车数据
      2.1 如果购物车列表中有此商家
        2.1.1 需要判断即将添加的商品是否在此商家的购物车中 如果存在数据累加
        2.1.2 如果不存在  创建TbOrderItem 添加到orderItemList;
      2.2  如果购物车列表中没有此商家
                创建一个cart对象  创建TBOrderItem对象  放到 cart中的orderItemList中
                把cart对象添加到cartList中
*/

    //商品详情页点击添加购物车,将商品生成一个订单,添加到用户的购物车列表中
    @Override                            //直接传过来一个List<Cart> cartList,就不用new了? 错!!!
                                        //从页面传过来的cartList购物车,可能携带一部分之前购物车的数据,不能new
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, int num) {

//        添加购物车逻辑：
//        1、根据itemId(skuId)  查询TbItem(sku类)
        TbItem tbItem = itemMapper.selectByPrimaryKey(itemId);
//        如果传过来的itemId在数据库中不存在,自然查不到,抛出异常,给Controller
        if (tbItem == null) {
            throw new RuntimeException("无此商品!");
        }
//        2、判断 TbItem 所属的商家  CartList 是否有购物车数据
        String sellerId = tbItem.getSellerId();
        //从购物车列表中寻找TbItem 所属商家的购物车类
        Cart cart = findCartFromCartList(sellerId, cartList);
//        2.1 如果购物车列表中有此商家的购物车类
        if (cart != null) {
            //2.1.1 需要判断即将添加的商品是否在此商家的购物车列表的订单中

            //通过该商家的购物车类中获取该商家的购物车列表
            List<TbOrderItem> orderItemList = cart.getOrderItemList();
            //findOrderItemFromOrderItemList : 从该商家的购物车列表中寻找此订单
            TbOrderItem orderItem = findOrderItemFromOrderItemList(itemId, orderItemList);

            //2.1.1.1 如果不存在  新建一个TbOrderItem (订单)添加到orderItemList(该商家的购物车列表);
            if (orderItem == null) {
                //根据sku类,购买数量,新建一个订单    从findCartListFromRedis方法中来的
                orderItem = createOrderitem(tbItem, num);
                //添加到orderItemList;
                orderItemList.add(orderItem);

                //2.1.1.2 如果存在,该订单的数量累加
            } else {
                //如果存在数据累加
                orderItem.setNum(orderItem.getNum() + num);
                //当商品数量为1的时候,用户再减,从该商家的购物车列表中移除该订单
                if (orderItem.getNum() < 1) {
                    //从orderItemList列表删除订单
                    orderItemList.remove(orderItem);
                    //判断orderItemList的长度  判断此商家是否还有订单
                    if (orderItemList.size() == 0) {    //当该商家的购物车列表为空,从购物车列表中移除该商家
                        //把此商家对应的cart(商家购物车类对象)从cartList(用户购物车列表)中移除
                        cartList.remove(cart);
                    }
                } else {
                    //计算该订单的总价
                    double totalFee = orderItem.getPrice().doubleValue() * orderItem.getNum();
                    //设置该订单总价
                    orderItem.setTotalFee(new BigDecimal(totalFee));
                }

            }
//      2.2  如果购物车列表中没有此商家
        } else {
            //创建一个cart(商家购物车类)对象  创建TBOrderItem对象  放到 cart中的orderItemList中
            cart = new Cart();
            cart.setSellerId(tbItem.getSellerId());
            cart.setSellerName(tbItem.getSeller());

            TbOrderItem orderItem = new TbOrderItem();
            List<TbOrderItem> orderItemList = new ArrayList<TbOrderItem>();
            //根据sku类,购买数量,新建一个订单
            orderItem = createOrderitem(tbItem, num);
            //将订单添加到商家购物车列表中
            orderItemList.add(orderItem);
            //将商家购物车列表重新设置到商家购物车类上
            cart.setOrderItemList(orderItemList);
            //将商家购物车类添加到用户购物车中
            cartList.add(cart);
        }
        //返回用户购物车
        return cartList;
    }

    //保存用户购物车到redis中    key:sessionId   value:cartListString
    @Override
    public void saveCartListToRedis(String sessionId, List<Cart> cartList) {
        String cartListString = JSON.toJSONString(cartList);//格式:"[{},{}]" 一个个购物车对象组成购物车列表
        //根据sessionId设置Sting类型的cartList,并设置redis销毁时间,24X7=336,单位是小时,数据类型为long
        redisTemplate.boundValueOps(sessionId).set(cartListString, 336L, TimeUnit.HOURS);
    }

    //将用户购物车存到redis中,key: username  value:cartListString
    @Override
    public void saveCartListToRedisByUsername(String username, List<Cart> cartList) {
        String cartListString = JSON.toJSONString(cartList);//"[{},{}]"
        redisTemplate.boundValueOps(username).set(cartListString);
    }

    //合并购物车,将未登录时的用户购物车数据添加到已登录用户的购物车中
    @Override
    public List<Cart> mergeCartList(List<Cart> cartList_sessionID, List<Cart> cartList_username) {
        for (Cart cart : cartList_sessionID) {
            for (TbOrderItem tbOrderItem : cart.getOrderItemList()) {
                cartList_username = addGoodsToCartList(cartList_username,tbOrderItem.getItemId(),tbOrderItem.getNum());
            }
        }
        return cartList_username;
    }

    //从redis中删除用户购物车
    @Override
    public void deleteCartListFromRedis(String sessionId) {
        redisTemplate.delete(sessionId);
    }

//-------------------------------- 以下为抽取出来的私有方法 --------------------------------------------------


    //从购物车列表中根据sellerId寻找该商家的购物车,找到就返回cart,如果没找到,返回null
    private Cart findCartFromCartList(String sellerId, List<Cart> cartList) {
        for (Cart cart : cartList) {
            if (cart.getSellerId().equals(sellerId)) {
                return cart;
            }
        }
        return null;
    }

    //判断即将添加的商品是否在此商家的购物车的订单中
    private TbOrderItem findOrderItemFromOrderItemList(Long itemId, List<TbOrderItem> orderItemList) {
        //循环此商家的购物车,如果有即将添加的商品的订单,将其返回,没有返回null
        for (TbOrderItem orderItem : orderItemList) {
            if (itemId.longValue() == orderItem.getItemId().longValue()) {
                return orderItem;
            }
        }
        return null;
    }

    //    如果单个商品在商家购物车列表的订单中不存在,创建一个订单,设置其属性,并返回
    private TbOrderItem createOrderitem(TbItem tbItem, int num) {

        //商品数量不能小于0
        if (num < 1) {
            throw new RuntimeException("数量非法！");
        }

        //新建一个订单
        TbOrderItem orderItem = new TbOrderItem();

//        设置订单的属性

        orderItem.setNum(num);
        orderItem.setGoodsId(tbItem.getGoodsId());
//                orderItem.setId();
        orderItem.setItemId(tbItem.getId());
//                orderItem.setOrderId();
        orderItem.setPicPath(tbItem.getImage());
        orderItem.setPrice(tbItem.getPrice());
        orderItem.setSellerId(tbItem.getSellerId());
        orderItem.setTitle(tbItem.getTitle());
//        计算订单总价
        double totalFee = orderItem.getPrice().doubleValue() * orderItem.getNum();
//        设置订单的总价
        orderItem.setTotalFee(new BigDecimal(totalFee));
        return orderItem;
    }

}
