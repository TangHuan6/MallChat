package com.th.mallchat.common.common.event.listener;

import com.th.mallchat.common.common.event.UserRegisterEvent;
import com.th.mallchat.common.user.dao.UserDao;
import com.th.mallchat.common.user.domain.entity.User;
import com.th.mallchat.common.user.domain.enums.IdempotentEnum;
import com.th.mallchat.common.user.domain.enums.ItemEnum;
import com.th.mallchat.common.user.service.UserBackpackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Spring 的事件驱动模型是基于观察者模式（Observer Pattern）设计的，允许发布者和订阅者解耦：
 *事件（ApplicationEvent）：表示发生的事情
 *事件发布器（ApplicationEventPublisher）：发布事件
 *事件监听器（EventListener）：订阅并处理事件
 */

@Component
public class UserRegisterListener {


    @Autowired
    private UserBackpackService userBackpackService;

    @Autowired
    private UserDao userDao;

    /**
     *
     * @param event
     * @throws InterruptedException
     *
     * @EventListener 	❌ 不受事务控制，立即响应 事件发布时立即执行
     * @TransactionalEventListener ✅ 受事务控制，根据事务状态决定是否触发 默认在事务 提交后 才执行
     * BEFORE_COMMIT：事务提交之前触发
     * AFTER_COMMIT（默认）：事务成功提交后触发
     * AFTER_ROLLBACK：事务回滚后触发
     * AFTER_COMPLETION：无论成功或失败，只要事务结束就触发
     */
    @Async
    @TransactionalEventListener(classes = UserRegisterEvent.class,phase = TransactionPhase.AFTER_COMMIT)
    public void sendCard(UserRegisterEvent event) throws InterruptedException {
        User user = event.getUser();
        userBackpackService.acquireItem(user.getId(), ItemEnum.MODIFY_NAME_CARD.getId(), IdempotentEnum.UID, user.getId().toString());
    }

    @Async
    @EventListener(classes = UserRegisterEvent.class)
    public void sendBadge(UserRegisterEvent event) throws InterruptedException {
        User user = event.getUser();
        int count = userDao.count();// 性能瓶颈，等注册用户多了直接删掉
        if (count <= 10) {
            userBackpackService.acquireItem(user.getId(), ItemEnum.REG_TOP10_BADGE.getId(), IdempotentEnum.UID, user.getId().toString());
        } else if (count <= 100) {
            userBackpackService.acquireItem(user.getId(), ItemEnum.REG_TOP100_BADGE.getId(), IdempotentEnum.UID, user.getId().toString());
        }
    }

}
