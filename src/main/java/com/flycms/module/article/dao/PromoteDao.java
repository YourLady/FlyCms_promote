package com.flycms.module.article.dao;

import com.flycms.module.article.model.Promote;
import com.flycms.module.article.model.UserLike;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromoteDao {



    public int deletePromote(@Param("id") Long id);

    public List<Promote> getPromoteListPer(@Param("userId") Long userId,@Param("publicflag")Integer publicflag);
    public int addPromote(Promote promote);
    public int checkPromoteByTitle(@Param("title") String title,@Param("userId") Long userId,@Param("id") Long id);

    Integer selectLike(@Param("userId") String userId, @Param("publishContentId") Long publishContentId);
}
