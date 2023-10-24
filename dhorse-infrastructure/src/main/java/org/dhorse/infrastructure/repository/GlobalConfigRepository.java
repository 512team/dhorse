package org.dhorse.infrastructure.repository;

import java.util.Date;
import java.util.List;

import org.dhorse.api.enums.GlobalConfigItemTypeEnum;
import org.dhorse.api.response.model.GlobalConfigAgg;
import org.dhorse.api.response.model.GlobalConfigAgg.CodeRepo;
import org.dhorse.api.response.model.GlobalConfigAgg.DingDing;
import org.dhorse.api.response.model.GlobalConfigAgg.EnvTemplate;
import org.dhorse.api.response.model.GlobalConfigAgg.ImageRepo;
import org.dhorse.api.response.model.GlobalConfigAgg.Ldap;
import org.dhorse.api.response.model.GlobalConfigAgg.Maven;
import org.dhorse.api.response.model.GlobalConfigAgg.More;
import org.dhorse.api.response.model.GlobalConfigAgg.TraceTemplate;
import org.dhorse.api.response.model.GlobalConfigAgg.WeChat;
import org.dhorse.infrastructure.param.GlobalConfigParam;
import org.dhorse.infrastructure.repository.mapper.CustomizedBaseMapper;
import org.dhorse.infrastructure.repository.mapper.GlobalConfigMapper;
import org.dhorse.infrastructure.repository.po.GlobalConfigPO;
import org.dhorse.infrastructure.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;

@Repository
public class GlobalConfigRepository extends BaseRepository<GlobalConfigParam, GlobalConfigPO> {

	@Autowired
	private GlobalConfigMapper mapper;

	public GlobalConfigPO queryByItemType(Integer itemType) {
		GlobalConfigParam bizParam = new GlobalConfigParam();
		bizParam.setItemType(itemType);
		return query(bizParam);
	}
	
	public GlobalConfigAgg queryAgg(GlobalConfigParam bizParam) {
		GlobalConfigAgg globalConfig = new GlobalConfigAgg();
		QueryWrapper<GlobalConfigPO> queryWrapper = buildQueryWrapper(bizParam, null);
		List<GlobalConfigPO> pos = mapper.selectList(queryWrapper);
		if(CollectionUtils.isEmpty(pos)) {
			return globalConfig;
		}
		for(GlobalConfigPO po : pos) {
			if(StringUtils.isBlank(po.getItemValue())) {
				continue;
			}
			if(GlobalConfigItemTypeEnum.LDAP.getCode().equals(po.getItemType())) {
				globalConfig.setLdap(JsonUtils.parseToObject(po.getItemValue(), Ldap.class));
				continue;
			}
			if(GlobalConfigItemTypeEnum.WECHAT.getCode().equals(po.getItemType())) {
				WeChat wechat = JsonUtils.parseToObject(po.getItemValue(), WeChat.class);
				globalConfig.setWechat(wechat);
				continue;
			}
			if(GlobalConfigItemTypeEnum.DINGDING.getCode().equals(po.getItemType())) {
				DingDing dingding = JsonUtils.parseToObject(po.getItemValue(), DingDing.class);
				globalConfig.setDingding(dingding);
				continue;
			}
			if(GlobalConfigItemTypeEnum.CODEREPO.getCode().equals(po.getItemType())) {
				globalConfig.setCodeRepo(JsonUtils.parseToObject(po.getItemValue(), CodeRepo.class));
				continue;
			}
			if(GlobalConfigItemTypeEnum.IMAGEREPO.getCode().equals(po.getItemType())) {
				globalConfig.setImageRepo(JsonUtils.parseToObject(po.getItemValue(), ImageRepo.class));
				continue;
			}
			if(GlobalConfigItemTypeEnum.MAVEN.getCode().equals(po.getItemType())) {
				globalConfig.setMaven(JsonUtils.parseToObject(po.getItemValue(), Maven.class));
				continue;
			}
			if(GlobalConfigItemTypeEnum.TRACE_TEMPLATE.getCode().equals(po.getItemType())) {
				TraceTemplate traceTemplate = JsonUtils.parseToObject(po.getItemValue(), TraceTemplate.class);
				globalConfig.setTraceTemplate(po.getId(), traceTemplate);
				continue;
			}
			if(GlobalConfigItemTypeEnum.ENV_TEMPLATE.getCode().equals(po.getItemType())) {
				EnvTemplate template = JsonUtils.parseToObject(po.getItemValue(), EnvTemplate.class);
				template.setId(po.getId());
				template.setCreationTime(po.getCreationTime());
				template.setUpdateTime(po.getUpdateTime());
				globalConfig.setEnvTemplate(template);
				continue;
			}
			if(GlobalConfigItemTypeEnum.MORE.getCode().equals(po.getItemType())) {
				More more = JsonUtils.parseToObject(po.getItemValue(), More.class);
				globalConfig.setMore(more);
				continue;
			}
		}
		return globalConfig;
	}

	@Override
	protected CustomizedBaseMapper<GlobalConfigPO> getMapper() {
		return mapper;
	}

	public boolean updateByMoreCondition(GlobalConfigParam bizParam) {
		UpdateWrapper<GlobalConfigPO> wrapper = new UpdateWrapper<>();
		wrapper.eq("item_type", bizParam.getItemType());
		wrapper.lt("item_value", bizParam.getItemValue());
		GlobalConfigPO e = param2Entity(bizParam);
		e.setUpdateTime(new Date());
		return getMapper().update(e, wrapper) > 0 ? true : false;
	}
	
	@Override
	protected GlobalConfigPO updateCondition(GlobalConfigParam bizParam) {
		GlobalConfigPO po = new GlobalConfigPO();
		po.setItemType(bizParam.getItemType());
		po.setId(bizParam.getId());
		return po;
	}
}
