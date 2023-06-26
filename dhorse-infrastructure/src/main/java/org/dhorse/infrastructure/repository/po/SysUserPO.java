package org.dhorse.infrastructure.repository.po;

import java.util.Date;
import java.util.List;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 用户表
 * 
 * @author Dahai 2021-12-01
 */
@TableName("SYS_USER")
public class SysUserPO extends BasePO {

	private static final long serialVersionUID = 1L;

	/**
	 * 登录名
	 */
	private String loginName;

	/**
	 * 用户名
	 */
	private String userName;

	/**
	 * 登录密码
	 */
	private String password;

	/**
	 * 邮箱
	 */
	private String email;

	/**
	 * 0：普通用户，1：管理员
	 */
	private Integer roleType;

	/**
	 * 注册来源，1：DHorse，2：LDAP，3：SSO
	 */
	private Integer registeredSource;

	/**
	 * 上次登录时间
	 */
	private Date lastLoginTime;

	/**
	 * 上次登录token
	 */
	private String lastLoginToken;

	@TableField(exist = false)
	private List<String> loginNames;

	public String getLoginName() {
		return loginName;
	}

	public void setLoginName(String loginName) {
		this.loginName = loginName;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Integer getRoleType() {
		return roleType;
	}

	public void setRoleType(Integer roleType) {
		this.roleType = roleType;
	}

	public Integer getRegisteredSource() {
		return registeredSource;
	}

	public void setRegisteredSource(Integer registeredSource) {
		this.registeredSource = registeredSource;
	}

	public Date getLastLoginTime() {
		return lastLoginTime;
	}

	public void setLastLoginTime(Date lastLoginTime) {
		this.lastLoginTime = lastLoginTime;
	}

	public String getLastLoginToken() {
		return lastLoginToken;
	}

	public void setLastLoginToken(String lastLoginToken) {
		this.lastLoginToken = lastLoginToken;
	}

	public List<String> getLoginNames() {
		return loginNames;
	}

	public void setLoginNames(List<String> loginNames) {
		this.loginNames = loginNames;
	}

	@Override
	public List<String> getIds() {
		return this.ids;
	}

	@Override
	public String getId() {
		return this.id;
	}

	@Override
	public Integer getDeletionStatus() {
		return this.deletionStatus;
	}

}