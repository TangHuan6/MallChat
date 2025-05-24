package com.th.mallchat.common.user.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.th.mallchat.common.user.domain.entity.User;
import com.th.mallchat.common.user.mapper.UserMapper;
import com.th.mallchat.common.user.service.UserService;
import org.springframework.stereotype.Service;

/**
* @author 29385
* @description 针对表【user(用户表)】的数据库操作Service实现
* @createDate 2025-05-11 14:08:04
*/
@Service
public class UserDao extends ServiceImpl<UserMapper, User>{

    public User getByOpenId(String openId) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getOpenId, openId);
        return this.getOne(queryWrapper);
    }

    public User getByName(String name) {
        return lambdaQuery().eq(User::getName, name).one();
    }

    public void modifyName(Long uid, String name) {
        lambdaUpdate()
                .eq(User::getId, uid)
                .set(User::getName, name)
                .update();
    }

    public void wearingBadge(Long uid, Long badgeId) {
        lambdaUpdate()
                .eq(User::getId, uid)
                .set(User::getItemId,badgeId)
                .update();
    }
}





