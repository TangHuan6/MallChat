package com.th.mallchat.common.user.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequestMapping("/capi/user")
@RestController
public class UserController {

    @GetMapping("/test")
    public String index() {
        return "hello";
    }

    @GetMapping("/userInfo")
    public void getUserInfo(){

    }
}
