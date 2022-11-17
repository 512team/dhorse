package org.dhorse.api.param.app.member;

import java.util.List;

/**
 * 添加应用成员参数模型
 * 
 * @author Dahai 2021-09-08
 */
public class AppMemberCreationParam extends AppMemberDeletionParam {

	private static final long serialVersionUID = 1L;

	/**
	 * 角色类型，1：管理员，2：开发，3：测试，4：运维，5：架构师，6：告警接收：7：部署审批
	 */
	private List<Integer> roleTypes;

	public List<Integer> getRoleTypes() {
		return roleTypes;
	}

	public void setRoleTypes(List<Integer> roleTypes) {
		this.roleTypes = roleTypes;
	}

}