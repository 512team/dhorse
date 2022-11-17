package org.dhorse.infrastructure.context;

import org.dhorse.infrastructure.repository.po.AppEnvPO;
import org.dhorse.infrastructure.repository.po.AppPO;
import org.dhorse.infrastructure.repository.po.ClusterPO;

public class AppEnvClusterContext {
	
	private AppPO appPO;

	private AppEnvPO appEnvPO;

	private ClusterPO clusterPO;

	public AppPO getAppPO() {
		return appPO;
	}

	public void setAppPO(AppPO appPO) {
		this.appPO = appPO;
	}

	public AppEnvPO getAppEnvPO() {
		return appEnvPO;
	}

	public void setAppEnvPO(AppEnvPO appEnvPO) {
		this.appEnvPO = appEnvPO;
	}

	public ClusterPO getClusterPO() {
		return clusterPO;
	}

	public void setClusterPO(ClusterPO clusterPO) {
		this.clusterPO = clusterPO;
	}

}
