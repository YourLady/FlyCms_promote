package com.flycms.module.article.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class PublishContent implements Serializable{

    private Long id;

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
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 评论数
     */
    private Integer commentCount;

    /**
     * 收藏数
     */
    private Integer collectCount;

    /**
     * 是否公开 0-否 1-是
     */
    private Integer isPublic;

    /**
     * 发布时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date publishTime;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /**
     * 评论ID列表
     */
    private List<Long> commentList;
}
