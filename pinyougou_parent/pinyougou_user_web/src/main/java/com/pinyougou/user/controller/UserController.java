package com.pinyougou.user.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbUser;
import com.pinyougou.user.service.UserService;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    @Reference//(timeout=5000) 也可以用注解配置zookeeper超时时间
    private UserService userService;

    //点击发送验证码
    @RequestMapping("/sendCode/{phone}")
    public Result sendCode(@PathVariable("phone") String phone) {
        try {
            userService.sendCode(phone);
            return new Result(true, "验证码发送成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "验证码发送失败");
        }
    }

    //点击完成注册
    @RequestMapping("/add/{code}")
    public Result add(@RequestBody TbUser user, @PathVariable("code") String code) {
        try {

            //这里用了一个嵌套try/catch ,调用userServiceImpl的checkCode方法
            // 接收checkCode方法抛出的RuntimeException异常,
            //如果验证码过期或错误,直接返回false,并返回错误信息,交给前台JS弹出
            try {
                //判断验证码
                userService.checkCode(user.getPhone(), code);
            } catch (RuntimeException e) {
                e.printStackTrace();
                //验证码错误返回Result结果集
                return new Result(false, e.getMessage());
            }
            //如果验证码正确,调用userService往用户表添加一条数据
            userService.add(user);

//            int a = 1/0; 测试系统异常,返回系统忙的错误信息

            //系统正常,注册成功,成功则什么都不显示
            return new Result(true, "");
        } catch (Exception e) {
            e.printStackTrace();
            //系统异常,注册失败
            return new Result(false, "系统忙,注册失败!请重新注册");
        }
    }

    //显示用户名
    @RequestMapping("/showName")
    public String showName() {
        //获取安全框架持有人在容器中的身份的名字
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
