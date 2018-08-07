package com.pinyougou.order.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbOrderItemMapper;
import com.pinyougou.mapper.TbOrderMapper;
import com.pinyougou.mapper.TbPayLogMapper;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pojo.TbOrder;
import com.pinyougou.pojo.TbOrderExample;
import com.pinyougou.pojo.TbOrderExample.Criteria;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojo.TbPayLog;
import entity.PageResult;
import entityGroup.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import utils.IdWorker;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private TbOrderMapper orderMapper;
    @Autowired
    private TbOrderItemMapper orderItemMapper;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private TbPayLogMapper payLogMapper;

    /**
     * 查询全部
     */
    @Override
    public List<TbOrder> findAll() {
        return orderMapper.selectByExample(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<TbOrder> page = (Page<TbOrder>) orderMapper.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 增加
     */
    @Override
    public void add(TbOrder order) {
        String cartListStr = (String) redisTemplate.boundValueOps(order.getUserId()).get();//取出String格式的cartList购物车列表
        List<Cart> cartList = JSON.parseArray(cartListStr, Cart.class);//转换为Json格式

        //定义一个空的订单列表
        String orderList = "";
        //定义一个空的总价格
        Double totalMomey = 0.00;
        for (Cart cart : cartList) {
            TbOrder tbOrder = new TbOrder();
//		订单表页面提供的数据
//  `payment_type` varchar(1) COLLATE utf8_bin DEFAULT NULL COMMENT '支付类型，1、在线支付，2、货到付款',
//  `receiver_area_name` varchar(100) COLLATE utf8_bin DEFAULT NULL COMMENT '收货人地区名称(省，市，县)街道',
//  `receiver_mobile` varchar(12) COLLATE utf8_bin DEFAULT NULL COMMENT '收货人手机',
//  `receiver` varchar(50) COLLATE utf8_bin DEFAULT NULL COMMENT '收货人',
            tbOrder.setPaymentType(order.getPaymentType());
            tbOrder.setReceiverAreaName(order.getReceiverAreaName());
            tbOrder.setReceiverMobile(order.getReceiverMobile());
            tbOrder.setReceiver(order.getReceiver());
            long orderId = idWorker.nextId();//通过工具类获取一个分布式ID（唯一，不重复）
            orderList += orderId + ",";
            //				订单表 保存订单是后台需要赋的数据
//  `order_id` bigint(20) NOT NULL COMMENT '订单id',
            tbOrder.setOrderId(orderId);//订单id

            //  `user_id` varchar(50) COLLATE utf8_bin DEFAULT NULL COMMENT '用户id',
            tbOrder.setUserId(order.getUserId());//用户id

//            ---------------------这里进行一个分单，每个商家的订单为一个分单---------------------------

            Double payment = 0.00;//分单总金额
            for (TbOrderItem orderItem : cart.getOrderItemList()) {
                payment += orderItem.getTotalFee().doubleValue();
                //				订单详情表
//  `order_id` bigint(20) NOT NULL COMMENT '订单id',
                orderItem.setOrderId(orderId);
//  `id` bigint(20) NOT NULL,
                orderItem.setId(idWorker.nextId());
                orderItemMapper.insert(orderItem);
            }

            totalMomey += payment;

            //  `payment` decimal(20,2) DEFAULT NULL COMMENT '实付金额。精确到2位小数;单位:元。如:200.07，表示:200元7分',
            tbOrder.setPayment(new BigDecimal(payment));

            //  `status` varchar(1) COLLATE utf8_bin DEFAULT NULL COMMENT '状态：1、未付款，2、已付款，3、未发货，4、已发货，5、交易成功，6、交易关闭,7、待评价',
            tbOrder.setStatus("1");
            //  `create_time` datetime DEFAULT NULL COMMENT '订单创建时间',
            tbOrder.setCreateTime(new Date());
            //  `update_time` datetime DEFAULT NULL COMMENT '订单更新时间',
            tbOrder.setUpdateTime(new Date());
            //  `seller_id` varchar(100) COLLATE utf8_bin DEFAULT NULL COMMENT '商家ID',
            tbOrder.setSellerId(cart.getSellerId());

            orderMapper.insert(tbOrder);

        }

//		支付日志表
        TbPayLog payLog = new TbPayLog();
        payLog.setCreateTime(new Date());
        payLog.setOrderList(orderList.substring(0, orderList.length() - 1));//去掉，号
        payLog.setOutTradeNo(idWorker.nextId() + "");//设置分布式ID
        payLog.setPayType(order.getPaymentType());//设置支付方式
        payLog.setTotalFee((totalMomey.longValue() * 100));  //单位：分
        payLog.setTradeState("0");//设置支付状态：未支付
        payLog.setUserId(order.getUserId());

        payLogMapper.insert(payLog);//添加到支付日志表
        redisTemplate.boundHashOps("payLog").put(order.getUserId(), payLog);

        redisTemplate.delete(order.getUserId());//清空购物车数据

    }

    /**
     * 修改
     */
    @Override
    public void update(TbOrder order) {
        orderMapper.updateByPrimaryKey(order);
    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public TbOrder findOne(Long id) {
        return orderMapper.selectByPrimaryKey(id);
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            orderMapper.deleteByPrimaryKey(id);
        }
    }

    @Override
    public PageResult findPage(TbOrder order, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        TbOrderExample example = new TbOrderExample();
        Criteria criteria = example.createCriteria();

        if (order != null) {
            if (order.getPaymentType() != null && order.getPaymentType().length() > 0) {
                criteria.andPaymentTypeLike("%" + order.getPaymentType() + "%");
            }
            if (order.getPostFee() != null && order.getPostFee().length() > 0) {
                criteria.andPostFeeLike("%" + order.getPostFee() + "%");
            }
            if (order.getStatus() != null && order.getStatus().length() > 0) {
                criteria.andStatusLike("%" + order.getStatus() + "%");
            }
            if (order.getShippingName() != null && order.getShippingName().length() > 0) {
                criteria.andShippingNameLike("%" + order.getShippingName() + "%");
            }
            if (order.getShippingCode() != null && order.getShippingCode().length() > 0) {
                criteria.andShippingCodeLike("%" + order.getShippingCode() + "%");
            }
            if (order.getUserId() != null && order.getUserId().length() > 0) {
                criteria.andUserIdLike("%" + order.getUserId() + "%");
            }
            if (order.getBuyerMessage() != null && order.getBuyerMessage().length() > 0) {
                criteria.andBuyerMessageLike("%" + order.getBuyerMessage() + "%");
            }
            if (order.getBuyerNick() != null && order.getBuyerNick().length() > 0) {
                criteria.andBuyerNickLike("%" + order.getBuyerNick() + "%");
            }
            if (order.getBuyerRate() != null && order.getBuyerRate().length() > 0) {
                criteria.andBuyerRateLike("%" + order.getBuyerRate() + "%");
            }
            if (order.getReceiverAreaName() != null && order.getReceiverAreaName().length() > 0) {
                criteria.andReceiverAreaNameLike("%" + order.getReceiverAreaName() + "%");
            }
            if (order.getReceiverMobile() != null && order.getReceiverMobile().length() > 0) {
                criteria.andReceiverMobileLike("%" + order.getReceiverMobile() + "%");
            }
            if (order.getReceiverZipCode() != null && order.getReceiverZipCode().length() > 0) {
                criteria.andReceiverZipCodeLike("%" + order.getReceiverZipCode() + "%");
            }
            if (order.getReceiver() != null && order.getReceiver().length() > 0) {
                criteria.andReceiverLike("%" + order.getReceiver() + "%");
            }
            if (order.getInvoiceType() != null && order.getInvoiceType().length() > 0) {
                criteria.andInvoiceTypeLike("%" + order.getInvoiceType() + "%");
            }
            if (order.getSourceType() != null && order.getSourceType().length() > 0) {
                criteria.andSourceTypeLike("%" + order.getSourceType() + "%");
            }
            if (order.getSellerId() != null && order.getSellerId().length() > 0) {
                criteria.andSellerIdLike("%" + order.getSellerId() + "%");
            }

        }

        Page<TbOrder> page = (Page<TbOrder>) orderMapper.selectByExample(example);
        return new PageResult(page.getTotal(), page.getResult());
    }

}
