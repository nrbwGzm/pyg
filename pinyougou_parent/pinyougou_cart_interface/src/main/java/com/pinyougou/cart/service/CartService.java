package com.pinyougou.cart.service;

import entityGroup.Cart;

import java.util.List;

public interface CartService {
    List<Cart> findCartListFromRedis(String sessionId);

    List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, int num);

    void saveCartListToRedis(String sessionId, List<Cart> cartList);

    void saveCartListToRedisByUsername(String username, List<Cart> cartList);

    List<Cart> mergeCartList(List<Cart> cartList_sessionID, List<Cart> cartList_username);

    void deleteCartListFromRedis(String sessionId);
}
