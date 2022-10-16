package org.dhorse.infrastructure.repository.po;

import java.util.List;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 项目相关用户
 * 
 * @author Dahai 2021-09-08
 */
@TableName("PROJECT_MEMBER")
public class ProjectMemberPO extends BasePO {

	private static final long serialVersionUID = 1L;

	/**
	 * 项目编号
	 */
	private String projectId;

	/**
	 * 用户编号
	 */
	private String userId;

	/**
	 * 登录名
	 */
	private String loginName;

	/**
	 * 存储格式：1,2,3，角色类型，1：管理员，2：开发，3：测试，4：运维，5：架构师，6：告警接收：7：部署审批
	 */
	private String roleType;

	@TableField(exist = false)
	private List<String> projectIds;

	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getLoginName() {
		return loginName;
	}

	public void setLoginName(String loginName) {
		this.loginName = loginName;
	}

	public String getRoleType() {
		return roleType;
	}

	public void setRoleType(String roleType) {
		this.roleType = roleType;
	}

	public List<String> getProjectIds() {
		return projectIds;
	}

	public void setProjectIds(List<String> projectIds) {
		this.projectIds = projectIds;
	}

	@Override
	public List<String> getIds() {
		return this.ids;
	}

	@Override
	public String getId() {
		return this.id;
	}

	@Override
	public Integer getDeletionStatus() {
		return this.deletionStatus;
	}

}