package org.dhorse.application.service;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.dhorse.api.enums.MessageCodeEnum;
import org.dhorse.api.enums.RegisteredSourceEnum;
import org.dhorse.api.enums.RoleTypeEnum;
import org.dhorse.api.param.user.PasswordSetParam;
import org.dhorse.api.param.user.PasswordUpdateParam;
import org.dhorse.api.param.user.RoleUpdateParam;
import org.dhorse.api.param.user.UserCreationParam;
import org.dhorse.api.param.user.UserDeletionParam;
import org.dhorse.api.param.user.UserLoginParam;
import org.dhorse.api.param.user.UserPageParam;
import org.dhorse.api.param.user.UserQueryParam;
import org.dhorse.api.param.user.UserSearchParam;
import org.dhorse.api.param.user.UserUpdateParam;
import org.dhorse.api.response.PageData;
import org.dhorse.api.response.model.GlobalConfigAgg;
import org.dhorse.api.response.model.SysUser;
import org.dhorse.infrastructure.param.SysUserParam;
import org.dhorse.infrastructure.repository.po.SysUserPO;
import org.dhorse.infrastructure.strategy.login.DingDingUserStrategy;
import org.dhorse.infrastructure.strategy.login.LdapUserStrategy;
import org.dhorse.infrastructure.strategy.login.NormalUserStrategy;
import org.dhorse.infrastructure.strategy.login.CasUserStrategy;
import org.dhorse.infrastructure.strategy.login.UserStrategy;
import org.dhorse.infrastructure.strategy.login.WeChatUserStrategy;
import org.dhorse.infrastructure.strategy.login.dto.LoginUser;
import org.dhorse.infrastructure.strategy.login.param.LoginUserParam;
import org.dhorse.infrastructure.utils.BeanUtils;
import org.dhorse.infrastructure.utils.Constants;
import org.dhorse.infrastructure.utils.GuavaCacheUtils;
import org.dhorse.infrastructure.utils.LogUtils;
import org.dhorse.infrastructure.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户应用服务
 * 
 * @author Dahai
 */
@Service
public class SysUserApplicationService extends BaseApplicationService<SysUser, SysUserPO> {

	private static final Logger logger = LoggerFactory.getLogger(SysUserApplicationService.class);

	@Autowired
	private PasswordEncoder passwordEncoder;

	public LoginUser login(UserLoginParam userLoginParam) {
		if (null == userLoginParam.getLoginSource()) {
			LogUtils.throwException(logger, MessageCodeEnum.LOGIN_SOURCE_IS_EMPTY);
		}
		
		if(RegisteredSourceEnum.DHORSE.getCode().equals(userLoginParam.getLoginSource())
				|| RegisteredSourceEnum.LDAP.getCode().equals(userLoginParam.getLoginSource())) {
			if (StringUtils.isEmpty(userLoginParam.getLoginName())) {
				LogUtils.throwException(logger, MessageCodeEnum.LOGIN_NAME_IS_EMPTY);
			}
			if (StringUtils.isEmpty(userLoginParam.getPassword())) {
				LogUtils.throwException(logger, MessageCodeEnum.PASSWORD_IS_EMPTY);
			}
		}
		
		GlobalConfigAgg globalConfig = globalConfig();
		if(RegisteredSourceEnum.LDAP.getCode().equals(userLoginParam.getLoginSource())
				&& globalConfig.getLdap() == null) {
			LogUtils.throwException(logger, MessageCodeEnum.LDAP_CONFIG_FAILURE);
		}
		
		UserStrategy userStrategy = userStrategy(userLoginParam.getLoginSource());
		LoginUser loginUser = userStrategy.login(userLoginParam, globalConfig, passwordEncoder);
		if (loginUser == null) {
			LogUtils.throwException(logger, MessageCodeEnum.USER_LOGIN_FAILED);
		}

		// 修改登录token和登录时间
		String loginToken = UUID.randomUUID().toString().replace("-", "");
		LoginUserParam bizParam = new LoginUserParam();
		bizParam.setLoginName(loginUser.getLoginName());
		bizParam.setLastLoginToken(loginToken);
		bizParam.setLastLoginTime(new Date());
		sysUserRepository.update(bizParam);

		loginUser.setLastLoginToken(loginToken);
		loginUser.setLastLoginTime(bizParam.getLastLoginTime());

		// 记录缓存
		GuavaCacheUtils.putLoginUser(loginToken, loginUser);
		return loginUser;
	}

	public Void logout(LoginUser loginUser) {
		if (loginUser == null) {
			return null;
		}
		if (validLoginTime(loginUser.getLastLoginTime())) {
			LoginUserParam bizParam = new LoginUserParam();
			bizParam.setLoginName(loginUser.getLoginName());
			bizParam.setLastLoginToken(UUID.randomUUID().toString().replace("-", ""));
			sysUserRepository.update(bizParam);
		}
		GuavaCacheUtils.removeLoginUserByLoginName(loginUser.getLoginName());
		return null;
	}

	public LoginUser queryLoginUserByToken(String loginToken) {
		if (StringUtils.isBlank(loginToken)) {
			LogUtils.throwException(logger, MessageCodeEnum.SYS_USER_NOT_LOGINED);
		}
		LoginUser loginUser = GuavaCacheUtils.getLoginUserByToken(loginToken);
		if (loginUser != null) {
			// 由于guava缓存没有不能设置每个key的失效时间，缓存的有效时长可能大于登录时长
			// 所以需要做过期验证，如果过期，则清除登录数据
			if (validLoginTime(loginUser.getLastLoginTime())) {
				return loginUser;
			}
			GuavaCacheUtils.removeLoginUserByToken(loginToken);
		}

		loginUser = sysUserRepository.queryLoginUser(loginToken);
		if (loginUser == null) {
			LogUtils.throwException(logger, MessageCodeEnum.SYS_USER_NOT_LOGINED);
		}

		if (loginUser.getLastLoginTime() == null) {
			LogUtils.throwException(logger, MessageCodeEnum.SYS_USER_NOT_LOGINED);
		}

		if (validLoginTime(loginUser.getLastLoginTime())) {
			GuavaCacheUtils.putLoginUser(loginUser.getLastLoginToken(), loginUser);
			return loginUser;
		}else {
			LogUtils.throwException(logger, MessageCodeEnum.SYS_USER_NOT_LOGINED);
		}

		return null;
	}

	/**
	 * 根据登录查询登录信息，这里只从缓存里查，不查数据库，因为拦截器会把登录信息放入缓存。
	 * 
	 * @param loginName 登录名
	 * @return 登录用户信息
	 */
	protected LoginUser queryLoginUserByLoginName(String loginName) {
		LoginUser loginUser = GuavaCacheUtils.getLoginUserByLoginName(loginName);
		if (loginUser != null) {
			// 由于guava缓存没有不能设置每个key的失效时间，缓存的有效时长可能大于登录时长
			// 所以需要做过期验证，如果过期，则清除登录数据
			if (validLoginTime(loginUser.getLastLoginTime())) {
				return loginUser;
			}
			GuavaCacheUtils.removeLoginUserByLoginName(loginName);
		}

		return null;
	}

	protected boolean validLoginTime(Date lastLoginTime) {
		if (lastLoginTime == null) {
			return false;
		}
		return new Date().getTime() - lastLoginTime.getTime() < Constants.HOUR_24;
	}

	/**
	 * 搜索用户<p>
	 * 该方法会一次性把所有的用户查询出来，然后通过前端过滤组件来实现搜索
	 * 如果用户量较大的话，会存在性能问题，后期可以实现真正的后端搜索
	 * 
	 * @param usersearchParam 搜索用户参数
	 * @return 符合条件的用户列表
	 */
	public List<SysUser> search(LoginUser loginUser, UserSearchParam usersearchParam) {
		//目前只支持从dhorse搜索用户，从别的渠道首先登录一下，会保存到dhorse系统里
		//如果后期需要从别的渠道搜索用户的话，实现方案如下：
		//记录用户最后的登录渠道，然后从该渠道搜索用户
		UserStrategy userStrategy = userStrategy(RegisteredSourceEnum.DHORSE.getCode());
		return userStrategy.search(usersearchParam.getLoginName(), this.globalConfig());
	}

	/**
	 * 
	 * 查询用户列表
	 * 
	 * @param 用户参数
	 * @return 符合条件的用户列表
	 */
	public PageData<SysUser> page(UserPageParam userPageParam) {
		SysUserParam sysUserParam = buildSysUserParam(userPageParam);
		return sysUserRepository.likeRightPage(sysUserParam);
	}

	/**
	 * 
	 * 查询用户
	 * 
	 * @param
	 * @return
	 */
	public SysUser query(UserQueryParam userQueryParam) {
		SysUserPO sysUserPO = needUserToExist(userQueryParam.getLoginName());
		SysUser sysUser = new SysUser();
		BeanUtils.copyProperties(sysUserPO, sysUser);
		return sysUser;
	}

	/**
	 * 
	 * 创建用户
	 * 
	 * @param
	 * @return
	 */
	public Void createUser(UserCreationParam userCreationParam) {

		checkLoginName(userCreationParam.getLoginName());
		checkRole(userCreationParam.getRoleType());
		notNeedUserToExist(userCreationParam.getLoginName());
		checkUserName(userCreationParam.getUserName());
		checkPassword(userCreationParam.getPassword(), userCreationParam.getConfirmPassword());

		SysUserParam sysUserParam = buildSysUserParam(userCreationParam);
		sysUserParam.setRegisteredSource(RegisteredSourceEnum.DHORSE.getCode());
		sysUserParam.setPassword(passwordEncoder.encode(userCreationParam.getPassword()));
		sysUserRepository.add(sysUserParam);
		return null;
	}

	/**
	 * 
	 * 修改用户
	 * 
	 * @param
	 * @return
	 */
	public Void updateUser(UserUpdateParam userUpdateParam) {
		checkLoginName(userUpdateParam.getLoginName());
		checkRole(userUpdateParam.getRoleType());
		checkUserName(userUpdateParam.getUserName());
		SysUserPO sysUser = needUserToExist(userUpdateParam.getLoginName());
		SysUserParam sysUserParam = buildSysUserParam(userUpdateParam);
		sysUserParam.setId(sysUser.getId());
		if (sysUserRepository.updateById(sysUserParam)) {
			GuavaCacheUtils.removeLoginUserByLoginName(userUpdateParam.getLoginName());
		}
		return null;
	}

	/**
	 * 
	 * 删除用户
	 * 
	 * @param
	 * @return
	 */
	@Transactional(rollbackFor = Throwable.class)
	public Void deleteUser(UserDeletionParam userDeletionParam) {
		checkLoginName(userDeletionParam.getLoginName());
		SysUserPO sysUser = needUserToExist(userDeletionParam.getLoginName());
		if (sysUserRepository.delete(sysUser.getId())) {
			GuavaCacheUtils.removeLoginUserByLoginName(userDeletionParam.getLoginName());
			appMemberRepository.deleteByUserId(sysUser.getId());
		}
		return null;
	}

	/**
	 * 
	 * 修改密码
	 * 
	 * @param
	 * @return
	 */
	public Void updatePassword(LoginUser loginUser, PasswordUpdateParam passwordUpdateParam) {
		if (StringUtils.isBlank(passwordUpdateParam.getOldPassword())) {
			LogUtils.throwException(logger, MessageCodeEnum.OLD_PASSWORD_IS_EMPTY);
		}
		checkPassword(passwordUpdateParam.getPassword(), passwordUpdateParam.getConfirmPassword());
		SysUserPO sysUserPO = sysUserRepository.queryByLoginName(loginUser.getLoginName());
		if (sysUserPO == null) {
			LogUtils.throwException(logger, MessageCodeEnum.SYS_USER_IS_INEXISTENCE);
		}
		if (!passwordEncoder.matches(passwordUpdateParam.getOldPassword(), sysUserPO.getPassword())) {
			LogUtils.throwException(logger, MessageCodeEnum.OLD_PASSWORD_FAILED);
		}
		SysUserParam sysUserParam = new SysUserParam();
		sysUserParam.setId(loginUser.getId());
		sysUserParam.setPassword(passwordEncoder.encode(passwordUpdateParam.getPassword()));
		sysUserParam.setLastLoginToken("0");
		if (sysUserRepository.updateById(sysUserParam)) {
			GuavaCacheUtils.removeLoginUserByLoginName(loginUser.getLoginName());
		}
		return null;
	}

	/**
	 * 
	 * 重置密码
	 * 
	 * @param
	 * @return
	 */
	public Void setPassword(PasswordSetParam passwordSetParam) {
		if(StringUtils.isBlank(passwordSetParam.getLoginName())) {
			LogUtils.throwException(logger, MessageCodeEnum.LOGIN_NAME_IS_EMPTY);
		}
		checkPassword(passwordSetParam.getPassword(), passwordSetParam.getConfirmPassword());
		SysUserPO sysUserPO = sysUserRepository.queryByLoginName(passwordSetParam.getLoginName());
		if (sysUserPO == null) {
			LogUtils.throwException(logger, MessageCodeEnum.SYS_USER_IS_INEXISTENCE);
		}
		SysUserParam sysUserParam = new SysUserParam();
		sysUserParam.setId(sysUserPO.getId());
		sysUserParam.setPassword(passwordEncoder.encode(passwordSetParam.getPassword()));
		sysUserParam.setLastLoginToken("0");
		if (sysUserRepository.updateById(sysUserParam)) {
			GuavaCacheUtils.removeLoginUserByLoginName(passwordSetParam.getLoginName());
		}
		return null;
	}

	/**
	 * 
	 * 修改角色类型，角色枚举：org.dhorse.api.enums.RoleTypeEnum
	 * 
	 * @param
	 * @return
	 */
	public Void updateUserRole(RoleUpdateParam roleUpdateParam) {
		checkLoginName(roleUpdateParam.getLoginName());
		checkRole(roleUpdateParam.getRoleType());
		SysUserPO sysUser = needUserToExist(roleUpdateParam.getLoginName());
		SysUserParam sysUserParam = buildSysUserParam(roleUpdateParam);
		sysUserParam.setId(sysUser.getId());
		if (sysUserRepository.update(sysUserParam)) {
			GuavaCacheUtils.removeLoginUserByLoginName(roleUpdateParam.getLoginName());
		}
		return null;
	}

	private SysUserParam buildSysUserParam(Serializable userParam) {
		SysUserParam sysUserParam = new SysUserParam();
		BeanUtils.copyProperties(userParam, sysUserParam);
		return sysUserParam;
	}

	private void checkLoginName(String loginName) {
		if (StringUtils.isBlank(loginName)) {
			LogUtils.throwException(logger, MessageCodeEnum.LOGIN_NAME_IS_EMPTY);
		}
	}

	private void checkUserName(String userName) {
		if (StringUtils.isBlank(userName)) {
			LogUtils.throwException(logger, MessageCodeEnum.USER_NAME_IS_EMPTY);
		}
	}

	private void checkPassword(String password, String confirmPassword) {
		if (StringUtils.isBlank(password)) {
			LogUtils.throwException(logger, MessageCodeEnum.PASSWORD_IS_EMPTY);
		}
		if (StringUtils.isBlank(confirmPassword)) {
			LogUtils.throwException(logger, MessageCodeEnum.CONFIRM_PASSWORD_IS_EMPTY);
		}
		if (!password.equals(confirmPassword)) {
			LogUtils.throwException(logger, MessageCodeEnum.PASSWORD_NOT_SAME);
		}
	}

	private void checkRole(Integer roleCode) {
		if (Objects.isNull(roleCode)) {
			LogUtils.throwException(logger, MessageCodeEnum.ROLE_TYPE_IS_EMPTY);
		}
		if (RoleTypeEnum.getByCode(roleCode) == null) {
			LogUtils.throwException(logger, MessageCodeEnum.INVALID_USER_ROLE);
		}
	}

	private void notNeedUserToExist(String loginName) {
		if (queryByLoginName(loginName) != null) {
			LogUtils.throwException(logger, MessageCodeEnum.LOGIN_USER_IS_EXISTENCE);
		}
	}

	private SysUserPO needUserToExist(String loginName) {
		SysUserPO sysUserPO = queryByLoginName(loginName);
		if (sysUserPO == null) {
			LogUtils.throwException(logger, MessageCodeEnum.SYS_USER_IS_INEXISTENCE);
		}
		return sysUserPO;
	}

	private SysUserPO queryByLoginName(String loginName) {
		checkLoginName(loginName);
		return sysUserRepository.queryByLoginName(loginName);
	}

	private UserStrategy userStrategy(Integer loginSource) {
		if (RegisteredSourceEnum.DHORSE.getCode().equals(loginSource)) {
			return new NormalUserStrategy();
		} else if (RegisteredSourceEnum.LDAP.getCode().equals(loginSource)) {
			return new LdapUserStrategy();
		}else if (RegisteredSourceEnum.WECHAT.getCode().equals(loginSource)) {
			return new WeChatUserStrategy();
		}else if (RegisteredSourceEnum.DINGDING.getCode().equals(loginSource)) {
			return new DingDingUserStrategy();
		}else if (RegisteredSourceEnum.CAS.getCode().equals(loginSource)) {
			return new CasUserStrategy();
		}
		return null;
	}
}