package com.flycms.module.config.service;

import com.aliyuncs.dysmsapi.model.v20170525.QuerySendDetailsResponse;
import com.flycms.core.utils.MathUtils;
import com.flycms.core.utils.SnowFlake;
import com.flycms.module.user.dao.UserDao;
import com.flycms.module.user.model.UserActivation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.sms.v20210111.SmsClient;
import com.tencentcloudapi.sms.v20210111.models.*;

import java.util.Date;
@Service
public class TencentSmsapiService {
    static final String SecretId="AKIDgJhvvhEXcT5TcBdtxnOmxwkQuzs67wYy";
    static final String SecretKey="INJ1WotmSt7AtPgkeLo46uUpy2tVwCG6";
    static final String domain = "sms.tencentcloudapi.com";
    @Autowired
    private UserDao userDao;

    public void SendMsgTen(String telPhone,int type){
        try{
            // 实例化一个认证对象，入参需要传入腾讯云账户 SecretId 和 SecretKey，此处还需注意密钥对的保密
            // 代码泄露可能会导致 SecretId 和 SecretKey 泄露，并威胁账号下所有资源的安全性。以下代码示例仅供参考，建议采用更安全的方式来使用密钥，请参见：https://cloud.tencent.com/document/product/1278/85305
            // 密钥可前往官网控制台 https://console.cloud.tencent.com/cam/capi 进行获取
            Credential cred = new Credential(SecretId, SecretKey);
            // 实例化一个http选项，可选的，没有特殊需求可以跳过
            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setEndpoint(domain);
            // 实例化一个client选项，可选的，没有特殊需求可以跳过
            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(httpProfile);
            // 实例化要请求产品的client对象,clientProfile是可选的
            SmsClient client = new SmsClient(cred, "ap-nanjing", clientProfile);
            // 实例化一个请求对象,每个接口都会对应一个request对象
            SendSmsRequest req = new SendSmsRequest();
            String[] phoneNumberSet1 = {"+86"+telPhone};
            req.setPhoneNumberSet(phoneNumberSet1);
            req.setSignName("夜航星教育");
            req.setSmsSdkAppId("1400866824");
            req.setTemplateId("1983283");
            String checkCode=MathUtils.getRandomCode(6);
            String[] templateParamSet1 = {checkCode, "5"};
            req.setTemplateParamSet(templateParamSet1);
            // 返回的resp是一个SendSmsResponse的实例，与请求对象对应
            SendSmsResponse response = client.SendSms(req);
            // 输出json格式的字符串回包
            Thread.sleep(3000L);
            //response.getRequestId() &&
            //查明细
            if(response.getSendStatusSet()[0].getCode()!= null && response.getSendStatusSet()[0].getCode().equals("Ok")) {
                        UserActivation activation=new UserActivation();
                        SnowFlake snowFlake = new SnowFlake(2, 3);
                        activation.setId(snowFlake.nextId());
                        activation.setUserName(telPhone);
                        activation.setCode(checkCode);
                        activation.setCodeType(type);
                        activation.setReferStatus(0);
                        activation.setReferTime(new Date());
                        userDao.addUserActivation(activation);
            }
        } catch (TencentCloudSDKException e) {
            //System.out.println(e.toString());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}