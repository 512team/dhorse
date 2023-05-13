package org.dhorse.infrastructure.strategy.login;

import java.util.List;

import org.dhorse.api.param.user.UserLoginParam;
import org.dhorse.api.response.model.GlobalConfigAgg;
import org.dhorse.api.response.model.SysUser;
import org.dhorse.infrastructure.component.SpringBeanContext;
import org.dhorse.infrastructure.param.SysUserParam;
import org.dhorse.infrastructure.repository.SysUserRepository;
import org.dhorse.infrastructure.repository.po.SysUserPO;
import org.dhorse.infrastructure.strategy.login.dto.LoginUser;
import org.dhorse.infrastructure.utils.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;

public class NormalUserStrategy extends UserStrategy {

	@Override
	public LoginUser login(UserLoginParam userLoginParam, GlobalConfigAgg globalConfig,
			PasswordEncoder passwordEncoder) {
		SysUserRepository sysUserRepository = SpringBeanContext.getBean(SysUserRepository.class);
		SysUserPO sysUserPO = sysUserRepository.queryByLoginName(userLoginParam.getLoginName());
		if(sysUserPO == null) {
			return null;
		}
		if(!passwordEncoder.matches(userLoginParam.getPassword(), sysUserPO.getPassword())) {
			return null;
		}
		LoginUser loginUser = new LoginUser();
		BeanUtils.copyProperties(sysUserPO, loginUser);
		return loginUser;
	}

	@Override
	public List<SysUser> search(String userName, GlobalConfigAgg globalConfig) {
		SysUserRepository sysUserRepository = SpringBeanContext.getBean(SysUserRepository.class);
		SysUserParam userInfoParam = new SysUserParam();
		userInfoParam.setLoginName(userName);
		return sysUserRepository.likeRightList(userInfoParam);
	}
}
