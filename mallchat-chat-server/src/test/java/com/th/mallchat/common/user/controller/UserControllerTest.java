package com.th.mallchat.common.user.controller;


import com.th.mallchat.common.user.domain.entity.User;
import com.th.mallchat.common.user.mapper.UserMapper;
import com.th.mallchat.common.user.service.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class UserControllerTest {

    @Autowired
    private UserService userService;

    @Test
    public void test(){
        User user = userService.getById(1);
        System.out.println(user);
    }
}