package org.dhorse.infrastructure.repository;

import java.util.List;
import java.util.stream.Collectors;

import org.dhorse.infrastructure.param.AppMemberParam;
import org.dhorse.infrastructure.repository.mapper.CustomizedBaseMapper;
import org.dhorse.infrastructure.repository.mapper.AppMemberMapper;
import org.dhorse.infrastructure.repository.po.AppMemberPO;
import org.dhorse.infrastructure.utils.QueryHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;

@Repository
public class AppMemberRepository
		extends BaseRepository<AppMemberParam, AppMemberPO> {

	@Autowired
	private AppMemberMapper mapper;

	public boolean deleteByUserId(String userId) {
		AppMemberPO po = new AppMemberPO();
		po.setUserId(userId);
		UpdateWrapper<AppMemberPO> wrapper = QueryHelper.buildUpdateWrapper(po);
		po.setDeletionStatus(1);
		return getMapper().update(po, wrapper) > 0 ? true : false;
	}
	
	public boolean deleteByAppId(String appId) {
		AppMemberPO po = new AppMemberPO();
		po.setAppId(appId);
		UpdateWrapper<AppMemberPO> wrapper = QueryHelper.buildUpdateWrapper(po);
		po.setDeletionStatus(1);
		return getMapper().update(po, wrapper) > 0 ? true : false;
	}
	
	public List<AppMemberPO> queryByLoginName(String loginName) {
		AppMemberParam appMemberParam = new AppMemberParam();
		appMemberParam.setLoginName(loginName);
		return super.list(appMemberParam);
	}
	
	public AppMemberPO queryByLoginNameAndAppId(String loginName, String appId) {
		AppMemberParam appMemberParam = new AppMemberParam();
		appMemberParam.setLoginName(loginName);
		appMemberParam.setAppId(appId);
		return super.query(appMemberParam);
	}
	
	@Override
	protected CustomizedBaseMapper<AppMemberPO> getMapper() {
		return mapper;
	}

	@Override
	protected AppMemberPO updateCondition(AppMemberParam bizParam) {
		AppMemberPO po = new AppMemberPO();
		po.setId(bizParam.getId());
		po.setUserId(bizParam.getUserId());
		return po;
	}
	
	protected AppMemberPO param2Entity(AppMemberParam bizParam) {
		AppMemberPO po = super.param2Entity(bizParam);
		if(CollectionUtils.isEmpty(bizParam.getRoleTypes())) {
			return po;
		}
		po.setRoleType(bizParam.getRoleTypes().stream().map(e -> e.toString()).collect(Collectors.joining(",")));
		return po;
	}
}
