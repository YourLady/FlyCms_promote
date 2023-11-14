package com.flycms.web.front;

import com.flycms.constant.Const;
import com.flycms.core.base.BaseController;
import com.flycms.core.entity.DataVo;
import com.flycms.core.entity.UserLoginVo;
import com.flycms.core.entity.UserVo;
import com.flycms.core.utils.*;
import com.flycms.module.question.service.ImagesService;
import com.flycms.module.user.model.User;
import com.flycms.module.user.service.UserService;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.ParseException;
import java.util.*;

@Controller
@Slf4j
public class UserController extends BaseController {
    @Autowired
    protected UserService userService;

    @Autowired
    private ImagesService imagesService;

    //测试公众号
    private static final String app_id = "wx1c1c8f133cf174f6";
    private static final String app_secret = "ceab468103a2696d99933dc6a21aad4a";

    private static final Gson gson = new Gson();

    //用户注册
    @GetMapping(value = "/reg")
    public String userReg(@RequestParam(value = "invite", required = false) String invite,ModelMap modelMap){
        if(invite==null){
            invite=CookieUtils.getCookie(request,"invite");
        }
        modelMap.addAttribute("invite",invite);
        return theme.getPcTemplate("user/reg");
    }

    /**
     * 用户提交手机号码申请获取验证码
     *test
     * @param username
     *        提交的手机号码
     * @param captcha
     *        验证码
     * @return
     * @throws Exception
     */
    @ResponseBody
    @PostMapping(value = "/ucenter/mobilecode")
    public DataVo getAddUserMobileCode(@RequestParam(value = "username", required = false) String username,@RequestParam(value = "captcha", required = false) String captcha) throws Exception {
        DataVo data = DataVo.failure("操作失败");
        String kaptcha = (String) session.getAttribute("kaptcha");
        // 校验验证码
//        if (captcha != null) {
//            if (!captcha.equalsIgnoreCase(kaptcha)) {
//                return DataVo.failure("验证码错误");
//            }
//            session.removeAttribute(Const.KAPTCHA_SESSION_KEY);
//        }else{
//            return DataVo.failure("验证码不能为空");
//        }
        if(!StringHelperUtils.checkPhoneNumber(username)) {
            return DataVo.failure("手机号码错误！");
        }
        data = userService.regMobileCode(username);
        return data;
    }


    /**
     * 添加新用户
     *
     * @param username
     * @param password
     * @return
     */
    @ResponseBody
    @PostMapping(value = "/ucenter/reg_user")
    public DataVo addUser(@RequestParam(value = "username", required = false) String username,
                          @RequestParam(value = "password", required = false) String password,
                          @RequestParam(value = "code", required = false) String code
    ) {
        DataVo data = DataVo.failure("操作失败");
        try {
            username=username.trim();
            password=password.trim();
            //password2=password2.trim();
            //captcha=captcha.trim();
            //String kaptcha = (String) session.getAttribute(Const.KAPTCHA_SESSION_KEY);
            // 校验验证码
//            if (captcha == null && "".equals(captcha)) {
//                return DataVo.failure("验证码不能为空");
//            }
//            captcha=captcha.toLowerCase();
//            if(!captcha.equals(kaptcha)){
//                return DataVo.failure("验证码错误");
//            }
            if (code == null) {
                return DataVo.failure("验证码不能为空");
            }
            if (StringUtils.isBlank(username)) {
                return DataVo.failure("用户名不能为空");
            }
            if (StringUtils.isBlank(password)) {
                return DataVo.failure("密码不能为空");
            }
            if (password.length() < 6) {
                return DataVo.failure("密码不能小于6位");
            }
            if (password.length() > 16) {
                return DataVo.failure("密码不能大于16位");
            }
//            if (!password.equals(password2)) {
//                return DataVo.failure("密码两次输入不一致");
//            }
            data = userService.addUserReg(1,username, password,code,null,request,response);
            Thread.sleep(3000L);
            //return DataVo.success("用户注册成功");
        } catch (Exception e) {
            data = DataVo.failure(e.getMessage());
        }
        return data;
    }

    @ResponseBody
    @PostMapping(value = "/ucenter/addMobileUser")
    public DataVo addMobileUser(@RequestParam(value = "phoneNumber", required = false) String phoneNumber,
                                @RequestParam(value = "mobilecode", required = false) String mobilecode,
                                @RequestParam(value = "password", required = false) String password,
                                @RequestParam(value = "password2", required = false) String password2,
                                @RequestParam(value = "invite", required = false) String invite) throws Exception{
        DataVo data = DataVo.failure("操作失败");
        if (mobilecode == null) {
            return data = DataVo.failure("验证码不能为空");
        }
        if (password == null) {
            return data = DataVo.failure("密码不能为空");
        }
        if(!password.equals(password2)){
            return data = DataVo.failure("两次密码输入不一样");
        }
        return data = userService.addUserReg(1,phoneNumber, password,mobilecode,invite,request,response);
    }

    //用户登录页面
    @GetMapping(value = "/login")
    public String userLogin(@RequestParam(value = "redirectUrl",required = false) String redirectUrl,ModelMap modelMap){
        if(getUser() != null && getUser().getUserId() !=null){
            return "redirect:/index";
        }
        modelMap.addAttribute("redirectUrl",redirectUrl);
        return theme.getPcTemplate("user/login");
    }

    //登录处理
    @ResponseBody
    @PostMapping(value = "/ucenter/login_act")
    public DataVo userLogin(
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "password", required = false) String password,
            @RequestParam(value = "rememberMe", required = false) String rememberMe,
            @RequestParam(value = "redirectUrl",required = false) String redirectUrl,
            @RequestParam(value = "captcha", required = false) String captcha) {
        try {
            //String kaptcha = (String) session.getAttribute("kaptcha");
            if (StringUtils.isBlank(username)) {
                return DataVo.failure("用户名不能为空");
            }
            if (StringUtils.isBlank(password)) {
                return DataVo.failure("密码不能为空");
            } else if (password.length() < 6 && password.length() > 30) {
                return DataVo.failure("密码最少6个字符，最多30个字符");
            }
            // 校验验证码
//            if (captcha != null) {
//                if (!captcha.equalsIgnoreCase(kaptcha)) {
//                    return DataVo.failure("验证码错误");
//                }
//            }else{
//                return DataVo.failure("验证码不能为空");
//            }
            boolean keepLogin = "1".equals(rememberMe) ? true : false;
            User entity = userService.userLogin(username,password,keepLogin,request,response);
            UserLoginVo retEntiry = new UserLoginVo();
            if(entity==null){
                return DataVo.failure("帐号或密码错误。");
            }else{
                //DataVo.success(entity);
                BeanUtils.copyProperties(entity,retEntiry);
                retEntiry.setUserId(entity.getUserId().toString());
                session.removeAttribute(Const.KAPTCHA_SESSION_KEY);
                if (StringUtils.isNotEmpty(redirectUrl)){
                    return DataVo.jump("操作成功", redirectUrl,retEntiry);
                }
                return DataVo.jump("操作成功", "/",retEntiry);
            }
        } catch (Exception e) {
            return DataVo.failure("帐号或密码错误。");
        }
    }

    /**
     * 页面ajax登录处理
     *
     * @param username
     * @param password
     * @return
     */
    @ResponseBody
    @PostMapping(value = "/ucenter/ajaxlogin")
    public DataVo userAjaxLogin( @RequestParam(value = "username", required = false) String username,
                                 @RequestParam(value = "password", required = false) String password,
                                 @RequestParam(value = "rememberMe", required = false) String rememberMe,
                                 @RequestParam(value = "code", required = false) String code) {
        DataVo data = DataVo.failure("操作失败");
        try {
            String kaptcha = (String) session.getAttribute("kaptcha");
            if (StringUtils.isBlank(username)) {
                return DataVo.failure("用户名不能为空");
            }
            if (StringUtils.isBlank(password)) {
                return DataVo.failure("密码不能为空");
            } else if (password.length() < 6 && password.length() > 30) {
                return DataVo.failure("密码最少6个字符，最多30个字符");
            }
            // 校验验证码
            if (code != null) {
                if (!code.equalsIgnoreCase(kaptcha)) {
                    return DataVo.failure("验证码错误");
                }
            }else{
                return DataVo.failure("验证码不能为空");
            }
            boolean keepLogin = "1".equals(rememberMe) ? true : false;
            User entity = userService.userLogin(username,password,keepLogin,request,response);
            if(entity==null){
                return DataVo.failure("帐号或密码错误。");
            }else{
                session.removeAttribute(Const.KAPTCHA_SESSION_KEY);
                return DataVo.jump("操作成功", "/");
            }
        } catch (Exception e) {
            data = DataVo.failure(e.getMessage());
        }
        return data;
    }

    //修改用户基本信息
    @GetMapping(value = "/ucenter/account")
    public String userAccount(ModelMap modelMap){
        modelMap.addAttribute("user", getUser());
        return theme.getPcTemplate("user/account");
    }

    //更新用户基本信息
    @PostMapping("/ucenter/account_update")
    @ResponseBody
    public DataVo updateUserAccount(@Valid User user, BindingResult result){
        DataVo data = DataVo.failure("操作失败");
        try {
            if (result.hasErrors()) {
                List<ObjectError> list = result.getAllErrors();
                for (ObjectError error : list) {
                    return DataVo.failure(error.getDefaultMessage());
                }
                return null;
            }
            if(!StringUtils.isNumeric(user.getUserId().toString())){
                return data=DataVo.failure("请勿非法传递数据！");
            }
            if(!user.getUserId().equals(getUser().getUserId())){
                return data=DataVo.failure("请勿非法传递数据！");
            }
            if(!getUser().getUserId().equals(user.getUserId())){
                return data=DataVo.failure("只能修改属于自己的基本信息！");
            }
            if(StringUtils.isBlank(user.getNickName())){
                return data=DataVo.failure("昵称不能为空！");
            }
            if(user.getBirthday()==null || "".equals(user.getBirthday())){
                return data=DataVo.failure("请选择您的生日日期！");
            }
            if(DateUtils.isValidDate(user.getBirthday().toString())){
                return data=DataVo.failure("生日日期格式错误！");
            }
            if(user.getProvince()==0){
                return data=DataVo.failure("省份未选择！");
            }
            if(user.getCity()==0){
                return data=DataVo.failure("地区为选择！");
            }

            data = userService.updateUserAccount(user);
        } catch (Exception e) {
            data = DataVo.failure(e.getMessage());
        }
        return data;
    }

    //安全手机账号设置
    @GetMapping(value = "/ucenter/safe_mobile")
    public String safeMobile(ModelMap modelMap){
        modelMap.addAttribute("user", getUser());
        return theme.getPcTemplate("user/safe_mobile");
    }

    /**
     * 用户提交手机号码申请获取验证码
     *
     * @param mobile
     *        提交的手机号码
     * @return
     * @throws Exception
     */
    @ResponseBody
    @PostMapping(value = "/ucenter/safemobilecode")
    public DataVo safeMobileCode(@RequestParam(value = "mobile", required = false) String mobile, @RequestParam(value = "captcha", required = false) String captcha) throws Exception {
        DataVo data = DataVo.failure("操作失败");
        String kaptcha = (String) session.getAttribute("kaptcha");
        // 校验验证码
//        if (captcha != null) {
//            if (!captcha.equalsIgnoreCase(kaptcha)) {
//                return DataVo.failure("验证码错误");
//            }
//            session.removeAttribute(Const.KAPTCHA_SESSION_KEY);
//        }else{
//            return DataVo.failure("验证码不能为空");
//        }
        if(!StringHelperUtils.checkPhoneNumber(mobile)) {
            return DataVo.failure("手机号码错误！");
        }
        //data = userService.safeMobileCode(getUser().getUserId(),mobile);
        data = userService.safeMobileCode(Long.valueOf("920137270298947584"),mobile);
        return data;
    }

    @ResponseBody
    @PostMapping(value = "/user/safemobilecode")
    public DataVo usersafeMobileCode(@RequestParam(value = "mobile", required = false) String mobile, @RequestParam(value = "userid", required = false) String userid) throws Exception {
        DataVo data = DataVo.failure("操作失败");
        String kaptcha = (String) session.getAttribute("kaptcha");
        // 校验验证码
//        if (captcha != null) {
//            if (!captcha.equalsIgnoreCase(kaptcha)) {
//                return DataVo.failure("验证码错误");
//            }
//            session.removeAttribute(Const.KAPTCHA_SESSION_KEY);
//        }else{
//            return DataVo.failure("验证码不能为空");
//        }
        if(!StringHelperUtils.checkPhoneNumber(mobile)) {
            return DataVo.failure("手机号码错误！");
        }
        //data = userService.safeMobileCode(getUser().getUserId(),mobile);
        data = userService.safeMobileCode(Long.valueOf(userid),mobile);
        return data;
    }

    @ResponseBody
    @PostMapping(value = "/user/safe_mobile_update")
    public DataVo usersafeMobile(
            @RequestParam(value = "password", required = false) String password,
            @RequestParam(value = "mobile", required = false) String mobile,
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "userid", required = false) String userid){
        DataVo data = DataVo.failure("操作失败");
        try {
            if (StringUtils.isBlank(password)) {
                return DataVo.failure("密码不能为空");
            } else if (password.length() < 6 && password.length() >= 32) {
                return DataVo.failure("密码最少6个字符，最多32个字符");
            }
            if(!StringHelperUtils.checkPhoneNumber(mobile)) {
                return DataVo.failure("手机号码错误！");
            }
            if (code == null && "".equals(code)) {
                return DataVo.failure("验证码不能为空");
            }
            //data=userService.updateSafeMobile(getUser().getUserId(),password, mobile, code);
            data=userService.updateSafeMobile(Long.valueOf(userid),password, mobile, code);
        } catch (Exception e) {
            data = DataVo.failure(e.getMessage());
        }
        return data;
    }


    @ResponseBody
    @PostMapping(value = "/ucenter/safe_mobile_update")
    public DataVo safeMobile(
            @RequestParam(value = "password", required = false) String password,
            @RequestParam(value = "mobile", required = false) String mobile,
            @RequestParam(value = "code", required = false) String code) {
        DataVo data = DataVo.failure("操作失败");
        try {
            if (StringUtils.isBlank(password)) {
                return DataVo.failure("密码不能为空");
            } else if (password.length() < 6 && password.length() >= 32) {
                return DataVo.failure("密码最少6个字符，最多32个字符");
            }
            if(!StringHelperUtils.checkPhoneNumber(mobile)) {
                return DataVo.failure("手机号码错误！");
            }
            if (code == null && "".equals(code)) {
                return DataVo.failure("验证码不能为空");
            }
            //data=userService.updateSafeMobile(getUser().getUserId(),password, mobile, code);
            data=userService.updateSafeMobile(Long.valueOf("920137270298947584"),password, mobile, code);
        } catch (Exception e) {
            data = DataVo.failure(e.getMessage());
        }
        return data;
    }

    //设置安全邮箱账号
    @GetMapping(value = "/ucenter/safe_email")
    public String safeEmail(ModelMap modelMap){
        modelMap.addAttribute("user", getUser());
        return theme.getPcTemplate("user/safe_email");
    }

    @ResponseBody
    @PostMapping(value = "/ucenter/safe_email_code")
    public DataVo userAjaxMailCaptcha(@RequestParam(value = "userEmail", required = false) String userEmail) {
        DataVo data = DataVo.failure("操作失败");
        try {
            if (!StringHelperUtils.emailFormat(userEmail)) {
                return DataVo.failure("邮箱格式错误！");
            }
            return userService.safeEmailVerify(userEmail,getUser().getUserId());
        } catch (Exception e) {
            data = DataVo.failure(e.getMessage());
        }
        return data;
    }

    @ResponseBody
    @PostMapping(value = "/ucenter/safe_email_update")
    public DataVo safeEmail(
            @RequestParam(value = "password", required = false) String password,
            @RequestParam(value = "userEmail", required = false) String userEmail,
            @RequestParam(value = "code", required = false) String code) {
        DataVo data = DataVo.failure("操作失败");
        try {
            // 校验验证码
            if (code == null && "".equals(code)) {
                return DataVo.failure("验证码不能为空");
            }
            if (StringUtils.isBlank(password)) {
                return DataVo.failure("新密码不能为空");
            } else if (password.length() < 6 && password.length() >= 32) {
                return DataVo.failure("密码最少6个字符，最多32个字符");
            }
            if(!StringHelperUtils.emailFormat(userEmail)) {
                return DataVo.failure("邮箱地址错误！");
            }
            data=userService.updateSafeEmail(getUser().getUserId(), password, userEmail,code);
        } catch (Exception e) {
            data = DataVo.failure(e.getMessage());
        }
        return data;
    }

    //我的积分
    @GetMapping(value = "/ucenter/integral")
    public String userIntegral(ModelMap modelMap){
        modelMap.addAttribute("user", getUser());
        return theme.getPcTemplate("user/integral");
    }

    //我的退款申请
    @GetMapping(value = "/ucenter/refunds")
    public String userRefunds(ModelMap modelMap){
        modelMap.addAttribute("user", getUser());
        return theme.getPcTemplate("user/refunds");
    }

    //我的网站建议
    @GetMapping(value = "/ucenter/complain")
    public String userComplain(ModelMap modelMap){
        modelMap.addAttribute("user", getUser());
        return theme.getPcTemplate("user/complain");
    }

    //我的产品收藏
    @GetMapping(value = "/ucenter/favorite")
    public String userFavorite(ModelMap modelMap){
        modelMap.addAttribute("user", getUser());
        return theme.getPcTemplate("user/favorite");
    }

    //线上推广列表
    @GetMapping(value = "/ucenter/invite")
    public String userInvite(@RequestParam(value = "p", defaultValue = "1") int p,ModelMap modelMap){
        modelMap.addAttribute("user", getUser());
        modelMap.addAttribute("p", p);
        return theme.getPcTemplate("user/invite");
    }

    //我的账户余额
    @GetMapping(value = "/ucenter/account_log")
    public String userAccount_log(@RequestParam(value = "p", defaultValue = "1") int p,ModelMap modelMap){
        modelMap.addAttribute("p", p);
        modelMap.addAttribute("user", getUser());
        return theme.getPcTemplate("user/account_log");
    }

    //我的账户余额体现申请
    @GetMapping(value = "/ucenter/withdraw")
    public String userWithdraw(ModelMap modelMap){
        modelMap.addAttribute("user", getUser());
        return theme.getPcTemplate("user/withdraw");
    }

    //我的在线充值
    @GetMapping(value = "/ucenter/online_recharge")
    public String userOnlineRecharge(ModelMap modelMap){
        modelMap.addAttribute("user", getUser());
        return theme.getPcTemplate("user/online_recharge");
    }

    //我的收货地址管理
    @GetMapping(value = "/ucenter/address")
    public String userAddress(ModelMap modelMap){
        modelMap.addAttribute("user", getUser());
        return theme.getPcTemplate("user/address");
    }

    //我的个人信息
    @GetMapping(value = "/ucenter/info")
    public String userInfo(ModelMap modelMap){
        modelMap.addAttribute("user", getUser());
        return theme.getPcTemplate("user/info");
    }

    //我的密码修改
    @GetMapping(value = "/ucenter/password")
    public String userPassword(ModelMap modelMap){
        modelMap.addAttribute("user", getUser());
        return theme.getPcTemplate("user/password");
    }

    @ResponseBody
    @PostMapping(value = "/ucenter/password_update")
    public DataVo updatePassword(
            @RequestParam(value = "old_password", required = false) String old_password,
            @RequestParam(value = "password", required = false) String password,
            @RequestParam(value = "password_confirmation", required = false) String password_confirmation,
            @RequestParam(value = "captcha", required = false) String captcha) {
        String kaptcha = (String) session.getAttribute("kaptcha");
        DataVo data = DataVo.failure("操作失败");
        // 校验验证码
        if (captcha == null && "".equals(captcha)) {
            return DataVo.failure("验证码不能为空");
        }
        captcha=captcha.toLowerCase();
        if(!captcha.equals(kaptcha)){
            return DataVo.failure("验证码错误");
        }
        try {
            if (StringUtils.isBlank(old_password)) {
                return DataVo.failure("原来密码不能为空");
            } else if (old_password.length() < 6 && old_password.length() >= 32) {
                return DataVo.failure("密码最少6个字符，最多32个字符");
            }
            if (StringUtils.isBlank(password)) {
                return DataVo.failure("新密码不能为空");
            } else if (password.length() < 6 && password.length() >= 32) {
                return DataVo.failure("密码最少6个字符，最多32个字符");
            }
            if (!password.equals(password_confirmation)) {
                return DataVo.failure("两次密码必须一样");
            }
            data=userService.updatePassword(getUser().getUserId(), old_password, password);
            session.removeAttribute(Const.KAPTCHA_SESSION_KEY);
        } catch (Exception e) {
            data = DataVo.failure(e.getMessage());
        }
        return data;
    }

    /**
     * 保存头像
     *
     * @param avatar
     * @return
     * @throws IOException
     * @throws ParseException
     */
    @ResponseBody
    @PostMapping("/ucenter/avatar.json")
    public DataVo changeAvatar(String avatar) throws IOException, ParseException {
        DataVo data = DataVo.failure("操作失败");
        if (StringUtils.isEmpty(avatar)) {
            return DataVo.failure("头像不能为空");
        }

        byte[] bytes;
        try {
            String _avatar = avatar.substring(avatar.indexOf(",") + 1, avatar.length());
            bytes = Base64HelperUtils.decode(_avatar);
        } catch (Exception e) {
            e.printStackTrace();
            return DataVo.failure("头像格式不正确");
        }
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        BufferedImage bufferedImage = ImageIO.read(bais);
        data =imagesService.saveAvatarDataFile(getUser(), bufferedImage);
        bais.close();
        return data;
    }

    @ResponseBody
    @PostMapping("/user/setAvatar")
    public DataVo setAvatar(@RequestParam(value = "avatar", required = true)MultipartFile avatar, @RequestParam(value = "userid", required = true)String userid) throws IOException, ParseException {
        DataVo data = DataVo.failure("操作失败");
        if (!avatar.isEmpty() && StringUtils.isNotEmpty(userid)) {
            try {
                byte[] bytes = avatar.getBytes();
                ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                BufferedImage bufferedImage = ImageIO.read(bais);
                data =imagesService.saveAvatarDataFile(getUser(Long.valueOf(userid)), bufferedImage);
                bais.close();
            } catch (Exception e) {

            }
        }else {
            return DataVo.failure("头像不能为空");
        }
        return data;
    }

    @ResponseBody
    @PostMapping("/promote/setAvatar")
    public DataVo setPromoteAvatar(@RequestParam(value = "avatar", required = true)MultipartFile avatar) throws IOException, ParseException {
        DataVo data = DataVo.failure("操作失败");
        if (!avatar.isEmpty()) {
            try {
                byte[] bytes = avatar.getBytes();
                ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                BufferedImage bufferedImage = ImageIO.read(bais);
                SnowFlake snowFlake = new SnowFlake(2, 3);
                data =imagesService.saveAvatarPromoteFile(String.valueOf(snowFlake.nextId()), bufferedImage);
                bais.close();
            } catch (Exception e) {

            }
        }else {
            return DataVo.failure("头像不能为空");
        }
        return data;
    }

    //处理关注用户信息
    @ResponseBody
    @PostMapping(value = "/ucenter/user/follow")
    public DataVo userFollow(@RequestParam(value = "id", required = false) String id) {
        DataVo data = DataVo.failure("操作失败");
        try {
            if (!NumberUtils.isNumber(id)) {
                return data=DataVo.failure("问题参数错误");
            }
            if(getUser()==null){
                return data=DataVo.failure("请登陆后关注");
            }
            if((getUser().getUserId().equals(Long.parseLong(id)))){
                return data=DataVo.failure("无法关注自己！");
            }
            data=userService.addUserFans(Long.parseLong(id),getUser().getUserId());
        } catch (Exception e) {
            data = DataVo.failure(e.getMessage());
        }
        return data;
    }

    /*
     *
     * 前台JS读取用户登录状态判断
     *
     */
    @ResponseBody
    @GetMapping(value = "/user/status.json")
    public void userSession() throws Exception {
        PrintWriter out =null;
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-type", "text/html;charset=utf-8");
        response.setContentType("text/javascript;charset=utf-8");
        /*response.setHeader("Cache-Control", "no-cache");*/
        out = response.getWriter();
        //out.flush();//清空缓存
        if(getUser()!=null){
            out.println("var userid='"+getUser().getUserId()+"';");
            out.println("var nickname = '"+getUser().getNickName()+"';");
            String signature="";
            if(getUser().getSignature()!=null){
                signature=getUser().getSignature();
            }else{
                signature="这个家伙很懒，啥也没留下！";
            }
            out.println("var signature = '"+signature+"';");
            String avatar=getUser().getAvatar();
            if(avatar==null){
                avatar="/assets/skin/pc_theme/defalut/images/avatar/default.jpg";
            }
            out.println("var smallAvatar = '"+avatar+"';");
        }else{
            out.println("var userid='';");
            out.println("var nickname = '';");
            out.println("var signature = '';");
            out.println("var smallAvatar = '/assets/skin/pc_theme/defalut/images/avatar/default.jpg';");
        }
        out.close();
    }

    /**
     * 用户退出登录
     *
     */
    @GetMapping(value = "/logout")
    public String logout() {
        //清除cookie、session
        userService.signOutLogin(request,response);
        return "redirect:/index-hot";
    }

    /**
     * 用户选择找回密码方式
     *
     */
    @GetMapping(value = "/forget_password")
    public String forgetPassword(ModelMap modelMap) {
        if(getUser()!=null){
            modelMap.addAttribute("user", getUser());
        }
        return theme.getPcTemplate("user/forget_password");
    }

    /**
     * 用户选择找回密码方式
     *
     */
    @GetMapping(value = "/forget_password/mobile")
    public String forgetPasswordMobile(ModelMap modelMap) {
        if(getUser()!=null){
            modelMap.addAttribute("user", getUser());
        }
        return theme.getPcTemplate("user/forget_password_mobile");
    }

    /**
     * 用户提交手机号码申请获取验证码
     *
     * @param username
     *        提交的手机号码
     * @param captcha
     *        验证码
     * @return
     * @throws Exception
     */
    @ResponseBody
    @PostMapping(value = "/forget_password/getbackcode")
    public DataVo getBackCode(@RequestParam(value = "username", required = false) String username,@RequestParam(value = "captcha", required = false) String captcha) throws Exception {
        DataVo data = DataVo.failure("操作失败");
        String kaptcha = (String) session.getAttribute("kaptcha");
        // 校验验证码
        if (captcha != null) {
            if (!captcha.equalsIgnoreCase(kaptcha)) {
                return DataVo.failure("验证码错误");
            }
            session.removeAttribute(Const.KAPTCHA_SESSION_KEY);
        }else{
            return DataVo.failure("验证码不能为空");
        }
        if (StringUtils.isBlank(username)) {
            return DataVo.failure("手机号码不能为空");
        }
        if(!StringHelperUtils.checkPhoneNumber(username)) {
            return DataVo.failure("手机号码错误！");
        }
        data = userService.userGetBackCode(username);
        return data;
    }

    /**
     * 用户选择找回密码方式
     *
     */
    @GetMapping(value = "/forget_password/email")
    public String forgetPasswordEmail(ModelMap modelMap) {
        if(getUser()!=null){
            modelMap.addAttribute("user", getUser());
        }
        return theme.getPcTemplate("user/forget_password_email");
    }

    /**
     * 用户提交邮箱地址申请获取验证码
     *
     * @param username
     *        提交的手机号码
     * @return
     * @throws Exception
     */
    @ResponseBody
    @PostMapping(value = "/forget_password/getbackemailcode")
    public DataVo getEmailBackCode(@RequestParam(value = "username", required = false) String username) throws Exception {
        DataVo data = DataVo.failure("操作失败");
        if (StringUtils.isBlank(username)) {
            return DataVo.failure("手机号码不能为空");
        }
        if (!StringHelperUtils.emailFormat(username)) {
            return DataVo.failure("邮箱格式错误！");
        }
        return data = userService.getEmailBackCode(username);
    }

    /**
     * 用户提交手机号码申请获取验证码
     *
     * @param username
     *        用户邮箱或者手机号码
     * @param code
     *        验证码
     * @param password
     *        重新设置的新密码
     * @return
     * @throws Exception
     */
    @ResponseBody
    @PostMapping(value = "/forget_password/update_password")
    public DataVo updateUserPassword(@RequestParam(value = "username", required = false) String username,
                                     @RequestParam(value = "code", required = false) String code,
                                     @RequestParam(value = "password", required = false) String password) throws Exception {
        DataVo data = DataVo.failure("操作失败");
        String kaptcha = (String) session.getAttribute("kaptcha");
        if (StringUtils.isBlank(username)) {
            return DataVo.failure("用户名不能为空");
        }
        if (StringUtils.isBlank(code)){
            return DataVo.failure("验证码不能为空");
        }
        if (StringUtils.isBlank(password)) {
            return DataVo.failure("密码不能为空");
        } else if (password.length() < 6 && password.length() > 30) {
            return DataVo.failure("密码最少6个字符，最多30个字符");
        }
        data = userService.updateGetBackPassword(username,code,password);
        return data;
    }

    /**
     * 关注用户
     * @param userId 自己的用户id
     * @param followUserId 关注人的用户ID
     * @return
     */
    @ResponseBody
    @PostMapping(value = "/save/follow")
    public DataVo saveFollow(@RequestParam(value = "userId") String userId,
                             @RequestParam(value = "followUserId") String followUserId){
        try {
            userService.saveFollow(userId,followUserId);
        } catch (Exception e) {
            log.error("关注失败，原因:{}",e);
            return DataVo.failure("关注失败！");
        }
        return DataVo.success("关注成功！");
    }

    /**
     * 取消关注
     * @param userId 自己的用户id
     * @param followUserId 关注人的用户ID
     * @return
     */
    @ResponseBody
    @PostMapping(value = "/cancel/follow")
    public DataVo cancelFollow(@RequestParam(value = "userId") String userId,
                             @RequestParam(value = "followUserId") String followUserId){
        try {
            userService.cancelFollow(userId,followUserId);
        } catch (Exception e) {
            log.error("取消关注失败，原因:{}",e);
            return DataVo.failure("取消关注失败！");
        }
        return DataVo.success("取消关注成功！");
    }

    //查询关注人列表
    @GetMapping("/query/followUsers")
    @ResponseBody
    public DataVo queryFollowUsers(@RequestParam(value = "userId", required = false) String userId){
        List<UserVo> result = new ArrayList<>();
        try {
            result = userService.queryFollowUsers(userId);
        } catch (Exception e) {
            log.error("查询关注人列表失败，原因:{}",e);
            return DataVo.failure("查询关注人列表失败！");
        }
        return DataVo.success(result);
    }

    // 生成带参数的二维码，扫描关注微信公众号，自动登录网站
    @ResponseBody
    @GetMapping("/wechat/getQrCode")
    public DataVo wechatMpLogin() throws Exception {
        HashMap modelMap = new HashMap<>();
        String access_token = getAccessToken();
        String url = "https://api.weixin.qq.com/cgi-bin/qrcode/create?access_token=" + access_token;
        String scene_str = "lrfun.com." + new Date().getTime();
        String params = "{\"expire_seconds\":600, \"action_name\":\"QR_STR_SCENE\", \"action_info\":{\"scene\":{\"scene_str\":\"" + scene_str + "\"}}}";
        Map<String, Object> resultMap = httpClientPost(url, params);
        if (resultMap.get("ticket") != null) {
            String qrcodeUrl = "https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket=" + resultMap.get("ticket");
            modelMap.put("qrcodeUrl", qrcodeUrl);
        }
        modelMap.put("scene_str", scene_str);
        return DataVo.success(modelMap);
    }

    // 获取access_tocken
    private String getAccessToken() throws Exception{
        String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=" + app_id + "&secret=" + app_secret;
        Map<String, Object> accessTokenMap = httpClientGet(url);
        System.out.println(accessTokenMap);
        return accessTokenMap.get("access_token").toString();
    }

    /***
     * httpClient-Get请求
     * @param url 请求地址
     * @return
     * @throws Exception
     */
    public static Map<String, Object> httpClientGet(String url) throws Exception {
        HttpClient client = new HttpClient();
        client.getParams().setContentCharset("UTF-8");
        GetMethod httpGet = new GetMethod(url);
        try {
            client.executeMethod(httpGet);
            String response = httpGet.getResponseBodyAsString();
            Map<String, Object> map = gson.fromJson(response, Map.class);
            return map;
        } catch (Exception e) {
            throw e;
        } finally {
            httpGet.releaseConnection();
        }
    }

    /***
     * httpClient-Post请求
     * @param url 请求地址
     * @param params post参数
     * @return
     * @throws Exception
     */
    public static Map<String, Object> httpClientPost(String url, String params) throws Exception {
        HttpClient client = new HttpClient();
        client.getParams().setContentCharset("UTF-8");
        PostMethod httpPost = new PostMethod(url);
        try {
            RequestEntity requestEntity = new ByteArrayRequestEntity(params.getBytes("utf-8"));
            httpPost.setRequestEntity(requestEntity);
            client.executeMethod(httpPost);
            String response = httpPost.getResponseBodyAsString();
            Map<String, Object> map = gson.fromJson(response, Map.class);
            return map;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            httpPost.releaseConnection();
        }
    }


    // 回调函数
    @ResponseBody
    @PostMapping(value = "/wechat/callback")
    public void callback(HttpServletRequest httpServletRequest) throws Exception {
        Map<String, String> callbackMap = xmlToMap(httpServletRequest); //获取回调信息
        //下面是返回的xml
        //<xml><ToUserName><![CDATA[gh_f6b4da984c87]]></ToUserName> //微信公众号的微信号
        //<FromUserName><![CDATA[oJxRO1Y2NgWJ9gMDyE3LwAYUNdAs]]></FromUserName> //openid用于获取用户信息，做登录使用
        //<CreateTime>1531130986</CreateTime> //回调时间
        //<MsgType><![CDATA[event]]></MsgType>
        //<Event><![CDATA[SCAN]]></Event>
        //<EventKey><![CDATA[lrfun.com.UxJkWC1531967386903]]></EventKey> //上面自定义的参数（scene_str）
        //<Ticket><![CDATA[gQF57zwAAAAAAAAAAS5odHRwOi8vd2VpeGluLnFxLmNvbS9xLzAyY2ljbjB3RGtkZWwxbExLY3hyMVMAAgTvM0NbAwSAOgkA]]></Ticket> //换取二维码的ticket
        //</xml>
        if (callbackMap != null && callbackMap.get("FromUserName").toString() != null) {
            // 通过openid获取用户信息
            //Map<String, Object> wechatUserInfoMap = getUserInfoByOpenid(callbackMap.get("FromUserName"));
            // 将数据写入到数据库中，前面自定义的参数（scene_str）也需记录到数据库中，后面用于检测匹配登录
            // INSERT INTO wechat_user_info......(数据库操作)
        }
    }

    // xml转为map
    private Map<String, String> xmlToMap(HttpServletRequest httpServletRequest) {
        Map<String, String> map = new HashMap<String, String>();
        try {
            InputStream inputStream = httpServletRequest.getInputStream();
            SAXReader reader = new SAXReader(); // 读取输入流
            org.dom4j.Document document = reader.read(inputStream);
            Element root = document.getRootElement(); // 得到xml根元素
            List<Element> elementList = root.elements(); // 得到根元素的所有子节点
            // 遍历所有子节点
            for (Element e : elementList)
                map.put(e.getName(), e.getText());
            // 释放资源
            inputStream.close();
            inputStream = null;
            return map;
        } catch (Exception e) {
            e.getMessage();
        }
        return null;
    }

    // 检测登录
    @ResponseBody
    @GetMapping("/wechat/checkLogin")
    public  Map<String, Object> wechatMpCheckLogin(){
        // 根据scene_str查询数据库，获取对应记录
        // SELECT * FROM wechat_user_info WHERE event_key='scene_str';
        Map<String, Object> returnMap = new HashMap<String, Object>();
        if (true) {
            returnMap.put("result", "true");
        } else {
            returnMap.put("result", "false");
        }
        return returnMap;
    }
}
