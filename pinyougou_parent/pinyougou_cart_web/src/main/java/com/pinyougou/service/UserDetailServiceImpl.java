package com.pinyougou.service;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;

//认证类,用于单点登录和安全认证,由spring-security.xml调用,给登陆者的赋予用户权限,发放一个ticket票据,实现单点登陆
public class UserDetailServiceImpl implements UserDetailsService {
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        List<GrantedAuthority> list = new ArrayList<GrantedAuthority>();//GrantedAuthority 授予权威

        //赋予用户权限,ROLE_USER是spring-security.xml中配置的权限,给用户赋予这个权限
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_USER");
        list.add(authority);

        //CAS与spring-security整合,spring-security提供一个用户名,password置空,由CAS给password那填一个ticket票据,实现单点登陆
        return new User(username,"",list);
    }
}


