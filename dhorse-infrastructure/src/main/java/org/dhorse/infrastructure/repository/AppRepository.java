package org.dhorse.infrastructure.repository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.dhorse.api.enums.LanguageTypeEnum;
import org.dhorse.api.enums.MessageCodeEnum;
import org.dhorse.api.enums.RoleTypeEnum;
import org.dhorse.api.enums.YesOrNoEnum;
import org.dhorse.api.result.PageData;
import org.dhorse.api.vo.App;
import org.dhorse.api.vo.App.AppExtend;
import org.dhorse.api.vo.AppExtendJava;
import org.dhorse.api.vo.AppExtendNode;
import org.dhorse.infrastructure.param.AppMemberParam;
import org.dhorse.infrastructure.param.AppParam;
import org.dhorse.infrastructure.repository.mapper.AppMapper;
import org.dhorse.infrastructure.repository.mapper.CustomizedBaseMapper;
import org.dhorse.infrastructure.repository.po.AppMemberPO;
import org.dhorse.infrastructure.repository.po.AppPO;
import org.dhorse.infrastructure.strategy.login.dto.LoginUser;
import org.dhorse.infrastructure.utils.BeanUtils;
import org.dhorse.infrastructure.utils.Constants;
import org.dhorse.infrastructure.utils.JsonUtils;
import org.dhorse.infrastructure.utils.LogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import com.baomidou.mybatisplus.core.metadata.IPage;

@Repository
public class AppRepository extends BaseRepository<AppParam, AppPO> {

	private static final Logger logger = LoggerFactory.getLogger(AppRepository.class);

	@Autowired
	private AppMapper appMapper;

	@Autowired
	private AppMemberRepository appMemberRepository;

	public PageData<App> page(LoginUser loginUser, AppParam bizParam) {
		// 如果admin角色的用户，直接查询应用表
		if (RoleTypeEnum.ADMIN.getCode().equals(loginUser.getRoleType())) {
			PageData<App> pageDto = pageData(super.page(bizParam));
			pageDto.getItems().forEach(e -> {
				e.setModifyRights(YesOrNoEnum.YES.getCode());
				e.setDeleteRights(YesOrNoEnum.YES.getCode());
			});
			return pageDto;
		}
		// 如果是普通用户，先查询应用成员表，再查询应用表
		AppMemberParam appMemberParam = new AppMemberParam();
		appMemberParam.setUserId(loginUser.getId());
		appMemberParam.setPageNum(bizParam.getPageNum());
		appMemberParam.setPageSize(bizParam.getPageSize());
		IPage<AppMemberPO> pageData = appMemberRepository.page(appMemberParam);
		if (CollectionUtils.isEmpty(pageData.getRecords())) {
			return pageData(bizParam);
		}
		Map<String, AppMemberPO> appMemberMap = pageData.getRecords().stream().collect(Collectors.toMap(AppMemberPO::getAppId, e -> e));
		bizParam.setIds(new ArrayList<>(appMemberMap.keySet()));
		PageData<App> pageDto = pageData(super.page(bizParam));
		//只有应用管理员才有修改（删除）权限
		for(App app : pageDto.getItems()) {
			AppMemberPO appUser = appMemberMap.get(app.getId());
			if(appUser.getLoginName().equals(loginUser.getLoginName())){
				String[] roleTypes = appUser.getRoleType().split(",");
				Set<Integer> roleSet = new HashSet<>();
				for (String role : roleTypes) {
					roleSet.add(Integer.valueOf(role));
				}
				List<Integer> adminRole = Constants.ROLE_OF_OPERATE_APP_USER.stream()
						.filter(item -> roleSet.contains(item))
						.collect(Collectors.toList());
				if(adminRole.size() > 0) {
					app.setModifyRights(YesOrNoEnum.YES.getCode());
					app.setDeleteRights(YesOrNoEnum.YES.getCode());
				}
			}

		}
		return pageDto;
	}

	public App queryWithExtendById(String id) {
		AppPO appPO = super.queryById(id);
		App app = po2Dto(appPO);
		app.setAppExtend(queryAppExtend(appPO));
		return app;
	}

	public App queryWithExtendById(LoginUser loginUser, String id) {
		if(StringUtils.isBlank(id)) {
			return null;
		}
		if (!RoleTypeEnum.ADMIN.getCode().equals(loginUser.getRoleType())) {
			AppMemberPO appMember = appMemberRepository
					.queryByLoginNameAndAppId(loginUser.getLoginName(), id);
			if (appMember == null) {
				return null;
			}
		}
		AppPO appPO = super.queryById(id);
		if(appPO == null) {
			return null;
		}
		App app = po2Dto(appPO);
		app.setAppExtend(queryAppExtend(appPO));
		return app;
	}

	public AppPO queryByAppName(String appName) {
		AppParam appInfoParam = new AppParam();
		appInfoParam.setAppName(appName);
		return super.query(appInfoParam);
	}

	private AppExtend queryAppExtend(AppPO appPO) {
		if(appPO == null) {
			return null;
		}
		if(StringUtils.isBlank(appPO.getExt())) {
			return null;
		}
		if (LanguageTypeEnum.JAVA.getCode().equals(appPO.getLanguageType())) {
			return JsonUtils.parseToObject(appPO.getExt(), AppExtendJava.class);
		}
		if (LanguageTypeEnum.NODE.getCode().equals(appPO.getLanguageType())) {
			return JsonUtils.parseToObject(appPO.getExt(), AppExtendNode.class);
		}
		return null;
	}

	public boolean update(LoginUser loginUser, AppParam bizParam) {
		if (!hasOperatingRights(loginUser, bizParam)) {
			LogUtils.throwException(logger, MessageCodeEnum.NO_ACCESS_RIGHT);
		}
		return super.updateById(bizParam);
	}

	public boolean delete(LoginUser loginUser, AppParam bizParam) {
		if (!hasOperatingRights(loginUser, bizParam)) {
			LogUtils.throwException(logger, MessageCodeEnum.NO_ACCESS_RIGHT);
		}
		return super.delete(bizParam.getId());
	}

	protected boolean hasOperatingRights(LoginUser loginUser, AppParam bizParam) {
		validateApp(bizParam.getId());
		AppPO e = queryById(bizParam.getId());
		if (e == null || !e.getId().equals(bizParam.getId())) {
			LogUtils.throwException(logger, MessageCodeEnum.RECORD_IS_INEXISTENCE);
		}

		if (RoleTypeEnum.ADMIN.getCode().equals(loginUser.getRoleType())) {
			return true;
		}
		AppMemberPO appUser = appMemberRepository
				.queryByLoginNameAndAppId(loginUser.getLoginName(), bizParam.getId());
		if (appUser == null || Objects.isNull(appUser.getRoleType())) {
			return false;
		}
		if (!appUser.getLoginName().equals(loginUser.getLoginName())) {
			return false;
		}
		String[] roleTypes = appUser.getRoleType().split(",");
		Set<Integer> roleSet = new HashSet<>();
		for (String role : roleTypes) {
			roleSet.add(Integer.valueOf(role));
		}
		List<Integer> adminRole = Constants.ROLE_OF_OPERATE_APP_USER.stream().filter(item -> roleSet.contains(item))
				.collect(Collectors.toList());
		return adminRole.size() > 0;
	}

	protected PageData<App> pageData(IPage<AppPO> pageEntity) {
		PageData<App> pageData = new PageData<>();
		pageData.setPageNum((int) pageEntity.getCurrent());
		pageData.setPageCount((int) pageEntity.getPages());
		pageData.setPageSize((int) pageEntity.getSize());
		pageData.setItemCount((int) pageEntity.getTotal());
		pageData.setItems(pos2Dtos(pageEntity.getRecords()));
		return pageData;
	}

	protected List<App> pos2Dtos(List<AppPO> pos) {
		return pos.stream().map(e -> po2Dto(e)).collect(Collectors.toList());
	}

	protected App po2Dto(AppPO e) {
		if (e == null) {
			return null;
		}
		App dto = new App();
		BeanUtils.copyProperties(e, dto);
		return dto;
	}

	protected PageData<App> pageData(AppParam bizParam) {
		PageData<App> pageData = new PageData<>();
		pageData.setPageNum(1);
		pageData.setPageCount(0);
		pageData.setPageSize(bizParam.getPageSize());
		pageData.setItemCount(0);
		pageData.setItems(null);
		return pageData;
	}

	@Override
	protected CustomizedBaseMapper<AppPO> getMapper() {
		return appMapper;
	}

	@Override
	protected AppPO updateCondition(AppParam bizParam) {
		AppPO po = new AppPO();
		po.setId(bizParam.getId());
		return po;
	}

	@Override
	protected AppPO param2Entity(AppParam bizParam) {
		AppPO po = super.param2Entity(bizParam);
		if(bizParam.getAppExtend() != null) {
			po.setExt(JsonUtils.toJsonString(bizParam.getAppExtend()));
		}
		return po;
	}
}
