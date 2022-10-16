package org.dhorse.infrastructure.strategy.login;

import java.util.List;

import org.dhorse.api.param.user.UserLoginParam;
import org.dhorse.api.vo.GlobalConfigAgg;
import org.dhorse.api.vo.SysUser;
import org.dhorse.infrastructure.strategy.login.dto.LoginUser;
import org.springframework.security.crypto.password.PasswordEncoder;

public abstract class UserStrategy {

	public abstract LoginUser login(UserLoginParam userLoginParam,
			GlobalConfigAgg globalConfig, PasswordEncoder passwordEncoder);
	
	public abstract List<SysUser> search(String userName, GlobalConfigAgg globalConfig);
}
