package com.th.mallchat.common.user.dao;


import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.th.mallchat.common.common.domain.vo.response.CursorPageBaseResp;
import com.th.mallchat.common.user.domain.entity.UserFriend;
import com.th.mallchat.common.user.domain.vo.request.CursorPageBaseReq;
import com.th.mallchat.common.user.mapper.UserFriendMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * <p>
 * 用户联系人表 服务实现类
 * </p>
 *
 * @author <a href="https://github.com/zongzibinbin">abin</a>
 * @since 2023-07-16
 */
@Service
public class UserFriendDao extends ServiceImpl<UserFriendMapper, UserFriend> {


    public CursorPageBaseResp<UserFriend> getFriendPage(Long uid, CursorPageBaseReq request) {
        LambdaQueryChainWrapper<UserFriend> wrapper = lambdaQuery();
        //游标字段（快速定位索引位置）
        wrapper.lt(UserFriend::getId,request.getCursor());
        //游标方向
        wrapper.orderByDesc(UserFriend::getId);
        wrapper.eq(UserFriend::getUid,uid);
        Page<UserFriend> page = page(request.plusPage(), wrapper);
        String cursor = Optional.ofNullable(CollectionUtil.getLast(page.getRecords()))
                .map(UserFriend::getId)
                .map(String::valueOf)
                .orElse(null);

        Boolean isLast = page.getRecords().size() != request.getPageSize();
        return new CursorPageBaseResp<>(cursor,isLast,page.getRecords());
    }

    public UserFriend getByFriend(Long uid, Long targetUid) {
        return lambdaQuery()
                .eq(UserFriend::getUid, uid)
                .eq(UserFriend::getFriendUid, targetUid)
                .one();
    }

    public List<UserFriend> getByFriends(Long uid, List<Long> uidList) {
        return lambdaQuery()
                .eq(UserFriend::getUid, uid)
                .in(UserFriend::getFriendUid, uidList)
                .list();
    }

    public List<UserFriend> getUserFriend(Long uid, Long friendUid) {
        return lambdaQuery()
                .eq(UserFriend::getUid, uid)
                .eq(UserFriend::getFriendUid, friendUid)
                .or()
                .eq(UserFriend::getFriendUid, uid)
                .eq(UserFriend::getUid, friendUid)
                .select(UserFriend::getId)
                .list();
    }
}
