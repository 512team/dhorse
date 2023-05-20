package org.dhorse.infrastructure.repository;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.dhorse.api.enums.MessageCodeEnum;
import org.dhorse.infrastructure.param.PageParam;
import org.dhorse.infrastructure.repository.mapper.CustomizedBaseMapper;
import org.dhorse.infrastructure.repository.po.BasePO;
import org.dhorse.infrastructure.utils.BeanUtils;
import org.dhorse.infrastructure.utils.ClassUtils;
import org.dhorse.infrastructure.utils.LogUtils;
import org.dhorse.infrastructure.utils.QueryHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public abstract class BaseRepository<P extends PageParam, E extends BasePO> {

	private static final Logger logger = LoggerFactory.getLogger(BaseRepository.class);
	
	@Autowired
	private SqlSessionFactory sqlSessionFactory;
	
	public long count(P bizParam) {
		QueryWrapper<E> queryWrapper = buildQueryWrapper(bizParam, null);
		Long count = getMapper().selectCount(queryWrapper);
		if (count == null) {
			return 0L;
		}
		return count.longValue();
	}

	public List<E> listAll() {
		P bizParam = ClassUtils.newParameterizedTypeInstance(getClass().getGenericSuperclass(), 0);
		return list(bizParam);
	}
	
	public List<E> list(P bizParam) {
		QueryWrapper<E> queryWrapper = buildQueryWrapper(bizParam, "update_time");
		return getMapper().selectList(queryWrapper);
	}
	
	public List<E> list(P bizParam, String orderField) {
		QueryWrapper<E> queryWrapper = buildQueryWrapper(bizParam, orderField);
		return getMapper().selectList(queryWrapper);
	}

	public IPage<E> page(P bizParam) {
		IPage<E> page = new Page<>(bizParam.getPageNum(), bizParam.getPageSize());
		QueryWrapper<E> queryWrapper = buildQueryWrapper(bizParam, "update_time");
		return getMapper().selectPage(page, queryWrapper);
	}
	
	public E query(P bizParam) {
		List<E> list = list(bizParam);
		if(CollectionUtils.isEmpty(list)) {
			return null;
		}
		return list.get(0);
	}

	public E queryById(String id) {
		if(id == null) {
			return null;
		}
		QueryWrapper<E> wrapper = new QueryWrapper<>();
		wrapper.eq("id", id);
		wrapper.eq("deletion_status", 0);
		return getMapper().selectOne(wrapper);
	}

	public String add(P bizParam) {
		E e = param2Entity(bizParam);
		getMapper().insert(e);
		return e.getId();
	}
	
	public void addList(List<P> bizParams) {
		List<E> es = bizParams.stream().map(b -> param2Entity(b)).collect(Collectors.toList());
		SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH, false);
		es.stream().forEach(e -> {
			getMapper().insert(e);
		});
		sqlSession.commit();
	}
	
	public boolean update(P bizParam) {
		E e = param2Entity(bizParam);
		e.setUpdateTime(new Date());
		return getMapper().update(e, buildUpdateWrapper(bizParam)) > 0 ? true : false;
	}

	public boolean updateById(P bizParam) {
		if(StringUtils.isBlank(bizParam.getId())) {
			LogUtils.throwException(logger, MessageCodeEnum.ID_IS_EMPTY);
		}
		UpdateWrapper<E> wrapper = new UpdateWrapper<>();
		wrapper.eq("id", bizParam.getId());
		wrapper.eq("deletion_status", 0);
		E e = param2Entity(bizParam);
		e.setUpdateTime(new Date());
		return getMapper().update(e, wrapper) > 0 ? true : false;
	}

	public boolean delete(String id) {
		E po = ClassUtils.newParameterizedTypeInstance(getClass().getGenericSuperclass(), 1);
		po.setId(id);
		po.setDeletionStatus(1);
		return getMapper().updateById(po) > 0 ? true : false;
	}
	
	protected void executeSql(String sql) {
		executeBatchSql(Arrays.asList(sql));
	}
	
	protected void executeBatchSql(List<String> sqls) {
		SqlSession sqlSession = sqlSessionFactory.openSession();
		try (Connection connection = sqlSession.getConnection();
				Statement statement = connection.createStatement()){
			for (String sql : sqls) {
				statement.execute(sql);
			}
		} catch (Exception e) {
			logger.error("Failed to execute batch sql", e);
		}
	}
	
	protected E param2Entity(P bizParam) {
		E po = ClassUtils.newParameterizedTypeInstance(getClass().getGenericSuperclass(), 1);
		BeanUtils.copyProperties(bizParam, po);
		return po;
	}

	protected QueryWrapper<E> buildQueryWrapper(P bizParam, String orderField) {
		return QueryHelper.buildQueryWrapper(param2Entity(bizParam), orderField);
	}

	protected UpdateWrapper<E> buildUpdateWrapper(P bizParam) {
		return QueryHelper.buildUpdateWrapper(updateCondition(bizParam));
	}

	protected void validateApp(String appId) {
		if(appId == null) {
			LogUtils.throwException(logger, MessageCodeEnum.APP_ID_IS_NULL);
		}
	}
	
	protected abstract E updateCondition(P bizParam);

	protected abstract CustomizedBaseMapper<E> getMapper();
}
