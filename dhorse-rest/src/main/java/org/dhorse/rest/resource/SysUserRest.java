package org.dhorse.rest.resource;

import java.util.List;

import org.dhorse.api.param.user.UserCreationParam;
import org.dhorse.api.param.user.UserDeletionParam;
import org.dhorse.api.param.user.UserLoginParam;
import org.dhorse.api.param.user.UserPageParam;
import org.dhorse.api.param.user.UserQueryParam;
import org.dhorse.api.param.user.UserSearchParam;
import org.dhorse.api.param.user.PasswordSetParam;
import org.dhorse.api.param.user.PasswordUpdateParam;
import org.dhorse.api.param.user.RoleUpdateParam;
import org.dhorse.api.param.user.UserUpdateParam;
import org.dhorse.api.result.PageData;
import org.dhorse.api.result.RestResponse;
import org.dhorse.api.vo.SysUser;
import org.dhorse.infrastructure.annotation.AccessNotLogin;
import org.dhorse.infrastructure.annotation.AccessOnlyAdmin;
import org.dhorse.infrastructure.exception.ApplicationException;
import org.dhorse.infrastructure.strategy.login.dto.LoginUser;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 
 * 用户
 * 
 * @author Dahai
 */
@RestController
@RequestMapping("/sysUser")
public class SysUserRest extends AbstractRest {

	/**
	 * 登录
	 * 
	 * @param userLoginParam 登录参数
	 * @return 登录用户信息
	 */
	@AccessNotLogin
	@PostMapping("/login")
	public RestResponse<LoginUser> login(@RequestBody UserLoginParam userLoginParam) {
		try {
			return this.success(sysUserApplicationService.login(userLoginParam));
		} catch (ApplicationException e) {
			return this.error(e);
		}
	}
	
	/**
	 * 登录
	 * 
	 * @param queryLoginUser 登录参数
	 * @return 登录用户信息
	 */
	@PostMapping("/queryLoginUser")
	public RestResponse<LoginUser> login(@CookieValue(name = "login_token", required = false) String loginToken) {
		try {
			return this.success(sysUserApplicationService.queryLoginUserByToken(loginToken));
		} catch (ApplicationException e) {
			return this.error(e);
		}
	}

	/**
	 * 退出
	 * 
	 * @param loginToken 登录的token
	 * @return 无
	 */
	@PostMapping("/logout")
	public RestResponse<Void> logout(@CookieValue("login_token") String loginToken) {
		try {
			return this.success(sysUserApplicationService.logout(this.queryLoginUserByToken(loginToken)));
		} catch (ApplicationException e) {
			return this.error(e);
		}
	}

	/**
	 * 搜索
	 * 
	 * @param usersearchParam 搜索参数
	 * @return 符合条件的用户列表
	 */
	@PostMapping("/search")
	public RestResponse<List<SysUser>> search(@RequestBody UserSearchParam usersearchParam) {
		try {
			return this.success(sysUserApplicationService.search(usersearchParam));
		} catch (ApplicationException e) {
			return this.error(e);
		}
	}

	/**
	 * 分页查询
	 * 
	 * @param sysUserParam 查询参数
	 * @return 符合条件的用户分页数据
	 */
	@AccessOnlyAdmin
	@PostMapping("/page")
	public RestResponse<PageData<SysUser>> page(@RequestBody UserPageParam userPageParam) {
		try {
			return this.success(sysUserApplicationService.page(userPageParam));
		} catch (ApplicationException e) {
			return this.error(e);
		}
	}

	/**
	 * 单个查询
	 * 
	 * @param sysUserParam 查询参数
	 * @return 符合条件的用户
	 */
	@AccessOnlyAdmin
	@PostMapping("/query")
	public RestResponse<SysUser> query(@RequestBody UserQueryParam userQueryParam) {
		try {
			return this.success(sysUserApplicationService.query(userQueryParam));
		} catch (ApplicationException e) {
			return this.error(e);
		}
	}

	/**
	 * 创建
	 * 
	 * @param sysUserParam 创建参数
	 * @return 无
	 */
	@AccessOnlyAdmin
	@PostMapping("/createUser")
	public RestResponse<Void> createUser(@RequestBody UserCreationParam userCreationParam) {
		try {
			return this.success(sysUserApplicationService.createUser(userCreationParam));
		} catch (ApplicationException e) {
			return this.error(e);
		}
	}

	/**
	 * 修改
	 * 
	 * @param sysUserParam 修改用户参数
	 * @return 无
	 */
	@AccessOnlyAdmin
	@PostMapping("/updateUser")
	public RestResponse<Void> updateUser(@RequestBody UserUpdateParam userUpdateParam) {
		try {
			return this.success(sysUserApplicationService.updateUser(userUpdateParam));
		} catch (ApplicationException e) {
			return this.error(e);
		}
	}

	/**
	 * 删除
	 * 
	 * @param sysUserParam 删除参数
	 * @return 无
	 */
	@AccessOnlyAdmin
	@PostMapping("/deleteUser")
	public RestResponse<Void> deleteUser(@RequestBody UserDeletionParam userDeletionParam) {
		try {
			return this.success(sysUserApplicationService.deleteUser(userDeletionParam));
		} catch (ApplicationException e) {
			return this.error(e);
		}
	}

	/**
	 * 修改密码
	 * 
	 * @param loginToken 登录token
	 * @param sysUserParam 密码参数
	 * @return 无
	 */
	@PostMapping("/updatePassword")
	public RestResponse<Void> updatePassword(@CookieValue("login_token") String loginToken,
			@RequestBody PasswordUpdateParam passwordUpdateParam) {
		try {
			return this.success(
					sysUserApplicationService.updatePassword(this.queryLoginUserByToken(loginToken), passwordUpdateParam));
		} catch (ApplicationException e) {
			return this.error(e);
		}
	}
	
	/**
	 * 重置登录密码
	 *<p>只有管理员角色才有权限。
	 * 
	 * @param passwordSetParam 密码参数
	 * @return 无
	 */
	@AccessOnlyAdmin
	@PostMapping("/setPassword")
	public RestResponse<Void> setPassword(@RequestBody PasswordSetParam passwordSetParam) {
		try {
			return this.success(sysUserApplicationService.setPassword(passwordSetParam));
		} catch (ApplicationException e) {
			return this.error(e);
		}
	}

	/**
	 * 修改角色类型
	 * 
	 * @param roleUpdateParam 参数模型
	 * @return 无
	 */
	@AccessOnlyAdmin
	@PostMapping("/updateUserRole")
	public RestResponse<Void> updateUserRole(@RequestBody RoleUpdateParam roleUpdateParam) {
		try {
			return this.success(sysUserApplicationService.updateUserRole(roleUpdateParam));
		} catch (ApplicationException e) {
			return this.error(e);
		}
	}

}
