package org.dhorse.infrastructure.strategy.cluster.k8s;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.dhorse.api.enums.MessageCodeEnum;
import org.dhorse.infrastructure.component.ComponentConstants;
import org.dhorse.infrastructure.component.SpringBeanContext;
import org.dhorse.infrastructure.repository.po.ClusterPO;
import org.dhorse.infrastructure.utils.Constants;
import org.dhorse.infrastructure.utils.HttpUtils;
import org.dhorse.infrastructure.utils.K8sUtils;
import org.dhorse.infrastructure.utils.LogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceList;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.Resource;

public class DHorseConfigHelper {
	
	private static final Logger logger = LoggerFactory.getLogger(DHorseConfigHelper.class);
	
	/**
	 * 通过ConfigMap向k8s集群写入dhorse服务器的地址，地址格式为：ip1:8100,ip2:8100
	 */
	public static void writeServerIp(ClusterPO clusterPO, KubernetesClient client) {
		NamespaceList namespaceList = client.namespaces().list();
		if(CollectionUtils.isEmpty(namespaceList.getItems())) {
			return;
		}
		
		ComponentConstants componentConstants = SpringBeanContext.getBean(ComponentConstants.class);
		for(Namespace n : namespaceList.getItems()) {
			String namespace = n.getMetadata().getName();
			if(!K8sUtils.DHORSE_NAMESPACE.equals(namespace)
					&& K8sUtils.getSystemNamspaces().contains(namespace)) {
				continue;
			}
			if(!"Active".equals(n.getStatus().getPhase())){
				continue;
			}
			
			ConfigMap configMap = dhorseConfigMap();
			Resource<ConfigMap> resource = client.configMaps().inNamespace(namespace)
					.resource(configMap);
			ConfigMap existedCP = resource.get();
			if(existedCP == null) {
				String ipPortUri = Constants.hostIp() + ":" + componentConstants.getServerPort();
				if(ipPortUri.startsWith(Constants.LOCALHOST_IP)) {
					LogUtils.throwException(logger, "Your dhorse server mast have a valid ip, not 127.0.0.1", MessageCodeEnum.DHORSE_SERVER_URL_FAILURE);
				}
				configMap.setData(Collections.singletonMap(K8sUtils.DHORSE_SERVER_URL_KEY, ipPortUri));
				resource.create();
			}else {
				Set<String> newIp = new HashSet<>();
				newIp.add(Constants.hostIp() + ":" + componentConstants.getServerPort());
				String ipStr = existedCP.getData().get(K8sUtils.DHORSE_SERVER_URL_KEY);
				if(!StringUtils.isBlank(ipStr)) {
					String[] ips = ipStr.split(",");
					for(String ip : ips) {
						if(ip.startsWith(Constants.LOCALHOST_IP)) {
							continue;
						}
						if(!HttpUtils.pingDHorseServer(ip)) {
							continue;
						}
						newIp.add(ip);
					}
				}
				configMap.setData(Collections.singletonMap(K8sUtils.DHORSE_SERVER_URL_KEY, String.join(",", newIp)));
				resource.update();
			}
		}
	}
	
	/**
	 * 通过ConfigMap向k8s集群删除dhorse服务器的地址，地址格式为：ip1:8100,ip2:8100
	 */
	public static void deleteServerIp(ClusterPO clusterPO, KubernetesClient client) {
		NamespaceList namespaceList = client.namespaces().list();
		if(CollectionUtils.isEmpty(namespaceList.getItems())) {
			return;
		}
		for(Namespace n : namespaceList.getItems()) {
			String namespace = n.getMetadata().getName();
			if(!K8sUtils.DHORSE_NAMESPACE.equals(namespace)
					&& K8sUtils.getSystemNamspaces().contains(namespace)) {
				continue;
			}
			if(!"Active".equals(n.getStatus().getPhase())){
				continue;
			}
			ConfigMap configMap = client.configMaps().inNamespace(namespace)
					.withName(K8sUtils.DHORSE_CONFIGMAP_NAME).get();
			if(configMap == null) {
				continue;
			}
			Set<String> newIp = new HashSet<>();
			String ipStr = configMap.getData().get(K8sUtils.DHORSE_SERVER_URL_KEY);
			if(!StringUtils.isBlank(ipStr)) {
				String[] ips = ipStr.split(",");
				//ip格式为：127.0.0.1:8100
				for(String ip : ips) {
					if(Constants.hostIp().equals(ip.split(":")[0])) {
						continue;
					}
					if(!HttpUtils.pingDHorseServer(ip)) {
						continue;
					}
					newIp.add(ip);
				}
			}
			configMap.setData(Collections.singletonMap(K8sUtils.DHORSE_SERVER_URL_KEY, String.join(",", newIp)));
			Resource<ConfigMap> resource = client.configMaps().inNamespace(namespace).resource(configMap);
			if(newIp.size() == 0) {
				resource.delete();
			}else {
				resource.update();
			}
		}
	}
	
	private static ConfigMap dhorseConfigMap() {
		ConfigMap configMap = new ConfigMap();
		ObjectMeta meta = new ObjectMeta();
		meta.setName(K8sUtils.DHORSE_CONFIGMAP_NAME);
		meta.setLabels(Collections.singletonMap(K8sUtils.DHORSE_LABEL_KEY, K8sUtils.DHORSE_CONFIGMAP_NAME));
		configMap.setMetadata(meta);
		return configMap;
	}
}