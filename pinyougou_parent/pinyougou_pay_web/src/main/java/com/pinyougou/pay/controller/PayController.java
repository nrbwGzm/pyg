package com.pinyougou.pay.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pinyougou.pay.service.PayService;
import java.util.Map;

@RestController
@RequestMapping("/pay")
public class PayController {

    @Reference
    private PayService payService;

    @RequestMapping("/unifiedorder")
    public Map unifiedorder(){
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        return  payService.unifiedorder(userId);
    }

    @RequestMapping("/queryOrder/{out_trade_no}")
    public Result queryOrder(@PathVariable("out_trade_no") String out_trade_no){

        int times=0;//次数

        try {
            while (times<=10){
                Map<String,String> map = payService.queryOrder(out_trade_no);
                if(map.get("trade_state").equals("NOTPAY")){ //未支付

                }
                if(map.get("trade_state").equals("SUCCESS")){    ;//支付成功
//                    payLog
//                    如果支付成功后需要修改订单的支付状态和支付时间  tb_order  tb_pay_log
                    String userId = SecurityContextHolder.getContext().getAuthentication().getName();
                    String  transaction_id = map.get("transaction_id"); //支付成功后微信的交易码
                    payService.updateOrderPayState(out_trade_no,userId,transaction_id);



                    return  new Result(true,"支付成功");
                }

                try {
                    Thread.sleep(3000);  //睡眠3秒
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                times++;

                System.out.println("times:"+times);

            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"支付失败");
        }

        return new Result(false,"支付超时");
    }


}
