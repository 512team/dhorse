package org.dhorse.api.param.project.env.replica;

import java.io.Serializable;

/**
 * 副本参数模型
 * 
 * @author Dahai
 */
public class EnvReplicaParam implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 副本名称
	 */
	private String replicaName;

	public String getReplicaName() {
		return replicaName;
	}

	public void setReplicaName(String replicaName) {
		this.replicaName = replicaName;
	}

}