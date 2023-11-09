package com.flycms.module.article.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class Promote implements Serializable{
    private Long id;
    /**
     * 用户ID
     */
    private Long userId;
    //文章标题
    private String title;
    //文章标题
    private String picture;

    private String description;

    private Integer publicflag;
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
}
