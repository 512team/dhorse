package org.dhorse.infrastructure.strategy.login;

import java.util.List;
import java.util.stream.Collectors;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.ldap.LdapContext;

import org.dhorse.api.enums.MessageCodeEnum;
import org.dhorse.api.enums.RegisteredSourceEnum;
import org.dhorse.api.enums.RoleTypeEnum;
import org.dhorse.api.param.user.UserLoginParam;
import org.dhorse.api.response.model.GlobalConfigAgg;
import org.dhorse.api.response.model.SysUser;
import org.dhorse.api.response.model.GlobalConfigAgg.Ldap;
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

public class LdapUserStrategy extends UserStrategy {

	private static final Logger logger = LoggerFactory.getLogger(LdapUserStrategy.class);
	
	@Override
	public LoginUser login(UserLoginParam userLoginParam, GlobalConfigAgg globalConfig, PasswordEncoder passwordEncoder) {
		//1.首先使用登录的方式验证账户
		Attributes attrs = LdapUtils.authByDn(globalConfig.getLdap().getUrl(),
				"uid="+ userLoginParam.getLoginName() + "," + globalConfig.getLdap().getSearchBaseDn(), userLoginParam.getPassword());
		//2.如果登录失败，再采用比较密码的方式验证
		if(attrs == null) {
			Ldap ldap = globalConfig.getLdap();
			LdapContext ldapContext = LdapUtils.initContext(ldap.getUrl(),
					ldap.getAdminDn(), ldap.getAdminPassword());
			attrs = LdapUtils.authByComparePassword(ldapContext, ldap.getSearchBaseDn(), 
					userLoginParam.getLoginName(), userLoginParam.getPassword());
		}
		
		if(attrs == null) {
			return null;
		}
		
		LoginUser loginUser = buildUser(attrs);
		
		//本地库查询角色
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

	@Override
	public List<SysUser> search(String userName, GlobalConfigAgg globalConfig) {
		Ldap ldap = globalConfig.getLdap();
		LdapContext ldapContext = LdapUtils.initContext(ldap.getUrl(),
				ldap.getAdminDn(), ldap.getAdminPassword());
		if(ldapContext == null) {
			LogUtils.throwException(logger, MessageCodeEnum.INIT_LDAP_FAILURE);
		}
		List<Attributes> attributesList = LdapUtils.searchEntity(ldapContext, ldap.getSearchBaseDn(), userName);
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
			}
			if(attrCn != null) {
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
