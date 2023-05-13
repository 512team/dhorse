package org.dhorse.infrastructure.repository;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.dhorse.api.enums.RoleTypeEnum;
import org.dhorse.api.enums.TechTypeEnum;
import org.dhorse.api.response.model.AppEnv;
import org.dhorse.api.response.model.AppEnv.EnvExtendNode;
import org.dhorse.api.response.model.AppEnv.EnvExtendSpringBoot;
import org.dhorse.infrastructure.param.AppEnvParam;
import org.dhorse.infrastructure.repository.mapper.AppEnvMapper;
import org.dhorse.infrastructure.repository.mapper.CustomizedBaseMapper;
import org.dhorse.infrastructure.repository.po.AppEnvPO;
import org.dhorse.infrastructure.repository.po.AppMemberPO;
import org.dhorse.infrastructure.repository.po.AppPO;
import org.dhorse.infrastructure.strategy.login.dto.LoginUser;
import org.dhorse.infrastructure.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

@Repository
public class AppEnvRepository extends RightRepository<AppEnvParam, AppEnvPO, AppEnv> {

	@Autowired
	private AppEnvMapper mapper;
	
	@Autowired
	private AppRepository appRepository;

	@Override
	protected CustomizedBaseMapper<AppEnvPO> getMapper() {
		return mapper;
	}

	public List<AppEnvPO> list(List<String> appIds) {
		QueryWrapper<AppEnvPO> wrapper = new QueryWrapper<>();
		wrapper.in("app_id", appIds);
		wrapper.eq("deletion_status", 0);
		return mapper.selectList(wrapper);
	}
	
	public AppEnv query(LoginUser loginUser, AppEnvParam bizParam) {
		validateApp(bizParam.getAppId());
		if (!RoleTypeEnum.ADMIN.getCode().equals(loginUser.getRoleType())) {
			AppMemberPO appMember = appMemberRepository
					.queryByLoginNameAndAppId(loginUser.getLoginName(), bizParam.getAppId());
			if (appMember == null) {
				return null;
			}
		}
		
		AppEnvPO appEnvPO = super.query(bizParam);
		AppEnv dto = po2Dto(appEnvPO);
		if(!StringUtils.isBlank(appEnvPO.getExt())){
			AppPO appPO = appRepository.queryById(bizParam.getAppId());
			if(TechTypeEnum.SPRING_BOOT.getCode().equals(appPO.getTechType())) {
				dto.setEnvExtend(JsonUtils.parseToObject(appEnvPO.getExt(), EnvExtendSpringBoot.class));
			}else if(TechTypeEnum.NODE.getCode().equals(appPO.getTechType())) {
				dto.setEnvExtend(JsonUtils.parseToObject(appEnvPO.getExt(), EnvExtendNode.class));
			}
		}
		return dto;
	}
	
	@Override
	protected AppEnvPO updateCondition(AppEnvParam bizParam) {
		AppEnvPO po = new AppEnvPO();
		po.setId(bizParam.getId());
		return po;
	}
}