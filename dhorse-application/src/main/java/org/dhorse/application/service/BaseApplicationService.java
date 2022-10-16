package org.dhorse.application.service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.dhorse.api.result.PageData;
import org.dhorse.infrastructure.utils.BeanUtils;
import org.dhorse.infrastructure.utils.ClassUtils;
import org.springframework.util.CollectionUtils;

import com.baomidou.mybatisplus.core.metadata.IPage;

public abstract class BaseApplicationService<D, PO> extends ApplicationService {

	protected PageData<D> zeroPageData() {
		PageData<D> pageData = new PageData<>();
		pageData.setPageNum(1);
		pageData.setPageCount(1);
		pageData.setPageSize(10);
		pageData.setItemCount(0);
		pageData.setItems(null);
		return pageData;
	}

	protected PageData<D> pageData(IPage<PO> pageEntity) {
		PageData<D> pageData = new PageData<>();
		pageData.setPageNum((int) pageEntity.getCurrent());
		pageData.setPageCount((int) pageEntity.getPages());
		pageData.setPageSize((int) pageEntity.getSize());
		pageData.setItemCount((int) pageEntity.getTotal());
		pageData.setItems(pos2Dtos(pageEntity.getRecords()));
		return pageData;
	}

	protected List<D> pos2Dtos(List<PO> pos) {
		if (CollectionUtils.isEmpty(pos)) {
			return Collections.emptyList();
		}
		return pos.stream().map(e -> po2Dto(e)).collect(Collectors.toList());
	}

	protected D po2Dto(PO po) {
		if (po == null) {
			return null;
		}
		D dto = ClassUtils.newParameterizedTypeInstance(getClass().getGenericSuperclass(), 0);
		BeanUtils.copyProperties(po, dto);
		return dto;
	}
}
