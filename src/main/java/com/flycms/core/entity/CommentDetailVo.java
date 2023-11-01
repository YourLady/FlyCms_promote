package com.flycms.core.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class CommentDetailVo implements Serializable {

    /**
     * 评论内容
     */
    private String commentContent;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 评论时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date commentTime;


    /**
     * 头像
     */
    private String avatar;
}
