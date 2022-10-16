package org.dhorse.infrastructure.context;

import org.dhorse.infrastructure.repository.po.ProjectEnvPO;
import org.dhorse.infrastructure.repository.po.ProjectPO;
import org.dhorse.infrastructure.repository.po.ClusterPO;

public class ProjectEnvClusterContext {
	
	private ProjectPO projectPO;

	private ProjectEnvPO projectEnvPO;

	private ClusterPO clusterPO;

	public ProjectPO getProjectPO() {
		return projectPO;
	}

	public void setProjectPO(ProjectPO projectPO) {
		this.projectPO = projectPO;
	}

	public ProjectEnvPO getProjectEnvPO() {
		return projectEnvPO;
	}

	public void setProjectEnvPO(ProjectEnvPO projectEnvPO) {
		this.projectEnvPO = projectEnvPO;
	}

	public ClusterPO getClusterPO() {
		return clusterPO;
	}

	public void setClusterPO(ClusterPO clusterPO) {
		this.clusterPO = clusterPO;
	}

}
