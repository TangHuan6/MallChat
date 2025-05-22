package com.th.mallchat.common.user.service.adapter;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import com.th.mallchat.common.common.domain.enums.YesOrNoEnum;
import com.th.mallchat.common.user.domain.entity.ItemConfig;
import com.th.mallchat.common.user.domain.entity.User;
import com.th.mallchat.common.user.domain.entity.UserBackpack;
import com.th.mallchat.common.user.domain.vo.response.BadgeResp;
import com.th.mallchat.common.user.domain.vo.response.UserInfoResp;
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;

import java.util.*;
import java.util.stream.Collectors;

public class UserAdapter {
    public static User buildUserSave(String openId){
        return User.builder().openId(openId).build();
    }


    public static User buildAuthorizeUser(Long uid, WxOAuth2UserInfo userInfo) {
        User user = new User();
        user.setId(uid);
        user.setAvatar(userInfo.getHeadImgUrl());
        user.setName(userInfo.getNickname());
        user.setSex(userInfo.getSex());
        if (userInfo.getNickname().length() > 6) {
            user.setName("名字过长" + RandomUtil.randomInt(100000));
        } else {
            user.setName(userInfo.getNickname());
        }
        return user;
    }

    public static UserInfoResp buildUserInfo(User user, Integer modifyNameCount) {
        UserInfoResp userInfoResp = new UserInfoResp();
        BeanUtil.copyProperties(user, userInfoResp);
        userInfoResp.setModifyNameChance(modifyNameCount);
        return userInfoResp;
    }

    public static List<BadgeResp> buildBadgeResp(List<ItemConfig> itemConfigs, List<UserBackpack> backpacks, User user) {
        if (ObjectUtil.isNull(user)){
            return Collections.emptyList();
        }
        Set<Long> obtainItemSet = backpacks.stream().map(UserBackpack::getItemId).collect(Collectors.toSet());
        return itemConfigs.stream().map(a -> {
            BadgeResp resp = new BadgeResp();
            BeanUtil.copyProperties(a, resp);
            resp.setObtain(obtainItemSet.contains(a.getId())? YesOrNoEnum.YES.getStatus():YesOrNoEnum.NO.getStatus());
            resp.setWearing(ObjectUtil.equal(a.getId(),user.getItemId())?YesOrNoEnum.YES.getStatus():YesOrNoEnum.NO.getStatus());
            return resp;
        }).sorted(Comparator.comparing(BadgeResp::getWearing,Comparator.reverseOrder())
                .thenComparing(BadgeResp::getObtain,Comparator.reverseOrder())).collect(Collectors.toList());

    }
}
