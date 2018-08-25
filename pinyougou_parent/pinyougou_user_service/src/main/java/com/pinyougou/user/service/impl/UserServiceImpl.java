package com.pinyougou.user.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.mapper.TbUserMapper;
import com.pinyougou.pojo.TbUser;
import com.pinyougou.user.service.UserService;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import utils.HttpClient;

import java.util.Date;
import java.util.concurrent.TimeUnit;
@Service
public class UserServiceImpl implements UserService{

    @Autowired
    private TbUserMapper userMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    //点击发送验证码
    @Override
    public void sendCode(String phone) throws Exception {
//      调用sms项目的发送验证码接口
        HttpClient httpClient = new HttpClient("http://localhost:7788/sms/sendSms");
//      1.需要设置的参数: String phone_numbers, String sign_name, String template_code, String template_param
//        a.可以指定的参数: 页面输入的用户手机号,阿里大于短信签名,阿里大于模板代码
        httpClient.addParameter("phone_numbers",phone);
        httpClient.addParameter("sign_name","高泽明");
        httpClient.addParameter("template_code","SMS_140735399");
//        b.随机生成的参数: 验证码
        //生成4位数的随机数字字符串 RandomStringUtils需要是org.apache.commons.lang3 包下的
        String sendCode = RandomStringUtils.randomNumeric(4);
        System.out.println("YanZhenMa: "+sendCode);
        //将验证码放入redis缓存中
//        redisTemplate.boundValueOps(key).set(value,Long类型的数字, 数字的单位);
        redisTemplate.boundValueOps(phone).set(sendCode,5l, TimeUnit.MINUTES);//验证码只在redis中存留5分钟

        //设置要发送的验证码
        httpClient.addParameter("template_param","{\"code\":\""+sendCode+"\"}");
        httpClient.post();
    }

//    判断验证码是否过期,未过期则判断与缓存中的验证码是否相同
    @Override
    public void checkCode(String phone, String code) {
        String sendCode = (String)redisTemplate.boundValueOps(phone).get();
        //如果验证码过期或错误,抛出一个RuntimeException异常,给Controller处理
        if(sendCode==null){
            throw new RuntimeException("验证码已过期");
        }else{
            if(!code.equals(sendCode)){
                throw new RuntimeException("验证码错误！");
            }
        }
    }
    //添加用户
    @Override
    public void add(TbUser user) {
        //修改时间
        user.setUpdated(new Date());
        //创建时间
        user.setCreated(new Date());
        //md5对密码加密
        String password = user.getPassword();
        String hex = DigestUtils.md5Hex(password);
        user.setPassword(hex);
        //存入表中
        userMapper.insert(user);
    }
}
