package com.flycms.module.article.dao;

import com.flycms.module.article.model.UserComment;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserCommentDao {


    /**
     * 添加评论
     * @param userComment
     */
    void addComment(UserComment userComment);

    /**
     * 查询评论列表
     * @param id
     * @return
     */
    List<UserComment> selectByContentId(Long id);
}
