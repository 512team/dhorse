package org.dhorse.infrastructure.strategy.cluster;

import java.io.InputStream;
import java.util.List;

import org.dhorse.api.param.app.env.replica.EnvReplicaPageParam;
import org.dhorse.api.param.cluster.namespace.ClusterNamespacePageParam;
import org.dhorse.api.response.PageData;
import org.dhorse.api.response.model.AppEnv;
import org.dhorse.api.response.model.ClusterNamespace;
import org.dhorse.api.response.model.EnvReplica;
import org.dhorse.api.response.model.GlobalConfigAgg.ImageRepo;
import org.dhorse.infrastructure.repository.po.AppEnvPO;
import org.dhorse.infrastructure.repository.po.AppPO;
import org.dhorse.infrastructure.repository.po.ClusterPO;
import org.dhorse.infrastructure.strategy.cluster.model.Replica;
import org.dhorse.infrastructure.utils.DeploymentContext;

import io.kubernetes.client.custom.PodMetricsList;

public interface ClusterStrategy {

	Void createSecret(ClusterPO clusterPO, ImageRepo imageRepo);
	
	Replica readDeployment(ClusterPO clusterPO, AppEnv appEnv, AppPO appPO);
	
	boolean createDeployment(DeploymentContext context);

	PageData<EnvReplica> replicaPage(EnvReplicaPageParam pageParam, ClusterPO clusterPO,
			AppPO appPO, AppEnvPO appEnvPO);

	boolean rebuildReplica(ClusterPO clusterPO, String replicaName, String namespace);

	InputStream streamPodLog(ClusterPO clusterPO, String replicaName, String namespace);
	
	String podLog(ClusterPO clusterPO, String replicaName, String namespace);

	boolean autoScaling(AppPO appPO, AppEnvPO appEnvPO, ClusterPO clusterPO);

	void openLogCollector(ClusterPO clusterPO);

	void closeLogCollector(ClusterPO clusterPO);

	boolean logSwitchStatus(ClusterPO clusterPO);

	boolean deleteDeployment(ClusterPO clusterPO, AppPO appPO, AppEnvPO appEnvPO);

	List<String> queryFiles(ClusterPO clusterPO, String replicaName, String namespace);

	InputStream downloadFile(ClusterPO clusterPO, String namespace, String replicaName, String fileName);
	
	String getClusterVersion(String clusterUrl, String authToken);
	
	List<ClusterNamespace> namespaceList(ClusterPO clusterPO, ClusterNamespacePageParam clusterNamespacePageParam);
	
	boolean addNamespace(ClusterPO clusterPO, String namespaceName);
	
	boolean deleteNamespace(ClusterPO clusterPO, String namespaceName);
	
	PodMetricsList replicaMetrics(ClusterPO clusterPO, String namespace);
	
	Void createDHorseConfig(ClusterPO clusterPO);
	
	Void deleteDHorseConfig(ClusterPO clusterPO);

}
