package com.flycms.module.article.model;

import lombok.Data;

import java.io.Serializable;
@Data
public class SearchResult implements Serializable {

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 收藏数
     */
    private Integer collectNum;

    /**
     * 关注数
     */
    private Integer followNum;

    /**
     * 简介
     */
    private String description;

    /**
     * 关注状态
     */
    private Integer followStat = 0;

    /**
     * 标题
     */
    private String title;

    /**
     * 文章描述
     */
    private String articleDescription;

}
