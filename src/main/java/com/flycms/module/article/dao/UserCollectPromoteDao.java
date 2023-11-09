package com.flycms.module.article.dao;

import com.flycms.module.article.model.Promote;
import com.flycms.module.article.model.PublishContent;
import com.flycms.module.article.model.UserCollect;
import com.flycms.module.article.model.UserCollectPromote;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserCollectPromoteDao {


    /**
     * 取消收藏
     * @param userId
     * @param publishContentId
     */
    void deleteUserCollectPromote(@Param("userId") String userId, @Param("promoteid") Long promoteid);

    /**
     * 新增收藏
     * @param userCollect
     */
    void addUserCollectPromote(UserCollectPromote userCollect);

    /**
     * 查询收藏的内容
     *
     * @param userId
     * @return
     */
    List<Promote> selectUserCollectPromote(@Param("userId") String userId);

    Integer selectCollect(@Param("userId") String userId, @Param("publishContentId") Long publishContentId);
}
