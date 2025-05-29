package com.th.mallchat.common.common.event.listener;

import com.th.mallchat.common.user.domain.entity.UserApply;
import org.springframework.context.ApplicationEvent;

import java.time.Clock;

public class UserApplyEvent extends ApplicationEvent {
    private UserApply userApply;
    public UserApplyEvent(Object source, UserApply userApply) {
        super(source);
        this.userApply = userApply;
    }
}
