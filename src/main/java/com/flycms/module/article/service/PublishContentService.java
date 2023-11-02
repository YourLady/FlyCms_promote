package com.flycms.module.article.service;

import cn.hutool.core.collection.CollectionUtil;
import com.flycms.core.entity.*;
import com.flycms.core.utils.DateUtils;
import com.flycms.module.article.dao.PublishContentDao;
import com.flycms.module.article.dao.UserCollectDao;
import com.flycms.module.article.dao.UserCommentDao;
import com.flycms.module.article.dao.UserLikeDao;
import com.flycms.module.article.model.PublishContent;
import com.flycms.module.article.model.UserCollect;
import com.flycms.module.article.model.UserComment;
import com.flycms.module.article.model.UserLike;
import com.flycms.module.user.dao.UserFollowRelationDao;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

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

    @Autowired
    private UserCommentDao userCommentDao;

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
        for (PublishContent publishContent : result) {
            UserVo userVo = userFollowRelationDao.selectUserByUserId(publishContent.getUserId());
            publishContent.setAvatar(userVo.getAvatar());
            publishContent.setUserName(userVo.getNickName());
            Integer likeCount = userLikeDao.selectLike(userId,publishContent.getId());
            if (likeCount > 0){
                publishContent.setLikeStat(1);
            }
            Integer collectCount = userCollectDao.selectCollect(userId,publishContent.getId());
            if (collectCount > 0){
                publishContent.setCollectStat(1);
            }
        }
        // 查询内容评论
        getComments(result);
        return result;
    }

    private void getComments(List<PublishContent> result) {
        for (PublishContent publishContent : result) {
            List<UserComment> comments = userCommentDao.selectByContentId(publishContent.getId());
            List<CommentDetailVo> commentList = new ArrayList<>();
            for (UserComment userComment : comments) {
                UserVo userVo = userFollowRelationDao.selectUserByUserId(userComment.getUserId());
                CommentDetailVo commentDetailVo = new CommentDetailVo();
                commentDetailVo.setCommentContent(userComment.getCommentContent());
                commentDetailVo.setUserName(userVo.getNickName());
                commentDetailVo.setAvatar(userVo.getAvatar());
                commentDetailVo.setCommentTime(userComment.getCreateTime());
                commentList.add(commentDetailVo);
            }
            publishContent.setCommentList(commentList);
        }
    }


    /**
     * 查询关注人发布内容列表
     * @return
     */
    public List<PublishContent> queryFollowPublishContent(String userId) {
        List<PublishContent> result = new ArrayList<>();
        // 查询关注的人
        List<UserVo> followUsers = userFollowRelationDao.selectFollowUser(userId);
        for (UserVo userVo : followUsers) {
            FollowPublishContentVo followPublishContentVo = new FollowPublishContentVo();
            // 查询内容
            List<PublishContent> publishContents = publishContentDao.selectMyPublishContentList(userVo.getUserId());
            for (PublishContent publishContent : publishContents) {
                publishContent.setUserName(userVo.getNickName());
                publishContent.setAvatar(userVo.getAvatar());
                Integer likeCount = userLikeDao.selectLike(userId,publishContent.getId());
                if (likeCount > 0){
                    publishContent.setLikeStat(1);
                }
                Integer collectCount = userCollectDao.selectCollect(userId,publishContent.getId());
                if (collectCount > 0){
                    publishContent.setCollectStat(1);
                }
            }
            getComments(publishContents);
            followPublishContentVo.setPublishContentList(publishContents);
            result.addAll(publishContents);
        }
        // 根据发布时间排序
        List<PublishContent> collect = result.stream().sorted(Comparator.comparing(PublishContent::getPublishTime).reversed()).collect(Collectors.toList());
        return collect;
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
    public void collectPublishContent(String userId, Long publishContentId) throws Exception{
        List<PublishContent> publishContents = userCollectDao.selectUserCollect(userId);
        List<Long> contentId = publishContents.stream().map(PublishContent::getId).collect(Collectors.toList());
        if (contentId.contains(publishContentId)){
            throw new Exception("不可重复收藏");
        }
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
        for (PublishContent publishContent : result) {
            UserVo userVo = userFollowRelationDao.selectUserByUserId(publishContent.getUserId());
            publishContent.setAvatar(userVo.getAvatar());
            publishContent.setUserName(userVo.getNickName());
            Integer likeCount = userLikeDao.selectLike(userId,publishContent.getId());
            if (likeCount > 0){
                publishContent.setLikeStat(1);
            }
            Integer collectCount = userCollectDao.selectCollect(userId,publishContent.getId());
            if (collectCount > 0){
                publishContent.setCollectStat(1);
            }
        }
        return result;
    }


    public List<PublishContent> recommendPublishContent(String userId) throws ParseException {
        List<PublishContent> result = new ArrayList<>();
        // 获取15分钟前时间
        Date followTime = DateUtils.addMin(-15);
        List<UserVo> userVos = userFollowRelationDao.selectFollowUser(userId);
        List<String> userList = userVos.stream().map(UserVo::getUserId).collect(Collectors.toList());
        if (CollectionUtil.isNotEmpty(userList)){
            List<PublishContent> followContents = publishContentDao.selectMyFollowContent(userList,followTime);
            followContents.forEach(item-> item.setFollowStat(1));
            result.addAll(followContents);
        }
        // 最近1小时发布
        Date lastOneHour = DateUtils.addMin(-60);
        List<PublishContent> oneHourContents = publishContentDao.selectOneHourContent(lastOneHour);
        for (PublishContent publishContent : oneHourContents) {
            if (userList.contains(publishContent.getUserId())){
                publishContent.setFollowStat(1);
            }
        }
        result.addAll(oneHourContents);
        // 收藏，评论，点赞排序
        List<PublishContent> orderContents = publishContentDao.selectOrderContent();
        for (PublishContent publishContent : orderContents) {
            if (userList.contains(publishContent.getUserId())){
                publishContent.setFollowStat(1);
            }
        }
        result.addAll(orderContents);
        List<PublishContent> publishContents = new ArrayList<>(new LinkedHashSet<>(result));
        for (PublishContent publishContent : publishContents) {
            UserVo userVo = userFollowRelationDao.selectUserByUserId(publishContent.getUserId());
            Integer likeCount = userLikeDao.selectLike(userId,publishContent.getId());
            if (likeCount > 0){
                publishContent.setLikeStat(1);
            }
            Integer collectCount = userCollectDao.selectCollect(userId,publishContent.getId());
            if (collectCount > 0){
                publishContent.setCollectStat(1);
            }
            publishContent.setAvatar(userVo.getAvatar());
            publishContent.setUserName(userVo.getNickName());
        }
        getComments(publishContents);
        return publishContents;
    }

    public void addComment(CommentVo commentVo) {
        UserComment userComment = new UserComment();
        userComment.setPublishContentId(commentVo.getContentId());
        userComment.setCommentContent(commentVo.getCommentContent());
        userComment.setUserId(commentVo.getUserId());
        userComment.setCreateBy(commentVo.getUserName());
        userComment.setUpdateBy(commentVo.getUserName());
        userComment.setUpdateTime(new Date());
        userComment.setCreateTime(new Date());
        userCommentDao.addComment(userComment);
        publishContentDao.addCommentCount(commentVo.getContentId());
    }
}
