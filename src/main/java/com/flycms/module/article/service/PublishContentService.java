package com.flycms.module.article.service;

import com.flycms.core.entity.FollowPublishContentVo;
import com.flycms.core.entity.PublishContentVo;
import com.flycms.core.entity.UserVo;
import com.flycms.module.article.dao.PublishContentDao;
import com.flycms.module.article.dao.UserCollectDao;
import com.flycms.module.article.dao.UserLikeDao;
import com.flycms.module.article.model.PublishContent;
import com.flycms.module.article.model.UserCollect;
import com.flycms.module.article.model.UserLike;
import com.flycms.module.user.dao.UserFollowRelationDao;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 发布内容处理类
 */
@Service
public class PublishContentService {

    @Autowired
    private PublishContentDao publishContentDao;

    @Autowired
    private UserFollowRelationDao userFollowRelationDao;

    @Autowired
    private UserLikeDao userLikeDao;

    @Autowired
    private UserCollectDao userCollectDao;

    /**
     * 发布内容
     * @param publishContentVo
     */
    public void publishContent(PublishContentVo publishContentVo) {
        PublishContent publishContent = new PublishContent();
        BeanUtils.copyProperties(publishContentVo,publishContent);
        publishContent.setCreateBy(publishContentVo.getUserName());
        publishContent.setCreateTime(new Date());
        publishContent.setPublishTime(new Date());
        publishContent.setUpdateTime(new Date());
        publishContent.setUpdateBy(publishContentVo.getUserName());
        publishContentDao.savePublishContent(publishContent);
    }


    /**
     * 删除发布内容
     * @param id
     */
    public void deletePublishContent(Long id) {
        publishContentDao.deletePublishContent(id);
    }

    /**
     * 查询发布内容列表
     * @return
     */
    public List<PublishContent> queryPublishContentList(String userId) {
        List<PublishContent> result = publishContentDao.selectMyPublishContentList(userId);
        return result;
    }


    /**
     * 查询关注人发布内容列表
     * @return
     */
    public List<FollowPublishContentVo> queryFollowPublishContent(String userId) {
        List<FollowPublishContentVo> result = new ArrayList<>();
        // 查询关注的人
        List<UserVo> followUsers = userFollowRelationDao.selectFollowUser(userId);
        for (UserVo userVo : followUsers) {
            FollowPublishContentVo followPublishContentVo = new FollowPublishContentVo();
            followPublishContentVo.setUserId(userVo.getUserId());
            followPublishContentVo.setAvatar(userVo.getAvatar());
            followPublishContentVo.setNickName(userVo.getNickName());
            // 查询内容
            List<PublishContent> publishContents = publishContentDao.selectMyPublishContentList(userVo.getUserId());
            followPublishContentVo.setPublishContentList(publishContents);
            result.add(followPublishContentVo);
        }
        return result;
    }

    @Transactional
    public void likePublishContent(String userId, Long publishContentId) {
        UserLike userLike = new UserLike();
        userLike.setUserId(userId);
        userLike.setPublishContentId(publishContentId);
        userLike.setCreateBy(userId);
        userLike.setUpdateBy(userId);
        userLike.setCreateTime(new Date());
        userLike.setUpdateTime(new Date());
        userLikeDao.addUserLike(userLike);
        // 点赞数加1
        publishContentDao.addLikeCount(publishContentId);
    }

    @Transactional
    public void cancelLikePublishContent(String userId, Long publishContentId) {
        userLikeDao.deleteUserLike(userId,publishContentId);
        // 点赞数减1
        publishContentDao.minusLikeCount(publishContentId);
    }


    @Transactional
    public void collectPublishContent(String userId, Long publishContentId) {
        UserCollect userCollect = new UserCollect();
        userCollect.setUserId(userId);
        userCollect.setPublishContentId(publishContentId);
        userCollect.setCreateBy(userId);
        userCollect.setUpdateBy(userId);
        userCollect.setCreateTime(new Date());
        userCollect.setUpdateTime(new Date());
        userCollectDao.addUserCollect(userCollect);
        // 收藏数加1
        publishContentDao.addCollectCount(publishContentId);
    }

    @Transactional
    public void cancelCollectPublishContent(String userId, Long publishContentId) {
        userCollectDao.deleteUserCollect(userId,publishContentId);
        // 收藏数减1
        publishContentDao.minusCollectCount(publishContentId);
    }

    public List<PublishContent> queryCollectPublishContent(String userId) {
        List<PublishContent> result = userCollectDao.selectUserCollect(userId);
        return result;
    }
}
