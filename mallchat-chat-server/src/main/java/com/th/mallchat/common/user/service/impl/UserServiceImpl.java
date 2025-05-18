package com.th.mallchat.common.user.service.impl;

import com.th.mallchat.common.user.dao.UserDao;
import com.th.mallchat.common.user.domain.entity.User;
import com.th.mallchat.common.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDao userDao;

    @Override
    @Transactional
    public Long register(User user) {
        userDao.save(user);
        //todo 用户注册的事件
        return user.getId();
    }
}
