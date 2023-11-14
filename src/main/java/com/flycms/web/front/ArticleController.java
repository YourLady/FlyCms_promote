package com.flycms.web.front;

import com.alibaba.fastjson.JSONArray;
import com.flycms.constant.Const;
import com.flycms.core.base.BaseController;
import com.flycms.core.entity.*;
import com.flycms.core.utils.PromoteSaveFilter;
import com.flycms.module.article.model.*;
import com.flycms.module.article.service.ArticleCategoryService;
import com.flycms.module.article.service.ArticleService;
import com.flycms.module.article.service.PublishContentService;
import com.flycms.module.other.service.FilterKeywordService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;

/**
 * 开发公司：28844.com<br/>
 * 版权：28844.com<br/>
 *
 * @author sun-kaifei
 * @version 1.0 <br/>
 * @email 79678111@qq.com
 * @Date: 19:77 2018/9/17
 */
@Controller
@Slf4j
public class ArticleController extends BaseController {
    @Autowired
    protected ArticleService articleService;
    @Autowired
    protected FilterKeywordService filterKeywordService;
    @Autowired
    protected ArticleCategoryService articleCategoryService;
    @Autowired
    protected PublishContentService publishContentService;

    //文章列表
    @GetMapping(value = {"/ac/","/ac/index","/ac/{id}" })
    public String getArticleList(@PathVariable(value = "id", required = false) String id, @RequestParam(value = "p", defaultValue = "1") int p, ModelMap modelMap){
        ArticleCategory category=null;
        if(id != null){
            if (!NumberUtils.isNumber(id)) {
                return theme.getPcTemplate("404");
            }
            category =articleCategoryService.findCategoryById(Long.parseLong(id),2);
            if(category==null){
                return theme.getPcTemplate("404");
            }
        }

        if (getUser() != null) {
            modelMap.addAttribute("user", getUser());
        }
        modelMap.addAttribute("id", id);
        modelMap.addAttribute("category", category);
        modelMap.addAttribute("p", p);
        return theme.getPcTemplate("article/index");
    }

    //文章详细页面
    @GetMapping(value = "/a/{shortUrl}")
    public String getArticleById(@PathVariable(value = "shortUrl", required = false) String shortUrl, @RequestParam(value = "p", defaultValue = "1") int p, ModelMap modelMap){
        if (StringUtils.isBlank(shortUrl)) {
            return theme.getPcTemplate("404");
        }
        //查询文章内容信息，由于缓存问题，统计要实时更新，所以统计和内容分开查询
        Article article=articleService.findArticleByShorturl(shortUrl);
        if(article==null){
            return theme.getPcTemplate("404");
        }
        //违禁词过滤
        article.setTitle(filterKeywordService.doFilter(article.getTitle()));
        article.setContent(filterKeywordService.doFilter(article.getContent()));
        //查询统计信息
        ArticleCount count=articleService.findArticleCountById(article.getId());
        article.setCountDigg(count.getCountDigg());
        article.setCountBurys(count.getCountBurys());
        article.setCountView(count.getCountView());
        article.setCountComment(count.getCountComment());
        if (getUser() != null) {
            modelMap.addAttribute("user", getUser());
        }
        modelMap.addAttribute("article", article);
        modelMap.addAttribute("p", p);
        return theme.getPcTemplate("article/detail");
    }

    //文章详细页面
    @ResponseBody
    @RequestMapping(value = "/findArticleById/{id}")
    public DataVo findArticleById(@PathVariable(value = "id", required = false) String id,ModelMap modelMap){
        DataVo data = DataVo.failure("操作失败");
        if (!NumberUtils.isNumber(id)) {
            return data = DataVo.failure("id参数错误");
        }
        Article article=articleService.findArticleById(Long.parseLong(id), 2);
        if(article==null){
            return data = DataVo.failure("该内容不存在或者未审核！");
        }
        Map<String, Object> map = new HashMap<>();
        map.put("content",article.getContent());
        return DataVo.success(map);
    }

    //添加文章
    @GetMapping(value = "/ucenter/article/add")
    public String getAddArticle(ModelMap modelMap){
        modelMap.addAttribute("user", getUser());
        return theme.getPcTemplate("article/add_article");
    }


    @ResponseBody
    @PostMapping(value = "/findPromoteArticleById/{id}")
    public DataVo findPromoteArticleById(@PathVariable(value = "id", required = false) String id){
        DataVo data = DataVo.failure("操作失败");
        if (!NumberUtils.isNumber(id)) {
            return data = DataVo.failure("id参数错误");
        }
        Article article=articleService.findArticleByIdPromote(Long.parseLong(id));
        if(article==null){
            return data = DataVo.failure("该内容不存在或者未审核！");
        }
        return DataVo.jump("操作成功", null,article);
    }

    @ResponseBody
    @PostMapping(value = "/findPromoteArticleListById/{promoteid}")
    public DataVo findPromoteArticleListById(@PathVariable(value = "promoteid", required = false) String promoteid){
        DataVo data = DataVo.failure("操作失败");
        if (!NumberUtils.isNumber(promoteid)) {
            return data = DataVo.failure("id参数错误");
        }
        List<Article> article=articleService.findPromoteArticleListById(Long.parseLong(promoteid));
        if(article==null){
            return data = DataVo.failure("该内容不存在或者未审核！");
        }
        return DataVo.jump("操作成功", null,article);
    }



    //保存添加文章
    @PostMapping("/ucenter/article/article_save")
    @ResponseBody
    public DataVo addAdminSave(@Valid Article article, BindingResult result){
        DataVo data = DataVo.failure("操作失败");
        try {
            if (result.hasErrors()) {
                List<ObjectError> list = result.getAllErrors();
                for (ObjectError error : list) {
                    return DataVo.failure(error.getDefaultMessage());
                }
                return null;
            }
            article.setUserId(getUser().getUserId());
            data = articleService.addArticle(article);
            PromoteSaveFilter util = new PromoteSaveFilter();
            util.SaveFileToFolder("/www/wwwroot/Vender_folder/",article,userService);
        } catch (Exception e) {
            data = DataVo.failure(e.getMessage());
        }
        return data;
    }

    @PostMapping("/user/promote/addPromoteSave")
    @ResponseBody
    public DataVo addPromoteSave(@RequestBody PromoteVo promote){
        //DataVo data = DataVo.failure("操作失败");
        try {
            Promote promote1 = new Promote();

            BeanUtils.copyProperties(promote,promote1);
            articleService.addPromote(promote1);

        } catch (Exception e) {
            return DataVo.failure(e.getMessage());
        }
        return DataVo.success("发布成功！");
    }

    //添加评论
    @PostMapping("/user/promote/addArticle")
    @ResponseBody
    public DataVo addPromoteArticle(@RequestBody ArticleVo ArticleVo){
        DataVo data = DataVo.failure("操作失败");
        try {
            Article article = new Article();
            BeanUtils.copyProperties(ArticleVo,article);
            if(StringUtils.isBlank(article.getUserId().toString()))
            {
                article.setUserId(getUser().getUserId());
            }

            data = articleService.addArticle2(article);
            //PromoteSaveFilter util = new PromoteSaveFilter();
           // util.SaveFileToFolder(Const.File_PATH,article,userService);
        } catch (Exception e) {
            data = DataVo.failure(e.getMessage());
        }
        return data;
    }

    @PostMapping("/user/promote/editArticle")
    @ResponseBody
    public DataVo editPromoteArticle(@RequestBody ArticleVo ArticleVo){
        DataVo data = DataVo.failure("操作失败");
        try {
            Article article = new Article();
            BeanUtils.copyProperties(ArticleVo,article);
            if(StringUtils.isBlank(article.getUserId().toString()))
            {
                article.setUserId(getUser().getUserId());
            }

            data = articleService.editArticleById2(article);
            //PromoteSaveFilter util = new PromoteSaveFilter();
           // util.EditFileToFolder(Const.File_PATH,article,userService);
        } catch (Exception e) {
            data = DataVo.failure(e.getMessage());
        }
        return data;
    }

    //保存用户文章评论内容
    @PostMapping("/ucenter/article/comment_save")
    @ResponseBody
    public DataVo addArticleComment(@Valid ArticleComment articleComment, BindingResult result){
        DataVo data = DataVo.failure("操作失败");
        try {
            if (result.hasErrors()) {
                List<ObjectError> list = result.getAllErrors();
                for (ObjectError error : list) {
                    return DataVo.failure(error.getDefaultMessage());
                }
                return null;
            }
            articleComment.setUserId(getUser().getUserId());
            data = articleService.addArticleComment(articleComment);
        } catch (Exception e) {
            data = DataVo.failure(e.getMessage());
        }
        return data;
    }

    //修改文章
    @GetMapping(value = "/ucenter/article/edit-{id}")
    public String getEditArticle(@PathVariable(value = "id", required = false) String id,ModelMap modelMap){
        if (!NumberUtils.isNumber(id)) {
            return theme.getPcTemplate("404");
        }
        Article article=articleService.findArticleById(Long.parseLong(id), 0);
        if(article==null){
            return theme.getPcTemplate("404");
        }
        if (!getUser().getUserId().equals(article.getUserId())) {
            modelMap.addAttribute("message", "只能修改属于自己的文章！");
            return theme.getPcTemplate("message_tip");
        }
        //article.setContent(StringEscapeUtils.escapeHtml4(article.getContent()));
        modelMap.addAttribute("article", article);
        modelMap.addAttribute("user", getUser());
        return theme.getPcTemplate("article/edit_article");
    }
    @PostMapping(value = "/user/article/delete")
    @ResponseBody
    public DataVo delArticle(@RequestParam(value = "id", required = false) String id){
        DataVo data = DataVo.failure("操作失败");
        data = articleService.deleteArticleById(Long.valueOf(id));
        return data;
    }
    @PostMapping(value = "/user/promote/delete")
    @ResponseBody
    public DataVo delpromote(@RequestParam(value = "id", required = false) String id){
        DataVo data = DataVo.failure("操作失败");
        data = articleService.deletePromoteById(Long.valueOf(id));
        return data;
    }


    //收藏发布内容
    @PostMapping("/user/promotecollect")
    @ResponseBody
    public DataVo collectPromote(@RequestParam(value = "userId") String userId, @RequestParam(value = "promoteid") Long promoteid){
        try {
            articleService.collectPromote(userId,promoteid);
        } catch (Exception e) {
            log.error("收藏失败，原因:{}",e);
            return DataVo.failure("收藏失败！");
        }
        return DataVo.success("收藏成功！");
    }

    //取消收藏发布内容
    @PostMapping("/user/cancelPromoteCollect")
    @ResponseBody
    public DataVo cancelCollectPromote(@RequestParam(value = "userId") String userId, @RequestParam(value = "promoteid") Long promoteid){
        try {
            articleService.cancelcollectPromote(userId,promoteid);
        } catch (Exception e) {
            log.error("取消收藏失败，原因:{}",e);
            return DataVo.failure("取消收藏失败！");
        }
        return DataVo.success("取消收藏成功！");
    }

    //查询收藏内容列表
    @PostMapping("/user/query/collectPromote")
    @ResponseBody
    public DataVo queryCollectPromote(@RequestParam(value = "userId", required = false) String userId){
        JSONArray result = new JSONArray();
        try {
            result = articleService.queryCollectPublishContent(userId);
        } catch (Exception e) {
            log.error("查询收藏内容列表失败，原因:{}",e);
            return DataVo.failure("查询收藏内容列表失败！");
        }
        return DataVo.success(result);
    }



    @PostMapping(value = "/ucenter/article/public-{id}-{public_flag}")//0-可视  1-非公
    @ResponseBody
    public void ArticlePublicFlag(@PathVariable(value = "id", required = false) String id,@PathVariable(value = "public_flag", required = false) String public_flag){
        articleService.updateArticlePublicFlag(Long.valueOf(id),Integer.valueOf(public_flag));
        return ;
    }
    @GetMapping(value = "/ucenter/article/search-{id}-{shortUrl}")
    public String ArticleUserSearch(@PathVariable(value = "id", required = false) String id,@PathVariable(value = "shortUrl", required = false) String shortUrl,ModelMap modelMap){

        //查询文章内容信息，由于缓存问题，统计要实时更新，所以统计和内容分开查询
        Article article=articleService.findArticleByUserShorturl(shortUrl,Long.valueOf(id));
        if(article==null){
            return theme.getPcTemplate("404");
        }
        //违禁词过滤
        article.setTitle(filterKeywordService.doFilter(article.getTitle()));
        article.setContent(filterKeywordService.doFilter(article.getContent()));
        //查询统计信息
        ArticleCount count=articleService.findArticleCountById(article.getId());
        article.setCountDigg(count.getCountDigg());
        article.setCountBurys(count.getCountBurys());
        article.setCountView(count.getCountView());
        article.setCountComment(count.getCountComment());
        if (getUser() != null) {
            modelMap.addAttribute("user", getUser());
        }
        modelMap.addAttribute("article", article);
        modelMap.addAttribute("p", "");
        return theme.getPcTemplate("article/detail");
    }
    //保存修改文章
    @PostMapping("/ucenter/article/article_update")
    @ResponseBody
    public DataVo editArticleById(@Valid Article article, BindingResult result){
        DataVo data = DataVo.failure("操作失败");
        try {
            if (result.hasErrors()) {
                List<ObjectError> list = result.getAllErrors();
                for (ObjectError error : list) {
                    return DataVo.failure(error.getDefaultMessage());
                }
                return null;
            }
            Article info=articleService.findArticleById(article.getId(),0);
            if(info==null){
                return data=DataVo.failure("该内容不存在！");
            }
            if (!getUser().getUserId().equals(info.getUserId())) {
                return data=DataVo.failure("只能修改属于自己的文章！");
            }
            article.setUserId(getUser().getUserId());
            article.setShortUrl(info.getShortUrl());
            data = articleService.editArticleById(article);
            PromoteSaveFilter util = new PromoteSaveFilter();
            util.EditFileToFolder("/www/wwwroot/Vender_folder/",article,userService);
        } catch (Exception e) {
            data = DataVo.failure(e.getMessage());
        }
        return data;
    }

    //按父级id查询id下所有分类列表
    @ResponseBody
    @RequestMapping(value = "/ucenter/article/category_child")
    public List<ArticleCategory> getCategoryChildList(@RequestParam(value = "parentId", defaultValue = "0") Long parentId){
        List<ArticleCategory> list=articleCategoryService.getCategoryListByFatherId(parentId);
        return list;
    }

    //文章浏览量更新
    @ResponseBody
    @GetMapping(value = "/article/viewcount")
    public DataVo updateArticleByViewCount(@RequestParam(value = "id", required = false) String id) {
        DataVo data = DataVo.failure("操作失败");
        try {
            if (!NumberUtils.isNumber(id)) {
                return data=DataVo.failure("文章参数错误");
            }
            articleService.updateArticleViewCount(Long.parseLong(id));
        } catch (Exception e) {
            data = DataVo.failure(e.getMessage());
        }
        return data;
    }

    //处理顶（用户推荐）信息
    @ResponseBody
    @PostMapping(value = "/article/digg")
    public DataVo articleDigg(@RequestParam(value = "id", required = false) String id,@RequestParam(value = "type", defaultValue = "0") String type) {
        DataVo data = DataVo.failure("操作失败");
        try {
            if(!StringUtils.isBlank(id)){
                if (!NumberUtils.isNumber(id)) {
                    return data=DataVo.failure("id参数错误");
                }
            }else{
                return data=DataVo.failure("ID不能为空！");
            }
            if(!StringUtils.isBlank(type)){
                if (!NumberUtils.isNumber(type)) {
                    return data=DataVo.failure("信息类型参数错误");
                }
            }else{
                return data=DataVo.failure("信息类型不能为空！");
            }
            if(getUser()==null){
                return data=DataVo.failure("请登陆后关注");
            }
            ArticleVotes articleVotes=new ArticleVotes();
            articleVotes.setInfoType(Integer.valueOf(type));
            articleVotes.setUserId(getUser().getUserId());
            articleVotes.setInfoId(Long.parseLong(id));
            articleVotes.setDigg(1);
            data=articleService.updateArticleVotesById(articleVotes);
        } catch (Exception e) {
            data = DataVo.failure(e.getMessage());
        }
        return data;
    }

    //保存发布内容
    @PostMapping("/user/save/publishContent")
    @ResponseBody
    public DataVo savePublishContent(@RequestBody PublishContentVo publishContentVo){
        try {
            publishContentService.publishContent(publishContentVo);
        } catch (Exception e) {
            log.error("发布失败，原因:{}",e);
            return DataVo.failure("发布失败！");
        }
        return DataVo.success("发布成功！");
    }

    //删除发布内容
    @GetMapping("/user/delete/publishContent")
    @ResponseBody
    public DataVo deletePublishContent(@RequestParam(value = "id", required = false) Long id){
        try {
            publishContentService.deletePublishContent(id);
        } catch (Exception e) {
            log.error("删除失败，原因:{}",e);
            return DataVo.failure("删除失败！");
        }
        return DataVo.success("删除成功！");
    }

    //查询发布内容列表
    @GetMapping("/user/query/publishContentList")
    @ResponseBody
    public DataVo queryPublishContentList(@RequestParam(value = "userId", required = false) String userId){
        List<PublishContent> result = new ArrayList<>();
        try {
            result = publishContentService.queryPublishContentList(userId);
        } catch (Exception e) {
            log.error("查询列表失败，原因:{}",e);
            return DataVo.failure("查询列表失败！");
        }
        return DataVo.success(result);
    }


    //查询关注人内容列表
    @GetMapping("/user/query/followPublishContent")
    @ResponseBody
    public DataVo queryFollowPublishContent(@RequestParam(value = "userId", required = false) String userId){
        List<PublishContent> result = new ArrayList<>();
        try {
            result = publishContentService.queryFollowPublishContent(userId);
        } catch (Exception e) {
            log.error("查询关注人内容列表失败，原因:{}",e);
            return DataVo.failure("查询关注人内容列表失败！");
        }
        return DataVo.success(result);
    }

    //点赞发布内容
    @PostMapping("/user/like")
    @ResponseBody
    public DataVo likePublishContent(@RequestParam(value = "userId") String userId,
                                     @RequestParam(value = "publishContentId") Long publishContentId){
        try {
            publishContentService.likePublishContent(userId,publishContentId);
        } catch (Exception e) {
            log.error("点赞失败，原因:{}",e);
            return DataVo.failure("点赞失败！");
        }
        return DataVo.success("点赞成功！");
    }

    //取消点赞发布内容
    @PostMapping("/user/cancelLike")
    @ResponseBody
    public DataVo cancelLikePublishContent(@RequestParam(value = "userId") String userId,
                                     @RequestParam(value = "publishContentId") Long publishContentId){
        try {
            publishContentService.cancelLikePublishContent(userId,publishContentId);
        } catch (Exception e) {
            log.error("取消点赞失败，原因:{}",e);
            return DataVo.failure("取消点赞失败！");
        }
        return DataVo.success("取消点赞成功！");
    }

    //收藏发布内容
    @PostMapping("/user/collect")
    @ResponseBody
    public DataVo collectPublishContent(@RequestParam(value = "userId") String userId,
                                     @RequestParam(value = "publishContentId") Long publishContentId){
        try {
            publishContentService.collectPublishContent(userId,publishContentId);
        } catch (Exception e) {
            log.error("收藏失败，原因:{}",e);
            return DataVo.failure("收藏失败！");
        }
        return DataVo.success("收藏成功！");
    }

    //取消收藏发布内容
    @PostMapping("/user/cancelCollect")
    @ResponseBody
    public DataVo cancelCollectPublishContent(@RequestParam(value = "userId") String userId,
                                           @RequestParam(value = "publishContentId") Long publishContentId){
        try {
            publishContentService.cancelCollectPublishContent(userId,publishContentId);
        } catch (Exception e) {
            log.error("取消收藏失败，原因:{}",e);
            return DataVo.failure("取消收藏失败！");
        }
        return DataVo.success("取消收藏成功！");
    }

    //查询收藏内容列表
    @GetMapping("/user/query/collectPublishContent")
    @ResponseBody
    public DataVo queryCollectPublishContent(@RequestParam(value = "userId", required = false) String userId){
        List<PublishContent> result = new ArrayList<>();
        try {
            result = publishContentService.queryCollectPublishContent(userId);
        } catch (Exception e) {
            log.error("查询收藏内容列表失败，原因:{}",e);
            return DataVo.failure("查询收藏内容列表失败！");
        }
        return DataVo.success(result);
    }


    //查询推荐动态列表
    @GetMapping("/user/query/recommendPublishContent")
    @ResponseBody
    public DataVo recommendPublishContent(@RequestParam(value = "userId", required = false) String userId){
        List<PublishContent> result = new ArrayList<>();
        try {
            result = publishContentService.recommendPublishContent(userId);
        } catch (Exception e) {
            log.error("查询推荐列表失败，原因:{}",e);
            return DataVo.failure("查询推荐列表失败！");
        }
        return DataVo.success(result);
    }

    //添加评论
    @PostMapping("/user/addComment")
    @ResponseBody
    public DataVo addComment(@RequestBody CommentVo commentVo){
        try {
            publishContentService.addComment(commentVo);
        } catch (Exception e) {
            log.error("添加评论失败，原因:{}",e);
            return DataVo.failure("评论失败！");
        }
        return DataVo.success("评论成功！");
    }

    /**
     * 搜索
     * @param keyword
     * @param type 1-用户 2-标题
     * @return
     */
    @GetMapping("/user/search")
    @ResponseBody
    public DataVo search(@RequestParam(value = "keyword", required = false) String keyword,
                         @RequestParam(value = "type", required = false) Integer type,
                         @RequestParam(value = "userId", required = false) String userId){
        List<SearchResult> result = new ArrayList<>();
        try {
            result = publishContentService.search(keyword,type,userId);
        } catch (Exception e) {
            log.error("搜索失败，原因:{}",e);
            return DataVo.failure("搜索失败！");
        }
        return DataVo.success(result);
    }
}
