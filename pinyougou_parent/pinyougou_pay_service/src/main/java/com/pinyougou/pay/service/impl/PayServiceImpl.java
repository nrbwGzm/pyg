package com.pinyougou.pay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import com.pinyougou.mapper.TbOrderMapper;
import com.pinyougou.mapper.TbPayLogMapper;
import com.pinyougou.pojo.TbOrder;
import com.pinyougou.pojo.TbPayLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import utils.HttpClient;
import utils.IdWorker;

import com.pinyougou.pay.service.PayService;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
@Service
@Transactional
public class PayServiceImpl implements PayService {

    @Value("${appid}")
    private String appid;
    @Value("${partner}")
    private String partner;
    @Value("${partnerkey}")
    private String partnerkey;
    @Value("${notifyurl}")
    private String notifyurl;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private TbPayLogMapper payLogMapper;
    @Autowired
    private TbOrderMapper orderMapper;


    @Autowired
    private RedisTemplate redisTemplate;
    @Override
    public Map unifiedorder(String userId) {
//        调用微信统一下单的接口
//        https://api.mch.weixin.qq.com/pay/unifiedorder

        HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
        httpClient.setHttps(true);
//        long out_trade_no = idWorker.nextId();

       TbPayLog payLog = (TbPayLog) redisTemplate.boundHashOps("payLog").get(userId);

        String out_trade_no = payLog.getOutTradeNo();

        System.out.println("out_trade_no:"+out_trade_no);

        Map<String,String> paramMap = new HashMap(); //构建一map 准备参数  放完后再转成xml字符串（微信要的就是xml字符串）
        paramMap.put("appid",appid);
        paramMap.put("mch_id",partner);
        paramMap.put("nonce_str",WXPayUtil.generateNonceStr());
        paramMap.put("body","品优购支付");
        paramMap.put("out_trade_no",out_trade_no);
        paramMap.put("total_fee","1");  //模拟一分钱的交易
//        paramMap.put("total_fee",payLog.getTotalFee()+"");
        paramMap.put("spbill_create_ip","127.0.0.1");
        paramMap.put("notify_url",notifyurl);
        paramMap.put("trade_type","NATIVE");

        try {
            //带着签名转xml
            String xmlParam = WXPayUtil.generateSignedXml(paramMap, partnerkey);

            httpClient.setXmlParam(xmlParam);
            httpClient.post();
            String content = httpClient.getContent();

            Map<String, String> resultMap = WXPayUtil.xmlToMap(content);


            resultMap.put("out_trade_no",out_trade_no+"");
            resultMap.put("total_fee",payLog.getTotalFee()+"");

            return resultMap;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Map queryOrder(String out_trade_no) {
//        调用查询订单的接口
//

        HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
        httpClient.setHttps(true);
        Map<String,String> paramMap = new HashMap(); //构建一map 准备参数  放完后再转成xml字符串（微信要的就是xml字符串）
        paramMap.put("appid",appid);
        paramMap.put("mch_id",partner);
        paramMap.put("out_trade_no",out_trade_no);
        paramMap.put("nonce_str",WXPayUtil.generateNonceStr());

        try {
            //带着签名转xml
            String xmlParam = WXPayUtil.generateSignedXml(paramMap, partnerkey);
            httpClient.setXmlParam(xmlParam);
            httpClient.post();
            String content = httpClient.getContent();

            Map<String, String> resultMap = WXPayUtil.xmlToMap(content);

            return resultMap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }



    }

    @Override
    public void updateOrderPayState(String out_trade_no, String userId, String transaction_id) {
//        修改payLog

       TbPayLog payLog = (TbPayLog) redisTemplate.boundHashOps("payLog").get(userId);
        payLog.setTradeState("1");//已支付
        payLog.setPayTime(new Date());
        payLog.setTransactionId(transaction_id);
        payLogMapper.updateByPrimaryKey(payLog);

//        修改order
        String[] orderIds = payLog.getOrderList().split(",");
        for (String orderId : orderIds) {
            TbOrder tbOrder = orderMapper.selectByPrimaryKey(Long.parseLong(orderId));
            tbOrder.setUpdateTime(new Date());
            tbOrder.setStatus("1");
            tbOrder.setPaymentTime(new Date());
            orderMapper.updateByPrimaryKey(tbOrder);
        }

//        清空Redis支付日志数据
        redisTemplate.boundHashOps("payLog").delete(userId);

    }
}
