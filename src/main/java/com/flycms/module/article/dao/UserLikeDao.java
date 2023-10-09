package com.flycms.module.article.dao;

import com.flycms.core.entity.UserVo;
import com.flycms.module.article.model.UserLike;
import com.flycms.module.user.model.UserFollowRelation;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserLikeDao {


    /**
     * 取消点赞
     * @param userId
     * @param publishContentId
     */
    void deleteUserLike(@Param("userId") String userId, @Param("publishContentId") Long publishContentId);

    /**
     * 新增点赞
     * @param userLike
     */
    void addUserLike(UserLike userLike);
}
