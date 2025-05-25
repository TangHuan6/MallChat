package com.th.mallchat.common.user.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户表
 * @TableName user
 *
 */
//表示自动使用配置的 TypeHandler 类型处理器（比如 JSON 类型转换器）。默认是 false，需要你显式开启。
@TableName(value ="user",autoResultMap = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User implements Serializable {
    /**
     * 用户id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户昵称
     */
    @TableField(value = "name")
    private String name;

    /**
     * 用户头像
     */
    @TableField(value = "avatar")
    private String avatar;

    /**
     * 性别 1为男性，2为女性
     */
    @TableField(value = "sex")
    private Integer sex;

    /**
     * 微信openid用户标识
     */
    @TableField(value = "open_id")
    private String openId;

    /**
     * 在线状态 1在线 2离线
     */
    @TableField(value = "active_status")
    private Integer activeStatus;

    /**
     * 最后上下线时间
     */
    @TableField(value = "last_opt_time")
    private Date lastOptTime;

    /**
     * ip信息
     */
    //使用 Jackson JSON 序列化器将对象转为 JSON 存入数据库，或将 JSON 反序列化为 Java 对象
    @TableField(value = "ip_info",typeHandler = JacksonTypeHandler.class)
    private IpInfo ipInfo;

    /**
     * 佩戴的徽章id
     */
    @TableField(value = "item_id")
    private Long itemId;

    /**
     * 使用状态 0.正常 1拉黑
     */
    @TableField(value = "status")
    private Integer status;

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

    public void refreshIp(String ip) {
        if (ipInfo == null) {
            ipInfo = new IpInfo();
        }
        ipInfo.refreshIp(ip);
    }
}