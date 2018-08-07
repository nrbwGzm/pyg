package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.cart.service.CartService;
import entity.Result;
import entityGroup.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import utils.CookieUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Reference
    private CartService cartService;

    //用于设置cookie
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private HttpServletResponse response;

    @Autowired
    private HttpSession session;

    private String getSessionId() {
//        从cookie中获取
        String sessionId = CookieUtil.getCookieValue(request, "cartCookie", "utf-8");
//		首次取sessionId时，sessionId为空，在此设置sessionId，存到cookie中
        if (sessionId == null) {
            sessionId = session.getId();
            CookieUtil.setCookie(request, response, "cartCookie", sessionId, 48 * 60 * 60, "utf-8");//48*60*60的单位为秒
        }
//        当sessionId为空时 放入一个sessionId
        return sessionId;
    }

    @RequestMapping("/findCartList")
    public List<Cart> findCartList() {
        //获取sessionId
        String sessionId = getSessionId();
        //根据sessionId从redis中查询出购物车列表
        List<Cart> cartList_sessionID = cartService.findCartListFromRedis(sessionId);
        //获取当前认证持有者的 Name,也就是用户名
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
//      未登录时默认是匿名访问,匿名访问时用户名为 anonymousUser
        if (!userName.equals("anonymousUser")) {
//            代表有登录人
//            根据账号获取购物车数据
            List<Cart> cartList_username = cartService.findCartListFromRedis(userName);

//                    需要和根据sessionId获取到的购物车数据合并
//            判断根据sessionId获取到的数据是否为空
            if (cartList_sessionID.size() != 0) {
//                cartList_sessionID  cartList_username
//                合并购物车
                cartList_username = cartService.mergeCartList(cartList_sessionID, cartList_username);
//
                // 1、 合并完成后,删除 cartList_sessionID 本地购物车
                cartService.deleteCartListFromRedis(sessionId);
                // 2、保存合并后的cartList_username
                cartService.saveCartListToRedis(userName, cartList_username);

            }
            //已登录,返回合并后的用户购物车
            return cartList_username;
        }
        //未登录,返回本地匿名用户购物车
        return cartList_sessionID;
    }

    //  向用户购物车中添加一条订单
    @RequestMapping("/addGoodsToCartList/{itemId}/{num}")
    @CrossOrigin(origins = "http://item.pinyougou.com")  //跨域：意味着此方法信任从"http://item.pinyougou.com"网址过来的请求
    public Result addGoodsToCartList(HttpSession session, @PathVariable("itemId") Long itemId, @PathVariable("num") int num) {
        String sessionId = getSessionId();
        try {
//          查询出购物车列表,调用抽取出来的查询购物车方法
            List<Cart> cartList = findCartList();

//          添加购物数据
            cartList = cartService.addGoodsToCartList(cartList, itemId, num);
//            获取当前登录人账号
            String userName = SecurityContextHolder.getContext().getAuthentication().getName();

//          anonymousUser匿名访问  没有登录人
            if (!userName.equals("anonymousUser")) {
                //已登录,保存用户购物车
                cartService.saveCartListToRedisByUsername(userName, cartList);
            } else {
                //未登录,保存匿名用户购物车
                cartService.saveCartListToRedis(sessionId, cartList);
            }

            //系统正常,添加购物车成功
            return new Result(true, "添加购物车成功");
        } catch (RuntimeException e) {
            e.printStackTrace();
            //接收业务层抛出的RuntimeException,打印异常信息
            return new Result(false, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            //系统异常,添加购物车失败
            return new Result(false, "添加购物车失败");
        }
    }

}

