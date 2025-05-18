package com.th.mallchat.common.user.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.th.mallchat.common.user.domain.entity.User;

/**
* @author 29385
* @description 针对表【user(用户表)】的数据库操作Service
* @createDate 2025-05-11 14:08:04
*/
public interface UserService{

    Long register(User user);
}
