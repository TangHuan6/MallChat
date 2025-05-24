package com.th.mallchat.common.user.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户背包表
 * @TableName user_backpack
 */
@TableName(value ="user_backpack")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserBackpack implements Serializable {
    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * uid
     */
    @TableField(value = "uid")
    private Long uid;

    /**
     * 物品id
     */
    @TableField(value = "item_id")
    private Long itemId;

    /**
     * 使用状态 0.未失效 1失效
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 幂等号
     */
    @TableField(value = "idempotent")
    private String idempotent;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    /**
     * 修改时间
     */
    @TableField(value = "update_time")
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}