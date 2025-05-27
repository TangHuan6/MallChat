package com.th.mallchat.common.common.event.listener;


import com.th.mallchat.common.common.event.UserBlackEvent;
import com.th.mallchat.common.user.dao.UserDao;
import com.th.mallchat.common.user.domain.vo.response.WSBlack;
import com.th.mallchat.common.user.service.cache.UserCache;
import com.th.mallchat.common.websocket.domain.enums.WSRespTypeEnum;
import com.th.mallchat.common.websocket.domain.vo.response.WSBaseResp;
import com.th.mallchat.common.websocket.service.WebSocketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 用户拉黑监听器
 *
 * @author zhongzb create on 2022/08/26
 */
@Slf4j
@Component
public class UserBlackListener {
    @Autowired
    private WebSocketService webSocketService;
    @Autowired
    private UserCache userCache;
    @Autowired
    private UserDao userDao;

    @Async
    @TransactionalEventListener(classes = UserBlackEvent.class,phase = TransactionPhase.AFTER_COMMIT)
    public void sendPush(UserBlackEvent event) {
        Long uid = event.getUser().getId();
        WSBaseResp<WSBlack> resp = new WSBaseResp<>();
        WSBlack wsBlack = new WSBlack(uid);
        resp.setData(wsBlack);
        resp.setType(WSRespTypeEnum.BLACK.getType());
        webSocketService.sendToAllOnline(resp,uid);
    }

    @Async
    @TransactionalEventListener(classes = UserBlackEvent.class,phase = TransactionPhase.AFTER_COMMIT)
    public void changeUserStatus(UserBlackEvent event) {
        userDao.invalidUid(event.getUser().getId());
    }
}
