package com.th.mallchat.common.user.controller;

import com.th.mallchat.common.common.domain.dto.RequestInfo;
import com.th.mallchat.common.common.domain.vo.response.ApiResult;
import com.th.mallchat.common.common.utils.RequestHolder;
import com.th.mallchat.common.user.domain.vo.response.UserInfoResp;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequestMapping("/capi/user")
@RestController
@Api(tags = "用户相关接口")
public class UserController {

    @GetMapping("/test")
    public String index() {
        return "hello";
    }

    @GetMapping("/userInfo")
    public ApiResult<UserInfoResp> getUserInfo(){
        RequestInfo requestInfo = RequestHolder.get();
        System.out.println(requestInfo.getIp());
        System.out.println(requestInfo.getUid());
        return null;
    }
}
