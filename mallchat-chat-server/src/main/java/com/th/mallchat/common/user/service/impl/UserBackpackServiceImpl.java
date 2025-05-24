package com.th.mallchat.common.user.service.impl;

import com.th.mallchat.common.common.annotation.RedissonLock;
import com.th.mallchat.common.common.domain.enums.YesOrNoEnum;
import com.th.mallchat.common.common.service.LockService;
import com.th.mallchat.common.common.utils.AssertUtil;
import com.th.mallchat.common.user.dao.UserBackpackDao;
import com.th.mallchat.common.user.domain.entity.UserBackpack;
import com.th.mallchat.common.user.domain.enums.IdempotentEnum;
import com.th.mallchat.common.user.service.UserBackpackService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class UserBackpackServiceImpl implements UserBackpackService {

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private UserBackpackDao userBackpackDao;

    @Autowired
    private LockService lockService;

    /**
     *
     * 幂等号=itemId+source+businessId
     * @param uid            用户id
     * @param itemId         物品id
     * @param idempotentEnum 幂等类型
     * @param businessId     上层业务发送的唯一标识
     */
    @Override
    public void acquireItem(Long uid, Long itemId, IdempotentEnum idempotentEnum, String businessId) {
        //组装幂等号
        String idempotent = getIdempotent(itemId, idempotentEnum, businessId);
        UserBackpackServiceImpl proxy = (UserBackpackServiceImpl)AopContext.currentProxy();
        proxy.doAcquireItem(uid, itemId, idempotent);
    }

    @RedissonLock(key = "#idempotent", waitTime = 5000)//相同幂等如果同时发奖，需要排队等上一个执行完，取出之前数据返回
    public void doAcquireItem(Long uid, Long itemId, String idempotent) {
        UserBackpack userBackpack = userBackpackDao.getByIdempotent(idempotent);
        if (Objects.nonNull(userBackpack)){
            return;
        }
        //发放物品
        UserBackpack insert = UserBackpack.builder()
                .uid(uid)
                .itemId(itemId)
                .status(YesOrNoEnum.NO.getStatus())
                .idempotent(idempotent)
                .build();
        userBackpackDao.save(insert);
        //用户收到物品的事件
//        applicationEventPublisher.publishEvent(new ItemReceiveEvent(this, insert));
    }


    private String getIdempotent(Long itemId, IdempotentEnum idempotentEnum, String businessId) {
        return String.format("%d_%d_%s", itemId, idempotentEnum.getType(), businessId);
    }

}
