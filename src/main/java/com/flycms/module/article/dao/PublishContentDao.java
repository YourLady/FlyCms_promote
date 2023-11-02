package com.flycms.module.article.dao;

import com.flycms.module.article.model.PublishContent;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface PublishContentDao {

    void savePublishContent(PublishContent publishContent);

    void deletePublishContent(@Param("id") Long id);

    List<PublishContent> selectMyPublishContentList(@Param("userId") String userId);

    void addLikeCount(@Param("publishContentId") Long publishContentId);

    void minusLikeCount(@Param("publishContentId") Long publishContentId);

    void addCollectCount(@Param("publishContentId") Long publishContentId);

    void minusCollectCount(@Param("publishContentId") Long publishContentId);

    List<PublishContent> selectMyFollowContent(@Param("userList")List<String> userList,@Param("followTime") Date followTime);

    List<PublishContent> selectOneHourContent(@Param("lastOneHour") Date lastOneHour);

    List<PublishContent> selectOrderContent();

    void addCommentCount(@Param("publishContentId") Long publishContentId);
}
