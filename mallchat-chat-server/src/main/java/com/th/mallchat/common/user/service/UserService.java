package com.th.mallchat.common.user.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.th.mallchat.common.user.domain.entity.User;
import com.th.mallchat.common.user.domain.vo.request.ModifyNameReq;
import com.th.mallchat.common.user.domain.vo.response.BadgeResp;
import com.th.mallchat.common.user.domain.vo.response.UserInfoResp;

import java.util.List;

/**
* @author 29385
* @description 针对表【user(用户表)】的数据库操作Service
* @createDate 2025-05-11 14:08:04
*/
public interface UserService{

    Long register(User user);

    UserInfoResp getUserInfo(Long uid);

    void modifyName(Long uid, String name);

    List<BadgeResp> badges(Long uid);
}
