package com.th.mallchat.common.user.domain.vo.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BlackReq {

    @NotNull
    @ApiModelProperty("拉黑目标uid")
    private Long uid;

}