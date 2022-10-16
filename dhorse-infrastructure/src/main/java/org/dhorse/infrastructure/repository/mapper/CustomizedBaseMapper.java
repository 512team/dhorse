package org.dhorse.infrastructure.repository.mapper;

import org.dhorse.infrastructure.repository.po.BasePO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

public interface CustomizedBaseMapper<T extends BasePO> extends BaseMapper<T> {

}