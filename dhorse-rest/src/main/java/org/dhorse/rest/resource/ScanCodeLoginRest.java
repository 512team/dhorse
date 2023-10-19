package org.dhorse.rest.resource;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.dhorse.api.enums.RegisteredSourceEnum;
import org.dhorse.api.param.user.UserLoginParam;
import org.dhorse.infrastructure.annotation.AccessNotLogin;
import org.dhorse.infrastructure.strategy.login.dto.LoginUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ScanCodeLoginRest extends AbstractRest {
	
	@AccessNotLogin
	@RequestMapping("/wechat")
	public String wechat(@RequestParam("code") String code, @RequestParam("state") String state,
			HttpServletResponse response) {
		UserLoginParam userLoginParam = new UserLoginParam();
		userLoginParam.setLoginSource(RegisteredSourceEnum.WECHAT.getCode());
		userLoginParam.setWechatCode(code);
		LoginUser loginUser = sysUserApplicationService.login(userLoginParam);
		Cookie c = new Cookie("login_token", loginUser.getLastLoginToken());
		response.addCookie(c);
		return "index.html";
	}
}
