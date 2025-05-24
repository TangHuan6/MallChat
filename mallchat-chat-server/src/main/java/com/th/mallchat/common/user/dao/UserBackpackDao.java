package com.th.mallchat.common.user.dao;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.th.mallchat.common.common.domain.enums.YesOrNoEnum;
import com.th.mallchat.common.user.domain.entity.UserBackpack;
import com.th.mallchat.common.user.mapper.UserBackpackMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author 29385
* @description 针对表【user_backpack(用户背包表)】的数据库操作Service实现
* @createDate 2025-05-21 19:29:14
*/
@Service
public class UserBackpackDao extends ServiceImpl<UserBackpackMapper, UserBackpack>{

    public Integer getCountByVaildId(Long uid, Long itemId) {
        return lambdaQuery()
                .eq(UserBackpack::getUid, uid)
                .eq(UserBackpack::getId, itemId)
                .eq(UserBackpack::getStatus, YesOrNoEnum.NO.getStatus())
                .count();
    }

    public UserBackpack getFirstValidItem(Long uid, Long itemId) {
        return lambdaQuery()
                .eq(UserBackpack::getUid, uid)
                .eq(UserBackpack::getId, itemId)
                .eq(UserBackpack::getStatus, YesOrNoEnum.NO.getStatus())
                .last("LIMIT 1").one();

    }

    public boolean useItem(Long id) {
        return lambdaUpdate()
                .eq(UserBackpack::getId, id)
                .eq(UserBackpack::getStatus, YesOrNoEnum.NO.getStatus())
                .set(UserBackpack::getStatus, YesOrNoEnum.YES.getStatus())
                .update();
    }

    public List<UserBackpack> getByItemIds(Long uid, List<Long> collect) {
        return lambdaQuery()
                .eq(UserBackpack::getUid,uid)
                .eq(UserBackpack::getStatus, YesOrNoEnum.NO.getStatus())
                .in(UserBackpack::getItemId, collect)
                .list();
    }

    public UserBackpack getByIdempotent(String idempotent) {
        return lambdaQuery()
                .eq(UserBackpack::getIdempotent, idempotent)
                .one();
    }
}




