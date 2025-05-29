package com.th.mallchat.common.user.dao;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.th.mallchat.common.user.domain.entity.UserApply;
import com.th.mallchat.common.user.domain.enums.ApplyReadStatusEnum;
import com.th.mallchat.common.user.domain.enums.ApplyStatusEnum;
import com.th.mallchat.common.user.domain.enums.ApplyTypeEnum;
import com.th.mallchat.common.user.mapper.UserApplyMapper;
import org.springframework.stereotype.Service;

import java.util.List;



/**
 * <p>
 * 用户申请表 服务实现类
 * </p>
 *
 * @author <a href="https://github.com/zongzibinbin">abin</a>
 * @since 2023-07-16
 */
@Service
public class UserApplyDao extends ServiceImpl<UserApplyMapper, UserApply> {


    public void agree(Long applyId) {
        lambdaUpdate()
                .eq(UserApply::getId,applyId)
                .set(UserApply::getStatus, ApplyStatusEnum.AGREE.getCode())
                .update();
    }

    public UserApply getFriendApproving(Long uid, Long targetUid) {
        return lambdaQuery()
                .eq(UserApply::getUid,uid)
                .eq(UserApply::getTargetId,targetUid)
                .eq(UserApply::getStatus,ApplyStatusEnum.WAIT_APPROVAL)
                .eq(UserApply::getType, ApplyTypeEnum.ADD_FRIEND.getCode())
                .one();
    }

    public IPage<UserApply> friendApplyPage(Long uid, Page page) {
        return lambdaQuery()
                .eq(UserApply::getTargetId,uid)
                .eq(UserApply::getType,ApplyTypeEnum.ADD_FRIEND.getCode())
                .orderByDesc(UserApply::getCreateTime)
                .page(page);
    }

    public void readApples(Long uid, List<Long> applyIds) {
        lambdaUpdate()
                .set(UserApply::getReadStatus, ApplyReadStatusEnum.READ.getCode())
                .eq(UserApply::getReadStatus, ApplyReadStatusEnum.UNREAD.getCode())
                .in(UserApply::getId, applyIds)
                .eq(UserApply::getTargetId, uid)
                .update();
    }

    public Integer getUnReadCount(Long uid) {
        return lambdaQuery()
                .eq(UserApply::getTargetId,uid)
                .eq(UserApply::getReadStatus,ApplyReadStatusEnum.UNREAD.getCode())
                .count();
    }
}
