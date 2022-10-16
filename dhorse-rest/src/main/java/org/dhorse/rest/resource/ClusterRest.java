package org.dhorse.rest.resource;

import java.util.List;

import org.dhorse.api.param.cluster.ClusterCreationParam;
import org.dhorse.api.param.cluster.ClusterSearchParam;
import org.dhorse.api.param.cluster.ClusterDeletionParam;
import org.dhorse.api.param.cluster.LogSwitchParam;
import org.dhorse.api.param.cluster.ClusterPageParam;
import org.dhorse.api.param.cluster.ClusterQueryParam;
import org.dhorse.api.param.cluster.ClusterUpdateParam;
import org.dhorse.api.result.PageData;
import org.dhorse.api.result.RestResponse;
import org.dhorse.api.vo.LogCollectorStatus;
import org.dhorse.api.vo.Cluster;
import org.dhorse.application.service.ClusterApplicationService;
import org.dhorse.infrastructure.annotation.AccessOnlyAdmin;
import org.dhorse.infrastructure.exception.ApplicationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 
 * 集群
 * 
 * @author Dahai
 */
@RestController
@RequestMapping("/cluster")
public class ClusterRest extends AbstractRest {

	@Autowired
	private ClusterApplicationService clusterApplicationService;
	
	/**
	 * 分页查询
	 * 
	 * @param clusterPageParam 查询参数
	 * @return 符合条件的分页数据
	 */
	@AccessOnlyAdmin
	@PostMapping("/page")
	public RestResponse<PageData<Cluster>> page(@RequestBody ClusterPageParam clusterPageParam) {
		try {
			return this.success(clusterApplicationService.page(clusterPageParam));
		} catch (ApplicationException e) {
			return this.error(e);
		}
	}
	
	/**
	 * 查询
	 * 
	 * @param clusterQueryParam 查询参数
	 * @return 符合条件的数据
	 */
	@AccessOnlyAdmin
	@PostMapping("/query")
	public RestResponse<Cluster> query(@RequestBody ClusterQueryParam clusterQueryParam) {
		try {
			return this.success(clusterApplicationService.query(clusterQueryParam));
		} catch (ApplicationException e) {
			return this.error(e);
		}
	}
	
	/**
	 * 搜索集群
	 * 
	 * @param clusterSearchParam 搜索参数
	 * @return 符合条件的数据
	 */
	@PostMapping("/search")
	public RestResponse<List<Cluster>> search(@RequestBody ClusterSearchParam clusterSearchParam) {
		try {
			return this.success(clusterApplicationService.search(clusterSearchParam));
		} catch (ApplicationException e) {
			return this.error(e);
		}
	}
	
	/**
	 * 添加
	 * 
	 * @param clusterCreationParam 添加集群参数
	 * @return 无
	 */
	@AccessOnlyAdmin
	@PostMapping("/add")
	public RestResponse<Void> add(@RequestBody ClusterCreationParam clusterCreationParam) {
		try {
			return this.success(clusterApplicationService.add(clusterCreationParam));
		} catch (ApplicationException e) {
			return this.error(e);
		}
	}
	
	/**
	 * 修改
	 * 
	 * @param clusterUpdateParam 修改服务器集群参数
	 * @return 无
	 */
	@AccessOnlyAdmin
	@PostMapping("/update")
	public RestResponse<Void> update(@RequestBody ClusterUpdateParam clusterUpdateParam) {
		try {
			return this.success(clusterApplicationService.update(clusterUpdateParam));
		} catch (ApplicationException e) {
			return this.error(e);
		}
	}
	
	/**
	 * 删除
	 * 
	 * @param clusterUpdateParam 删除服务器集群参数
	 * @return 无
	 */
	@AccessOnlyAdmin
	@PostMapping("/delete")
	public RestResponse<Void> delete(@RequestBody ClusterDeletionParam clusterDeletionParam) {
		try {
			return this.success(clusterApplicationService.delete(clusterDeletionParam.getClusterId()));
		} catch (ApplicationException e) {
			return this.error(e);
		}
	}
	
	/**
	 * 开启日志收集
	 * 
	 * @param logCollectorParam 日志收集器参数
	 * @return 无
	 */
	@AccessOnlyAdmin
	@PostMapping("/logSwitch/open")
	public RestResponse<Void> openLogSwitch(@RequestBody LogSwitchParam logSwitchParam) {
		try {
			return this.success(clusterApplicationService.openLogSwitch(logSwitchParam.getClusterId()));
		} catch (ApplicationException e) {
			return this.error(e);
		}
	}
	
	/**
	 * 关闭日志收集
	 * 
	 * @param logCollectorParam 日志收集器参数
	 * @return 无
	 */
	@AccessOnlyAdmin
	@PostMapping("/logSwitch/close")
	public RestResponse<Void> closeLogSwitch(@RequestBody LogSwitchParam logSwitchParam) {
		try {
			return this.success(clusterApplicationService.closeLogSwitch(logSwitchParam.getClusterId()));
		} catch (ApplicationException e) {
			return this.error(e);
		}
	}
	
	/**
	 * 查询日志收集器状态
	 * 
	 * @param logCollectorParam 日志收集器参数
	 * @return 日志收集器状态
	 */
	@AccessOnlyAdmin
	@PostMapping("/logSwitch/status")
	public RestResponse<LogCollectorStatus> logSwitchStatus(@RequestBody LogSwitchParam logSwitchParam) {
		try {
			return this.success(clusterApplicationService.logSwitchStatus(logSwitchParam));
		} catch (ApplicationException e) {
			return this.error(e);
		}
	}
}
