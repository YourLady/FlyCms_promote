package com.flycms.module.article.dao;

import com.flycms.module.article.model.PublishContent;
import com.flycms.module.article.model.UserCollect;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserCollectDao {


    /**
     * 取消收藏
     * @param userId
     * @param publishContentId
     */
    void deleteUserCollect(@Param("userId") String userId, @Param("publishContentId") Long publishContentId);

    /**
     * 新增收藏
     * @param userCollect
     */
    void addUserCollect(UserCollect userCollect);

    /**
     * 查询收藏的内容
     * @param userId
     * @return
     */
    List<PublishContent> selectUserCollect(@Param("userId") String userId);

    Integer selectCollect(@Param("userId") String userId, @Param("publishContentId") Long publishContentId);
}
