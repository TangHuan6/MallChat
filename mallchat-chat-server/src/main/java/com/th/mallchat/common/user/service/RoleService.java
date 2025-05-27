package com.th.mallchat.common.user.service;

import com.th.mallchat.common.user.domain.enums.RoleEnum;

public interface RoleService {
    /**
     * 是否有某个权限，临时做法
     *
     * @return
     */
    boolean hasPower(Long uid, RoleEnum roleEnum);
}
