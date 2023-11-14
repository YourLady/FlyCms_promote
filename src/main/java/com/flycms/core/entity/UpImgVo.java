package com.flycms.core.entity;

import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;

/**
 * 
 * 开发公司：97560.com<br/>
 * 版权：97560.com<br/>
 * <p>
 * 
 * 实体类
 * 
 * <p>
 * 
 * 区分　责任人　日期　　　　说明<br/>
 * 创建　孙开飞　2017年5月25日 　<br/>
 * <p>
 * *******
 * <p>
 * 
 * @author sun-kaifei
 * @email admin@97560.com
 * @version 1.0,2017年7月25日 <br/>
 * 
 */
public class UpImgVo implements Serializable {
	private static final long serialVersionUID = 1L;
	private String userid;
	private String promoteid;
	private MultipartFile avatar;

	public String getUserid() {
		return userid;
	}

	public void setUserid(String userid) {
		this.userid = userid;
	}

	public String getPromoteid() {
		return promoteid;
	}

	public void setPromoteid(String promoteid) {
		this.promoteid = promoteid;
	}

	public MultipartFile getAvatar() {
		return avatar;
	}

	public void setAvatar(MultipartFile avatar) {
		this.avatar = avatar;
	}
}
