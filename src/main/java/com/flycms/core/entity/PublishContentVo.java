package com.flycms.core.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class PublishContentVo implements Serializable{

    /**
     * 用户id
     */
    private String userId;

    /**
     * 引用的提示词ID
     */
    private String promptId;

    /**
     * 发布内容标题
     */
    private String title;

    /**
     * 发布内容
     */
    private String content;

    /**
     * 是否公开 0-否 1-是
     */
    private Integer isPublic;

    /**
     * 用户名称
     */
    private String userName;
}
