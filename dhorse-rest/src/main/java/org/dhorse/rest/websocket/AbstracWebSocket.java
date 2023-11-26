package org.dhorse.rest.websocket;

import org.dhorse.application.service.EnvReplicaApplicationService;
import org.dhorse.application.service.SysUserApplicationService;
import org.dhorse.infrastructure.component.SpringBeanContext;
import org.dhorse.infrastructure.context.AppEnvClusterContext;
import org.dhorse.infrastructure.strategy.login.dto.LoginUser;
import org.dhorse.rest.websocket.ssh.SSHContext;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;

/**
 * 
 * WebSocket的基础功能
 * 
 * @author Dahi
 */
public abstract class AbstracWebSocket {

	public SSHContext sshContext(String loginToken, String appId, String envId, String replicaName) {
		SysUserApplicationService sysUserApplicationService = SpringBeanContext
				.getBean(SysUserApplicationService.class);
		EnvReplicaApplicationService replicaApplicationService = SpringBeanContext
				.getBean(EnvReplicaApplicationService.class);
		LoginUser loginUser = sysUserApplicationService.queryLoginUserByToken(loginToken);
		AppEnvClusterContext appEnvClusterContext = replicaApplicationService
					.queryCluster(appId, envId, loginUser);
		 Config config = new ConfigBuilder()
				 .withTrustCerts(true)
				 .withMasterUrl(appEnvClusterContext.getClusterPO().getClusterUrl())
				 .withOauthToken(appEnvClusterContext.getClusterPO().getAuthToken())
				 .build();
		 
		 KubernetesClient client = new KubernetesClientBuilder()
				 .withConfig(config)
				 .build();
		 
		 SSHContext context = new SSHContext();
		 context.setClient(client);
		 context.setNamespace(appEnvClusterContext.getAppEnvPO().getNamespaceName());
		 context.setReplicaName(replicaName);
		 
		return context;
	}
	
}