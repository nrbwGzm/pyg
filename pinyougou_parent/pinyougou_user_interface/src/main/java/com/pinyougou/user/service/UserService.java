package com.pinyougou.user.service;

import com.pinyougou.pojo.TbUser;

public interface UserService {

    //可能发送失败,抛出异常给Controller处理
    void sendCode(String phone) throws Exception;

    void checkCode(String phone, String code);

    void add(TbUser user);
}
