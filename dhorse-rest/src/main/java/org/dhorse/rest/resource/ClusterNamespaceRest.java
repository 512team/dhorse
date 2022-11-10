package org.dhorse.rest.resource;

import org.dhorse.api.param.cluster.namespace.ClusterNamespaceCreationParam;
import org.dhorse.api.param.cluster.namespace.ClusterNamespaceDeletionParam;
import org.dhorse.api.param.cluster.namespace.ClusterNamespacePageParam;
import org.dhorse.api.result.PageData;
import org.dhorse.api.result.RestResponse;
import org.dhorse.api.vo.ClusterNamespace;
import org.dhorse.application.service.ClusterNamespaceApplicationService;
import org.dhorse.infrastructure.annotation.AccessOnlyAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 
 * 命名空间
 * 
 * @author Dahai
 */
@RestController
@RequestMapping("/cluster/namespace")
public class ClusterNamespaceRest extends AbstractRest {

	@Autowired
	private ClusterNamespaceApplicationService clusterNamespaceApplicationService;
	
	/**
	 * 分页查询
	 * 
	 * @param pageClusterNamespaceParam 查询参数
	 * @return 符合条件的分页数据
	 */
	@PostMapping("/page")
	public RestResponse<PageData<ClusterNamespace>> page(@RequestBody ClusterNamespacePageParam pageClusterNamespaceParam) {
		return this.success(clusterNamespaceApplicationService.page(pageClusterNamespaceParam));
	}
	
	/**
	 * 添加
	 * 
	 * @param clusterNamespaceCreationParam 添加集群命名空间参数
	 * @return 无
	 */
	@AccessOnlyAdmin
	@PostMapping("/add")
	public RestResponse<Void> add(@RequestBody ClusterNamespaceCreationParam clusterNamespaceCreationParam) {
		return this.success(clusterNamespaceApplicationService.add(clusterNamespaceCreationParam));
	}
	
	/**
	 * 删除
	 * 
	 * @param updateNameSpaceParam 删除服务器集群命名空间参数
	 * @return 无
	 */
	@AccessOnlyAdmin
	@PostMapping("/delete")
	public RestResponse<Void> delete(@RequestBody ClusterNamespaceDeletionParam clusterNamespaceDeletionParam) {
		return this.success(clusterNamespaceApplicationService.delete(clusterNamespaceDeletionParam));
	}
}
