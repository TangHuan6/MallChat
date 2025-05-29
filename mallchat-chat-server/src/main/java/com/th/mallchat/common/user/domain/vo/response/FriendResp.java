package com.th.mallchat.common.user.domain.vo.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Description: 好友校验
 * Author:
 * Date: 2023-03-23
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FriendResp {

    @ApiModelProperty("好友uid")
    private Long uid;
    /**
     * @see
     */
    @ApiModelProperty("在线状态 1在线 2离线")
    private Integer activeStatus;
}