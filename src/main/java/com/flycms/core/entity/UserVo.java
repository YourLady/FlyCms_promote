package com.flycms.core.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserVo implements Serializable{

    /**
     * 用户id
     */
    private String userId;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 用户昵称
     */
    private String nickName;
}
