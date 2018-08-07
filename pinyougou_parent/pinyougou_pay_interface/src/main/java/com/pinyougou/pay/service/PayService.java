package com.pinyougou.pay.service;

import java.util.Map;

public interface PayService {
    Map unifiedorder(String userId);

    Map queryOrder(String out_trade_no);

    void updateOrderPayState(String out_trade_no, String userId, String transaction_id);
}
