package org.dhorse.infrastructure.repository;

import java.util.Date;

import org.dhorse.api.vo.ReplicaMetrics;
import org.dhorse.infrastructure.param.ReplicaMetricsParam;
import org.dhorse.infrastructure.repository.mapper.CustomizedBaseMapper;
import org.dhorse.infrastructure.repository.mapper.ReplicaMetricsMapper;
import org.dhorse.infrastructure.repository.po.ReplicaMetricsPO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;

@Repository
public class ReplicaMetricsRepository
		extends RightRepository<ReplicaMetricsParam, ReplicaMetricsPO, ReplicaMetrics> {

	@Autowired
	private ReplicaMetricsMapper mapper;

	public void delete(Date time) {
		LambdaUpdateWrapper<ReplicaMetricsPO> deleteWrapper = new LambdaUpdateWrapper<>();
		deleteWrapper.le(ReplicaMetricsPO::getUpdateTime, time);
		mapper.delete(deleteWrapper);
	}
	
	@Override
	protected CustomizedBaseMapper<ReplicaMetricsPO> getMapper() {
		return mapper;
	}

	@Override
	protected ReplicaMetricsPO updateCondition(ReplicaMetricsParam bizParam) {
		ReplicaMetricsPO po = new ReplicaMetricsPO();
		po.setId(bizParam.getId());
		return po;
	}

	@Override
	protected QueryWrapper<ReplicaMetricsPO> buildQueryWrapper(ReplicaMetricsParam bizParam, String orderField) {
		QueryWrapper<ReplicaMetricsPO> wrapper = super.buildQueryWrapper(bizParam, orderField);
		wrapper.ge("update_time", bizParam.getStartTime());
		wrapper.le("update_time", bizParam.getEndTime());
		return wrapper;
	}

	
}