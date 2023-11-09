package com.flycms.module.user.dao;

import com.flycms.core.entity.UserVo;
import com.flycms.module.user.model.UserFollowRelation;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface UserFollowRelationDao {

    /**
     * 新增关注
     * @param userFollowRelation
     */
    void addFollowRelation(UserFollowRelation userFollowRelation);

    /**
     * 取消关注
     * @param userId
     * @param followUserId
     */
    void deleteFollowRelation(@Param("userId") String userId,@Param("followUserId")  String followUserId);

    /**
     * 查询关注人列表
     * @param userId
     * @return
     */
    List<UserVo> selectFollowUser(@Param("userId") String userId);

    /**
     * 查询用户信息
     * @param userId
     * @return
     */
    UserVo selectUserByUserId(@Param("userId") String userId);

    Date selectUserByUserIdUYT(@Param("userId") String userId);
}
