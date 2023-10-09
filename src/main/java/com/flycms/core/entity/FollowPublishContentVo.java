package com.flycms.core.entity;

import com.flycms.module.article.model.PublishContent;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class FollowPublishContentVo implements Serializable{

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 昵称
     */
    private String nickName;

    /**
     * 发布内容
     */
    private List<PublishContent> publishContentList;
}
