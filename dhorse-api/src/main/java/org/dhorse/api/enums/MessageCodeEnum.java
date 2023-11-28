package org.dhorse.api.enums;

public enum MessageCodeEnum {
	
	SUCESS("000000", "成功"),
	
	INVALID_PARAM("200000", "非法的参数"),
	FAILURE("100000", "操作失败，请查看详细日志信息"),
	AUTH_FAILURE("200000", "认证失败"),
	COPY_FILE_FAILURE("200000", "复制文件失败"),
	HTT_GET_FAILURE("200000", "Get请求失败"),
	HTT_POST_FAILURE("200000", "Post请求失败"),
	QUERY_FAILURE("200000", "查询失败"),
	CREATE_FAILURE("200000", "创建失败"),
	DELETE_FAILURE("200000", "删除失败"),
	DOWNLOAD_FAILURE("200000", "下载文件失败"),
	COMPRESSION_FILE_FAILURE("200000", "压缩文件失败"),
	DECOMPRESSION_FILE_FAILURE("200000", "解压文件失败"),
	
	SYS_USER_NOT_LOGINED("300000", "用户未登录"),
	NO_ACCESS_RIGHT("200000", "无权限"),
	REQUEST_WECHAT_FAILURE("200000", "请求微信失败"),
	REQUEST_DINGDING_FAILURE("200000", "请求钉钉失败"),
	RECORD_IS_INEXISTENCE("200000", "记录不存在"),
	USER_LOGIN_FAILED("200000", "登录名或密码不正确"),
	USER_PASSWORD_FAILED("200000", "登录密码不正确"),
	LOGIN_NAME_IS_EMPTY("200000", "登录名不能为空"),
	USER_NAME_IS_EMPTY("200000", "用户名不能为空"),
	PASSWORD_IS_EMPTY("200000", "密码不能为空"),
	OLD_PASSWORD_IS_EMPTY("200000", "旧密码不能为空"),
	OLD_PASSWORD_FAILED("200000", "旧密码不正确"),
	CONFIRM_PASSWORD_IS_EMPTY("200000", "确认密码不能为空"),
	PASSWORD_NOT_SAME("200000", "密码和确认密码不一致"),
	ROLE_TYPE_IS_EMPTY("200000", "角色类型不能为空"),
	INVALID_USER_ROLE("200000", "角色类型不合法"),
	SYS_USER_IS_INEXISTENCE("200000", "用户不存在"),
	INVALID_LOGINED_TOKEN("200000", "无效的登录token"),
	LOGIN_USER_IS_EXISTENCE("200000", "登录名已经存在"),
	PAGE_NUM_IS_EMPTY("200000", "页码不能为空"),
	LOGIN_SOURCE_IS_EMPTY("200000", "登录来源不能为空"),
	USER_ID_IS_EMPTY("200000", "用户编号不能为空"),
	INIT_LDAP_FAILURE("200000", "初始化Ldap失败"),
	LDAP_CONFIG_FAILURE("200000", "请首先进行Ldap配置"),
	MK_DIR_FAILURE("200000", "创建目录失败"),
	
	FILE_BEAT_K8S_FILE_INEXISTENCE("200000", "filebeat-k8s文件不存在"),
	
	//服务器集群配置
	AUTH_TYPE_IS_EMPTY("200000", "认证方式不能为空"),
	CLUSER_NAME_EXISTENCE("200000", "集群名称已经存在"),
	IMAGE_REPO_AUTH_FAILURE("200000", "创建镜像仓库认证key失败，请检查集群管理配置"),
	DHORSE_SERVER_URL_FAILURE("200000", "创建DHorse服务器地址失败，请重新进行该操作"),
	CLUSER_ID_IS_EMPTY("200000", "集群编号不能为空"),
	CLUSER_NAME_IS_EMPTY("200000", "集群名称不能为空"),
	CLUSER_URL_IS_EMPTY("200000", "集群地址不能为空"),
	AUTH_TOKEN_IS_EMPTY("200000", "集群认证令牌不能为空"),
	CLUSTER_AUTHP_ASSWORD_IS_EMPTY("200000", "集群认证名称和密码不能为空"),
	NAMESPACE_NAME_EMPTY("200000", "命名空间名称不能为空"),
	CLUSER_EXISTENCE("200000", "集群不存在"),
	TAGE_EXISTENCE("200000", "标签名称已经存在"),
	CLUSER_NAMESPACE_ID_IS_EMPTY("200000", "集群命名空间编号不能为空"),
	CLUSER_NAMESPACE_IS_EMPTY("200000", "集群命名空间不能为空"),
	CONNECT_CLUSTER_FAILURE("200000", "连接集群失败"),
	CLUSTER_FAILURE("200000", "集群错误"),
	CLUSTER_NAMESPACE_FAILURE("200000", "获取命名空间列表出错"),
	CLUSTER_DEPLOYMENT_FAILURE("200000", "获取集群deployment出错"),
	ADD_NAMESPACE_FAILURE("200000", "添加命名空间出错"),
	DELETE_NAMESPACE_FAILURE("200000", "删除命名空间出错"),
	NAMESPACE_EXISTENCE("200000", "命名空间已经存在"),
	NAMESPACE_INEXISTENCE("200000", "命名空间不存在"),
	NAMESPACE_NOT_ALLOWED_DELETION("200000", "该命名空间不允许删除"),
	
	//系统配置
	TEMPLATE_NAME_IS_EMPTY("200000", "模板名称不能为空"),
	SERVICE_URL_IS_EMPTY("200000", "服务地址不能为空"),
	AGENT_TECH_TYPE_IS_EMPTY("200000", "Agent技术类型不能为空"),
	
	//应用
	APP_ID_IS_NULL("200000", "应用编号不能为空"),
	APP_IS_INEXISTENCE("200000", "应用不存在"),
	APP_NAME_EXISTENCE("200000", "应用名称已经存在"),
	APP_NAME_IS_EMPTY("200000", "应用名称不能为空"),
	CODE_REPO_PATH_IS_EMPTY("200000", "代码仓库路径不能为空"),
	TECH_TYPE_IS_EMPTY("200000", "技术类型不能为空"),
	PACKAGE_TARGET_PATH_IS_EMPTY("200000", "打包路径不能为空"),
	PACKAGE_BUILD_TYPE_IS_EMPTY("200000", "构建方式不能为空"),
	PACKAGE_FILE_TYPE_IS_EMPTY("200000", "文件类型不能为空"),
	APP_ENV_DELETED("200000", "请先删除关联的环境"),
	APP_USER_IS_EXISTENCE("200000", "应用成员已经存在"),
	
	//环境
	APP_ENV_ID_IS_EMPTY("200000", "环境编号不能为空"),
	APP_ENV_NAME_IS_EMPTY("200000", "环境名称不能为空"),
	APP_ENV_TAG_IS_EMPTY("200000", "环境标识不能为空"),
	APP_ENV_TAG_INEXISTENCE("200000", "环境标识已经存在"),
	APP_ENV_INEXISTENCE("200000", "环境不存在"),
	ID_IS_EMPTY("200000", "编号不能为空"),
	APP_ENV_TRACE_STATUS_IS_EMPTY("200000", "链路追踪状态不能为空"),
	APP_ENV_TRACE_IMAGE_IS_EMPTY("200000", "链路追踪镜像不能为空"),
	TRACE_TEMPLATE_IS_EMPTY("200000", "链路模板不存在"),
	WAR_APP_SERVICE_PORT_8080("200000", "构建文件类型是War时，服务端口必须是8080"),
	
	//代码仓库
	CODE_REPO_IS_EMPTY("200000", "请先完成代码仓库配置"),
	
	//镜像仓库
	IMAGE_REPO_PROJECT_FAILURE("200000", "创建镜像仓库项目失败"),
	IMAGE_REPO_IS_EMPTY("200000", "请先完成镜像仓库配置"),
	SSL_CLIENT_FAILURE("200000", "创建https客户端失败"),
	PACK_FAILURE("200000", "打包失败"),
	JAVA_HOME_IS_EMPTY("200000", "Java安装目录不能为空"),
	JAVA_VERSION_IS_EMPTY("200000", "Java版本不能为空"),
	BUILD_IMAGE("200000", "构建镜像失败"),
	
	//代码分支
	APP_BRANCH_PAGE_FAILURE("200000", "获取分支列表失败"),
	APP_BRANCH_FAILURE("200000", "获取分支失败"),
	APP_BRANCH_NAME_INEXISTENCE("200000", "分支名称已经存在"),
	APP_BRANCH_ID_IS_EMPTY("200000", "分支编号不能为空"),
	APP_BRANCH_NAME_IS_EMPTY("200000", "分支名称不能为空"),
	BRANCH_DEPLOYED_DETAIL_ID_IS_EMPTY("200000", "明细编号不能为空"),
	DEPLOYED_STATUS_NOT_ROLLBACK("200000", "当前部署状态不能回滚"),
	DEPLOYMENT_DELETED_FAILURE("200000", "删除部署失败"),
	UNFINISHED_MERGE_REQUEST_EXISTS("200000", "合并失败，该应用存在未完成的合并请求，请手工合并"),
	MERGE_BRANCH("200000", "合并分支失败，请手工合并"),
	CREATE_BRANCH_FAILURE("200000", "创建分支失败"),
	DELETE_BRANCH_FAILURE("200000", "删除分支失败"),
	DOWNLOAD_BRANCH("200000", "下载分支代码失败"),
	APP_TAG_PAGE_FAILURE("200000", "获取标签列表失败"),
	APP_TAG_NAME_IS_EMPTY("200000", "标签名称不能为空"),
	
	//副本
	REQUIRED_REPLICA_NAME("200000", "副本名称不能为空"),
	REPLICA_LOG_FAILED("200000", "获取副本日志失败"),
	REPLICA_LIST_FAILURE("200000", "获取副本列表失败"),
	REPLICA_RESTARTED_FAILURE("200000", "启动副本失败"),
	DELETE_AUTO_SCALING_FAILURE("200000", "删除自动扩容失败"),
	REPLICA_NAME_INVALIDE("200000", "无效的副本名称"),
	DOWNLOAD_FILE_FAILURE("200000", "下载文件失败"),
	RENAME_FILE_FAILURE("200000", "重命名文件失败"),
	
	//部署
	INIT_GLOBAL_CONFIG("200000", "未进行全局配置"),
	SERVER_ERROR("200000", "系统错误，请查看详细日志"),
	CONFIG_IS_USING("200000", "该配置具有使用者，不允许删除"),
	ENV_DEPLOYING("200000", "环境正在部署中，请查看部署历史"),
	VERSION_IS_BUILDING("200000", "上个版本正在构建中，请稍后重试"),
	DEPLOYEMENT_VERSION_ID_IS_EMPTY("200000", "版本编号不能为空"),
	ADD_PULISHED_DETAIL("200000", "新增部署记录失败"),
	UPDATE_PULISHED_DETAIL("200000", "修改部署记录失败"),
	APPROVE("800000", "提交成功，等待审核"),
	REQUIRED_APPROVER("200000", "审批人不能为空"),
	DEPLOYING_BRANCH_IS_APPROVED("200000", "不是待审核状态"),
	DEPLOYING_BRANCH_INEXISTENCE("200000", "部署记录不存在"),
	CREATE_PART_POD("200000", "部署失败，请查看详细日志"),
	CHECK_REPLICA_TIMEOUT("200000", "副本没有完全启动"),
	CODE_REPO_FAILED("200000", "请求代码仓库失败"),
	TRACE_SERVER_IS_EMPTY("200000", "链路追踪服务地址不能为空"),
	DEPLOY("200000", "部署失败"),
	NOT_ALLOW_ABORT("200000", "该状态不允许终止"),
	;

	private String code;

	private String message;

	private MessageCodeEnum(String code, String message) {
		this.code = code;
		this.message = message;
	}

	public String getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}
}
