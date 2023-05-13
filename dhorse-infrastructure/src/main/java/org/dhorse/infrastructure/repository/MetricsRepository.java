package org.dhorse.infrastructure.repository;

import java.util.Date;
import java.util.List;

import org.dhorse.infrastructure.param.MetricsParam;
import org.dhorse.infrastructure.repository.mapper.CustomizedBaseMapper;
import org.dhorse.infrastructure.repository.mapper.MetricsMapper;
import org.dhorse.infrastructure.repository.po.MetricsPO;
import org.dhorse.infrastructure.utils.Constants;
import org.dhorse.infrastructure.utils.ThreadLocalUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;

@Repository
public class MetricsRepository extends BaseRepository<MetricsParam, MetricsPO> {

	@Autowired
	private MetricsMapper mapper;

	@Override
	protected CustomizedBaseMapper<MetricsPO> getMapper() {
		return mapper;
	}

	@Override
	public String add(MetricsParam bizParam) {
		tableCache(bizParam.getReplicaName());
		try {
			return super.add(bizParam);
		}finally {
			ThreadLocalUtils.DynamicTable.remove();
		}
	}

	/**
	 * 会按照replicaName存储到不同的表，保存同一批数据的replicaName必须相同
	 */
	@Override
	public void addList(List<MetricsParam> bizParams) {
		if(CollectionUtils.isEmpty(bizParams)) {
			return;
		}
		tableCache(bizParams.get(0).getReplicaName());
		try {
			super.addList(bizParams);
		}finally {
			ThreadLocalUtils.DynamicTable.remove();
		}
	}

	@Override
	public List<MetricsPO> list(MetricsParam bizParam) {
		tableCache(bizParam.getReplicaName());
		try {
			return super.list(bizParam);
		}finally {
			ThreadLocalUtils.DynamicTable.remove();
		}
	}

	public void delete(Date time) {
		LambdaUpdateWrapper<MetricsPO> deleteWrapper = new LambdaUpdateWrapper<>();
		deleteWrapper.le(MetricsPO::getUpdateTime, time);
		for(int i = 0; i < Constants.METRICS_TABLE_SIZE; i++) {
			tableCache(i);
			try {
				mapper.delete(deleteWrapper);
			}finally {
				ThreadLocalUtils.DynamicTable.remove();
			}
		}
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

	private void tableCache(String replicaName) {
		tableCache(replicaName.hashCode());
	}
	
	private void tableCache(int hashCode) {
		ThreadLocalUtils.DynamicTable.put("_" + hashCode % Constants.METRICS_TABLE_SIZE);
	}
}