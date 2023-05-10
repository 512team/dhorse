package org.dhorse.infrastructure.repository;

import java.util.Date;

import org.dhorse.infrastructure.param.MetricsParam;
import org.dhorse.infrastructure.repository.mapper.CustomizedBaseMapper;
import org.dhorse.infrastructure.repository.mapper.MetricsMapper;
import org.dhorse.infrastructure.repository.po.MetricsPO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;

@Repository
public class MetricsRepository
		extends BaseRepository<MetricsParam, MetricsPO> {

	@Autowired
	private MetricsMapper mapper;

	@Override
	protected CustomizedBaseMapper<MetricsPO> getMapper() {
		return mapper;
	}
	
	public void delete(Date time) {
		LambdaUpdateWrapper<MetricsPO> deleteWrapper = new LambdaUpdateWrapper<>();
		deleteWrapper.le(MetricsPO::getUpdateTime, time);
		mapper.delete(deleteWrapper);
	}
	
	@Override
	protected MetricsPO updateCondition(MetricsParam bizParam) {
		MetricsPO po = new MetricsPO();
		po.setId(bizParam.getId());
		return po;
	}

	@Override
	protected QueryWrapper<MetricsPO> buildQueryWrapper(MetricsParam bizParam, String orderField) {
		QueryWrapper<MetricsPO> wrapper = super.buildQueryWrapper(bizParam, orderField);
		wrapper.ge("update_time", bizParam.getStartTime());
		wrapper.le("update_time", bizParam.getEndTime());
		return wrapper;
	}

	
}