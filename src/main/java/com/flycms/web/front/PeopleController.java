package com.flycms.web.front;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.flycms.core.base.BaseController;
import com.flycms.core.entity.DataVo;
import com.flycms.module.article.model.Article;
import com.flycms.module.article.model.Promote;
import com.flycms.module.article.service.ArticleService;
import com.flycms.module.user.model.User;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

/**
 * Open source house, All rights reserved
 * 版权：28844.com<br/>
 * 开发公司：28844.com<br/>
 *
 * @author sun-kaifei
 * @version 1.0 <br/>
 * @email 79678111@qq.com
 * @Date: 22:20 2018/9/13
 */
@Controller
public class PeopleController extends BaseController {
    private static Logger logger = LoggerFactory.getLogger(PeopleController.class);

    //用户首页页面
    @GetMapping(value = "/people/{shortUrl}")
    public String people(@RequestParam(value = "p", defaultValue = "1") int p, @PathVariable(value = "shortUrl", required = false) String shortUrl, ModelMap modelMap){
        if (StringUtils.isBlank(shortUrl)) {
            return theme.getPcTemplate("404");
        }
        User people=userService.findUserByShorturl(shortUrl);
        if(people==null){
            return theme.getPcTemplate("404");
        }
        if (getUser() != null) {
            modelMap.addAttribute("user", getUser());
        }
        modelMap.addAttribute("p", p);
        modelMap.addAttribute("people", people);
        modelMap.addAttribute("count", userService.findUserCountById(people.getUserId()));
        return theme.getPcTemplate("/people/index");
    }

    //用户问题列表页面
    @GetMapping(value = "/people/{shortUrl}/question")
    public String peopleQuestion(@RequestParam(value = "p", defaultValue = "1") int p, @PathVariable(value = "shortUrl", required = false) String shortUrl, ModelMap modelMap){
        if (StringUtils.isBlank(shortUrl)) {
            return theme.getPcTemplate("404");
        }
        User people=userService.findUserByShorturl(shortUrl);
        if(people==null){
            return theme.getPcTemplate("404");
        }
        if (getUser() != null) {
            modelMap.addAttribute("user", getUser());
        }
        modelMap.addAttribute("p", p);
        modelMap.addAttribute("people", people);
        modelMap.addAttribute("count", userService.findUserCountById(people.getUserId()));
        return theme.getPcTemplate("/people/list_question");
    }

    //用户问题列表页面
    @GetMapping(value = "/people/{shortUrl}/answers")
    public String peopleAnswers(@RequestParam(value = "p", defaultValue = "1") int p, @PathVariable(value = "shortUrl", required = false) String shortUrl, ModelMap modelMap){
        if (StringUtils.isBlank(shortUrl)) {
            return theme.getPcTemplate("404");
        }
        User people=userService.findUserByShorturl(shortUrl);
        if(people==null){
            return theme.getPcTemplate("404");
        }
        if (getUser() != null) {
            modelMap.addAttribute("user", getUser());
        }
        modelMap.addAttribute("p", p);
        modelMap.addAttribute("people", people);
        modelMap.addAttribute("count", userService.findUserCountById(people.getUserId()));
        return theme.getPcTemplate("/people/list_answers");
    }

    //用户问题列表页面
    @GetMapping(value = "/people/{shortUrl}/article")
    public String peopleArticle(@RequestParam(value = "p", defaultValue = "1") int p, @PathVariable(value = "shortUrl", required = false) String shortUrl, ModelMap modelMap){
        if (StringUtils.isBlank(shortUrl)) {
            return theme.getPcTemplate("404");
        }
        User people=userService.findUserByShorturl(shortUrl);
        if(people==null){
            return theme.getPcTemplate("404");
        }
        if (getUser() != null) {
            modelMap.addAttribute("user", getUser());
        }
        modelMap.addAttribute("p", p);
        modelMap.addAttribute("count", userService.findUserCountById(people.getUserId()));
        modelMap.addAttribute("people", people);
        return theme.getPcTemplate("/people/list_article");
    }

    @PostMapping(value = "/people/{shortUrl}/{publicFlag}/promotes")
    @ResponseBody
    public DataVo peoplePromote(@PathVariable(value = "shortUrl", required = false) String shortUrl, @PathVariable(value = "publicFlag", required = false) String publicFlag){
        if (StringUtils.isBlank(shortUrl)) {
            return null;
        }
        User people=userService.findUserByShorturl(shortUrl);
        if(people==null){
            return null;
        }
        Integer flag = Integer.valueOf(publicFlag);
        List<Article> AList = articleService.findArticleIndexByPer(people.getUserId(),flag);
        HashMap<String,JSONArray> RetTree = new HashMap<>();
        JSONArray Jarry = new JSONArray();
        String title1="",title2 = null;
        for (Article a:AList) {
            title1 = a.getTitle();
            JSONObject object = new JSONObject();
            object.put("article_id",a.getId());
            object.put("promoteid",a.getPromoteid());
            object.put("title",a.getTitle());
            object.put("promoteVersion",a.getPromoteVersion());
            if(title1.equals(title2) || title2 == null){
                Jarry.add(object);
                title2 = a.getTitle();
            }
            else{
                JSONArray Jarry1 = new JSONArray();
                for (int i = 0; i< Jarry.size(); i++) {
                    JSONObject value = Jarry.getJSONObject(i);
                    Jarry1.add(value);
                }
                RetTree.put(title2,Jarry1);
                title2 = a.getTitle();
                Jarry.clear();
                Jarry.add(object);
            }
        }
        RetTree.put(title1,Jarry);
        if(AList.size() == 0){
            return DataVo.success("查询成功,无记录",RetTree);
        }
        return DataVo.success("查询成功",RetTree);
    }


    @PostMapping(value = "/people/{shortUrl}/{publicFlag}/promotesList")
    @ResponseBody
    public DataVo peoplePromoteList(@PathVariable(value = "shortUrl", required = false) String shortUrl,@PathVariable(value = "publicFlag", required = false) String publicFlag){
        if (StringUtils.isBlank(shortUrl)) {
            return null;
        }
        User people=userService.findUserByShorturl(shortUrl);
        if(people==null){
            return null;
        }
        Integer flag = Integer.valueOf(publicFlag);
        List<Promote> AList = articleService.findPromoteIndexByPer(people.getUserId(),flag);
        HashMap<String,JSONArray> RetTree = new HashMap<>();
        JSONArray Jarry = new JSONArray();
        for (Promote a:AList) {
            JSONObject object = new JSONObject();
            object.put("promoteid",a.getId());
            object.put("description",a.getDescription());
            object.put("title",a.getTitle());
            object.put("picture",a.getPicture());
            object.put("publicflag",a.getPublicflag());
            object.put("create_time",a.getCreateTime());
            object.put("update_time",a.getUpdateTime());
            Jarry.add(object);
        }
        RetTree.put(people.getUserId().toString(),Jarry);
        if(AList.size() == 0){
            return DataVo.success("查询成功,无记录",RetTree);
        }
        return DataVo.success("查询成功",RetTree);
    }
    //用户问题列表页面
    @GetMapping(value = "/people/{shortUrl}/share")
    public String peopleShare(@RequestParam(value = "p", defaultValue = "1") int p, @PathVariable(value = "shortUrl", required = false) String shortUrl, ModelMap modelMap){
        if (StringUtils.isBlank(shortUrl)) {
            return theme.getPcTemplate("404");
        }
        User people=userService.findUserByShorturl(shortUrl);
        if(people==null){
            return theme.getPcTemplate("404");
        }
        if (getUser() != null) {
            modelMap.addAttribute("user", getUser());
        }
        modelMap.addAttribute("p", p);
        modelMap.addAttribute("count", userService.findUserCountById(people.getUserId()));
        modelMap.addAttribute("people", people);
        return theme.getPcTemplate("/people/list_share");
    }

    //用户关注列表页面
    @GetMapping(value = "/people/{shortUrl}/follow")
    public String peopleFollow(@RequestParam(value = "p", defaultValue = "1") int p, @PathVariable(value = "shortUrl", required = false) String shortUrl, ModelMap modelMap){
        if (StringUtils.isBlank(shortUrl)) {
            return theme.getPcTemplate("404");
        }
        User people=userService.findUserByShorturl(shortUrl);
        if(people==null){
            return theme.getPcTemplate("404");
        }
        if (getUser() != null) {
            modelMap.addAttribute("user", getUser());
        }
        modelMap.addAttribute("p", p);
        modelMap.addAttribute("count", userService.findUserCountById(people.getUserId()));
        modelMap.addAttribute("people", people);
        return theme.getPcTemplate("/people/list_follow");
    }
    //用户问题列表页面
    @GetMapping(value = "/people/{shortUrl}/fans")
    public String peopleFans(@RequestParam(value = "p", defaultValue = "1") int p, @PathVariable(value = "shortUrl", required = false) String shortUrl, ModelMap modelMap){
        if (StringUtils.isBlank(shortUrl)) {
            return theme.getPcTemplate("404");
        }
        User people=userService.findUserByShorturl(shortUrl);
        if(people==null){
            return theme.getPcTemplate("404");
        }
        if (getUser() != null) {
            modelMap.addAttribute("user", getUser());
        }
        modelMap.addAttribute("p", p);
        modelMap.addAttribute("count", userService.findUserCountById(people.getUserId()));
        modelMap.addAttribute("people", people);
        return theme.getPcTemplate("/people/list_fans");
    }
}
