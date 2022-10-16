package org.dhorse.api.vo;

/**
 * 集群命名空间
 * 
 * @author Dahai 2021-11-24
 */
public class ClusterNamespace extends BaseDto {

	private static final long serialVersionUID = 1L;

	/**
	 * 名称，如：default、qa、pl、ol等
	 */
	private String namespaceName;

	public String getNamespaceName() {
		return namespaceName;
	}

	public void setNamespaceName(String namespaceName) {
		this.namespaceName = namespaceName;
	}

}