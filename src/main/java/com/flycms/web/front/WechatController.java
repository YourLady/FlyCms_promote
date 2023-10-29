//package com.flycms.web.front;
//import com.google.gson.Gson;
//import org.apache.commons.httpclient.HttpClient;
//import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
//import org.apache.commons.httpclient.methods.GetMethod;
//import org.apache.commons.httpclient.methods.PostMethod;
//import org.apache.commons.httpclient.methods.RequestEntity;
//import org.dom4j.Element;
//import org.dom4j.io.SAXReader;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.ModelMap;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.ResponseBody;
//import org.springframework.web.servlet.ModelAndView;
//
//import javax.servlet.http.HttpServletRequest;
//import java.io.InputStream;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//@Controller
//public class WechatController {
//
//    //测试公众号
//    private static final String app_id = "wx825e3c2ed057b3ff";
//    private static final String app_secret = "2f7a783c502a40454ce4d321b509a562";
//
//    private static final Gson gson = new Gson();
//    /***
//     * httpClient-Get请求
//     * @param url 请求地址
//     * @return
//     * @throws Exception
//     */
//    public static Map<String, Object> httpClientGet(String url) throws Exception {
//        HttpClient client = new HttpClient();
//        client.getParams().setContentCharset("UTF-8");
//        GetMethod httpGet = new GetMethod(url);
//        try {
//            client.executeMethod(httpGet);
//            String response = httpGet.getResponseBodyAsString();
//            Map<String, Object> map = gson.fromJson(response, Map.class);
//            return map;
//        } catch (Exception e) {
//            throw e;
//        } finally {
//            httpGet.releaseConnection();
//        }
//    }
//
//    /***
//     * httpClient-Post请求
//     * @param url 请求地址
//     * @param params post参数
//     * @return
//     * @throws Exception
//     */
//    public static Map<String, Object> httpClientPost(String url, String params) throws Exception {
//        HttpClient client = new HttpClient();
//        client.getParams().setContentCharset("UTF-8");
//        PostMethod httpPost = new PostMethod(url);
//        try {
//            RequestEntity requestEntity = new ByteArrayRequestEntity(params.getBytes("utf-8"));
//            httpPost.setRequestEntity(requestEntity);
//            client.executeMethod(httpPost);
//            String response = httpPost.getResponseBodyAsString();
//            Map<String, Object> map = gson.fromJson(response, Map.class);
//            return map;
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        } finally {
//            httpPost.releaseConnection();
//        }
//    }
//
//    // 获取access_tocken
//    private String getAccessToken() throws Exception{
//        String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=" + app_id + "&secret=" + app_secret;
//        Map<String, Object> accessTokenMap = WechatController.httpClientGet(url);
//        System.out.println(accessTokenMap);
//        return accessTokenMap.get("access_token").toString();
//    }
//
//    // 通过openid获取用户信息
//    private Map<String, Object> getUserInfoByOpenid(String openid) throws Exception {
//        String access_tocken = getAccessToken();
//        String url = "https://api.weixin.qq.com/cgi-bin/user/info?access_token=" + access_tocken + "&openid=" + openid;
//        Map<String, Object> map = httpClientGet(url);
//        return map;
//    }
//
////    // 生成带参数的二维码，扫描关注微信公众号，自动登录网站
////    @ResponseBody
////    @PostMapping(value = "/wechat/getQrCode")
////    public ModelAndView wechatMpLogin(ModelMap modelMap) throws Exception {
////        String access_token = getAccessToken();
////        String url = "https://api.weixin.qq.com/cgi-bin/qrcode/create?access_token=" + access_token;
////        String scene_str = "lrfun.com." + new Date().getTime();
////        String params = "{\"expire_seconds\":600, \"action_name\":\"QR_STR_SCENE\", \"action_info\":{\"scene\":{\"scene_str\":\"" + scene_str + "\"}}}";
////        Map<String, Object> resultMap = httpClientPost(url, params);
////        if (resultMap.get("ticket") != null) {
////            String qrcodeUrl = "https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket=" + resultMap.get("ticket");
////            modelMap.put("qrcodeUrl", qrcodeUrl);
////        }
////        modelMap.put("scene_str", scene_str);
////        return new ModelAndView("/test/wechatMpLogin.vm", modelMap);
////    }
//
//    // 检测登录
//    @RequestMapping("/wechat/checkLogin.html")
//    public @ResponseBody Map<String, Object> wechatMpCheckLogin(String scene_str){
//        // 根据scene_str查询数据库，获取对应记录
//        // SELECT * FROM wechat_user_info WHERE event_key='scene_str';
//        Map<String, Object> returnMap = new HashMap<String, Object>();
//        if (true) {
//            returnMap.put("result", "true");
//        } else {
//            returnMap.put("result", "false");
//        }
//        return returnMap;
//    }
//
//    // 回调函数
//    @RequestMapping(value = "/wechat/callback")
//    public void callback(HttpServletRequest httpServletRequest) throws Exception {
//        Map<String, String> callbackMap = xmlToMap(httpServletRequest); //获取回调信息
//        //下面是返回的xml
//        //<xml><ToUserName><![CDATA[gh_f6b4da984c87]]></ToUserName> //微信公众号的微信号
//        //<FromUserName><![CDATA[oJxRO1Y2NgWJ9gMDyE3LwAYUNdAs]]></FromUserName> //openid用于获取用户信息，做登录使用
//        //<CreateTime>1531130986</CreateTime> //回调时间
//        //<MsgType><![CDATA[event]]></MsgType>
//        //<Event><![CDATA[SCAN]]></Event>
//        //<EventKey><![CDATA[lrfun.com.UxJkWC1531967386903]]></EventKey> //上面自定义的参数（scene_str）
//        //<Ticket><![CDATA[gQF57zwAAAAAAAAAAS5odHRwOi8vd2VpeGluLnFxLmNvbS9xLzAyY2ljbjB3RGtkZWwxbExLY3hyMVMAAgTvM0NbAwSAOgkA]]></Ticket> //换取二维码的ticket
//        //</xml>
//        if (callbackMap != null && callbackMap.get("FromUserName").toString() != null) {
//            // 通过openid获取用户信息
//            Map<String, Object> wechatUserInfoMap = getUserInfoByOpenid(callbackMap.get("FromUserName"));
//            // 将数据写入到数据库中，前面自定义的参数（scene_str）也需记录到数据库中，后面用于检测匹配登录
//            // INSERT INTO wechat_user_info......(数据库操作)
//        }
//    }
//
//    // xml转为map
//    private Map<String, String> xmlToMap(HttpServletRequest httpServletRequest) {
//        Map<String, String> map = new HashMap<String, String>();
//        try {
//            InputStream inputStream = httpServletRequest.getInputStream();
//            SAXReader reader = new SAXReader(); // 读取输入流
//            org.dom4j.Document document = reader.read(inputStream);
//            Element root = document.getRootElement(); // 得到xml根元素
//            List<Element> elementList = root.elements(); // 得到根元素的所有子节点
//            // 遍历所有子节点
//            for (Element e : elementList)
//                map.put(e.getName(), e.getText());
//            // 释放资源
//            inputStream.close();
//            inputStream = null;
//            return map;
//        } catch (Exception e) {
//            e.getMessage();
//        }
//        return null;
//    }
//}
//
