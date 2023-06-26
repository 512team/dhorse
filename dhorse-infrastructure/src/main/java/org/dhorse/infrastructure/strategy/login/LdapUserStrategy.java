package org.dhorse.infrastructure.strategy.login;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.naming.AuthenticationException;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.ldap.LdapContext;

import org.dhorse.api.enums.MessageCodeEnum;
import org.dhorse.api.enums.RegisteredSourceEnum;
import org.dhorse.api.enums.RoleTypeEnum;
import org.dhorse.api.param.user.UserLoginParam;
import org.dhorse.api.response.model.GlobalConfigAgg;
import org.dhorse.api.response.model.GlobalConfigAgg.Ldap;
import org.dhorse.api.response.model.SysUser;
import org.dhorse.infrastructure.component.SpringBeanContext;
import org.dhorse.infrastructure.param.SysUserParam;
import org.dhorse.infrastructure.repository.SysUserRepository;
import org.dhorse.infrastructure.repository.po.SysUserPO;
import org.dhorse.infrastructure.strategy.login.dto.LoginUser;
import org.dhorse.infrastructure.utils.LdapUtils;
import org.dhorse.infrastructure.utils.LogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.CollectionUtils;

public class LdapUserStrategy extends UserStrategy {

	private static final Logger logger = LoggerFactory.getLogger(LdapUserStrategy.class);
	
	@Override
	public LoginUser login(UserLoginParam userLoginParam, GlobalConfigAgg globalConfig, PasswordEncoder passwordEncoder) {
		Attributes attrs = null;
		//1.首先用cn登录
		try {
			attrs = doLogin("cn", userLoginParam, globalConfig);
		}catch(Exception e) {
			if(e instanceof AuthenticationException && e.getMessage().contains("non-existant")) {
				logger.error("Failed to login by cn, message: {}", e.getMessage());
			}else {
				logger.error("Failed to login by cn", e);
			}
		}
		
		//2.如果cn登录不成功，则用uid登录
		if(attrs == null) {
			try {
				attrs = doLogin("uid", userLoginParam, globalConfig);
			}catch(Exception e) {
				logger.error("Failed to login by uid", e);
			}
		}
		
		LoginUser loginUser = buildUser(attrs);
		
		//3.本地库查询角色
		SysUserRepository sysUserRepository = SpringBeanContext.getBean(SysUserRepository.class);
		SysUserPO sysUserPO = sysUserRepository.queryByLoginName(userLoginParam.getLoginName());
		if(sysUserPO == null) {
			loginUser.setRoleType(RoleTypeEnum.NORMAL.getCode());
			//如果用户第一次登录，登记到内部系统
			SysUserParam bizParam = new SysUserParam();
			bizParam.setLoginName(loginUser.getLoginName());
			bizParam.setUserName(loginUser.getUserName());
			bizParam.setEmail(loginUser.getEmail());
			bizParam.setRegisteredSource(RegisteredSourceEnum.LDAP.getCode());
			bizParam.setRoleType(RoleTypeEnum.NORMAL.getCode());
			sysUserRepository.add(bizParam);
		}else{
			loginUser.setRoleType(sysUserPO.getRoleType());
		}
		
		return loginUser;
	}

	private Attributes doLogin(String type, UserLoginParam userLoginParam, GlobalConfigAgg globalConfig) throws NamingException {
		String dn = type + "=" + userLoginParam.getLoginName() + "," + globalConfig.getLdap().getSearchBaseDn();
		//1.首先使用登录的方式验证账户
		Attributes attrs = LdapUtils.authByDn(globalConfig.getLdap().getUrl(), dn, userLoginParam.getPassword());
		//2.如果登录失败，再采用比较密码的方式验证
		if(attrs == null) {
			Ldap ldap = globalConfig.getLdap();
			LdapContext ldapContext = LdapUtils.initContext(ldap.getUrl(),
					ldap.getAdminDn(), ldap.getAdminPassword());
			attrs = LdapUtils.authByComparePassword(ldapContext, ldap.getSearchBaseDn(), 
					userLoginParam.getLoginName(), userLoginParam.getPassword());
		}
		
		return attrs;
	}
	
	@Override
	public List<SysUser> search(String userName, GlobalConfigAgg globalConfig) {
		Ldap ldap = globalConfig.getLdap();
		LdapContext ldapContext = null;
		try {
			ldapContext = LdapUtils.initContext(ldap.getUrl(),
					ldap.getAdminDn(), ldap.getAdminPassword());
		} catch (NamingException e) {
			LogUtils.throwException(logger, e, MessageCodeEnum.INIT_LDAP_FAILURE);
		}
		List<Attributes> attributesList = LdapUtils.searchEntity(ldapContext, ldap.getSearchBaseDn(), userName);
		if(CollectionUtils.isEmpty(attributesList)) {
			return Collections.emptyList();
		}
		return attributesList.stream().map(e -> buildUser(e)).collect(Collectors.toList());
	}

	private LoginUser buildUser(Attributes attrs) {
		try {
			LoginUser loginUser = new LoginUser();
			Attribute attrUid = attrs.get("uid");
			Attribute attrCn = attrs.get("cn");
			Attribute attrEmail = attrs.get("email");
			if(attrUid != null) {
				loginUser.setLoginName((String)attrUid.get());
				loginUser.setUserName((String)attrCn.get());
			}else if(attrCn != null) {
				loginUser.setLoginName((String)attrCn.get());
				loginUser.setUserName((String)attrCn.get());
			}
			if(attrEmail != null) {
				loginUser.setEmail((String)attrEmail.get());
			}
			return loginUser;
		} catch (NamingException e) {
			logger.error("Failed to get user name.", e);
		}
		return null;
	}
}
