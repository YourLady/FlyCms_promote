package com.flycms.core.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class CommentVo implements Serializable {

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 内容ID
     */
    private Long contentId;

    /**
     * 评论内容
     */
    private String commentContent;

    /**
     * 用户名称
     */
    private String userName;
}
