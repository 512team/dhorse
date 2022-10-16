package org.dhorse.api.param.project.env;

import org.dhorse.api.param.PageParam;

/**
 * 分页查询项目环境信息参数模型。
 * 
 * @author Dahai
 */
public class ProjectEnvPageParam extends PageParam {

	private static final long serialVersionUID = 1L;

	/**
	 * 项目编号
	 */
	private String projectId;

	/**
	 * 环境名称，如：开发、测试、预发、生产等
	 */
	private String envName;

	/**
	 * 环境标识，如：dev、qa、pl、ol等
	 */
	private String tag;

	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	public String getEnvName() {
		return envName;
	}

	public void setEnvName(String envName) {
		this.envName = envName;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}
}