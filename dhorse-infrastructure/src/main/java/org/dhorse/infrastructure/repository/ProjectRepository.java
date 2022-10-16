package org.dhorse.infrastructure.repository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.dhorse.api.enums.LanguageTypeEnum;
import org.dhorse.api.enums.MessageCodeEnum;
import org.dhorse.api.enums.RoleTypeEnum;
import org.dhorse.api.enums.YesOrNoEnum;
import org.dhorse.api.result.PageData;
import org.dhorse.api.vo.Project;
import org.dhorse.api.vo.ProjectExtendJava;
import org.dhorse.infrastructure.param.ProjectExtendJavaParam;
import org.dhorse.infrastructure.param.ProjectParam;
import org.dhorse.infrastructure.param.ProjectMemberParam;
import org.dhorse.infrastructure.repository.mapper.CustomizedBaseMapper;
import org.dhorse.infrastructure.repository.mapper.ProjectMapper;
import org.dhorse.infrastructure.repository.po.ProjectExtendJavaPO;
import org.dhorse.infrastructure.repository.po.ProjectPO;
import org.dhorse.infrastructure.repository.po.ProjectMemberPO;
import org.dhorse.infrastructure.strategy.login.dto.LoginUser;
import org.dhorse.infrastructure.utils.BeanUtils;
import org.dhorse.infrastructure.utils.Constants;
import org.dhorse.infrastructure.utils.LogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import com.baomidou.mybatisplus.core.metadata.IPage;

@Repository
public class ProjectRepository extends BaseRepository<ProjectParam, ProjectPO> {

	private static final Logger logger = LoggerFactory.getLogger(ProjectRepository.class);

	@Autowired
	private ProjectMapper projectMapper;

	@Autowired
	private ProjectExtendJavaRepository projectExtendJavaRepository;

	@Autowired
	private ProjectMemberRepository projectMemberRepository;

	public PageData<Project> page(LoginUser loginUser, ProjectParam bizParam) {
		// 如果admin角色的用户，直接查询项目表
		if (RoleTypeEnum.ADMIN.getCode().equals(loginUser.getRoleType())) {
			PageData<Project> pageDto = pageData(super.page(bizParam));
			pageDto.getItems().forEach(e -> {
				e.setModifyRights(YesOrNoEnum.YES.getCode());
				e.setDeleteRights(YesOrNoEnum.YES.getCode());
			});
			return pageDto;
		}
		// 如果是普通用户，先查询项目成员表，再查询项目表
		ProjectMemberParam projectMemberParam = new ProjectMemberParam();
		projectMemberParam.setUserId(loginUser.getId());
		projectMemberParam.setPageNum(bizParam.getPageNum());
		projectMemberParam.setPageSize(bizParam.getPageSize());
		IPage<ProjectMemberPO> pageData = projectMemberRepository.page(projectMemberParam);
		if (CollectionUtils.isEmpty(pageData.getRecords())) {
			return pageData(bizParam);
		}
		Map<String, ProjectMemberPO> projectMemberMap = pageData.getRecords().stream().collect(Collectors.toMap(ProjectMemberPO::getProjectId, e -> e));
		bizParam.setIds(new ArrayList<>(projectMemberMap.keySet()));
		PageData<Project> pageDto = pageData(super.page(bizParam));
		//只有项目管理员才有修改（删除）权限
		for(Project project : pageDto.getItems()) {
			ProjectMemberPO projectUser = projectMemberMap.get(project.getId());
			if(projectUser.getLoginName().equals(loginUser.getLoginName())){
				String[] roleTypes = projectUser.getRoleType().split(",");
				Set<Integer> roleSet = new HashSet<>();
				for (String role : roleTypes) {
					roleSet.add(Integer.valueOf(role));
				}
				List<Integer> adminRole = Constants.ROLE_OF_OPERATE_PROJECT_USER.stream()
						.filter(item -> roleSet.contains(item))
						.collect(Collectors.toList());
				if(adminRole.size() > 0) {
					project.setModifyRights(YesOrNoEnum.YES.getCode());
					project.setDeleteRights(YesOrNoEnum.YES.getCode());
				}
			}

		}
		return pageDto;
	}

	public Project query(LoginUser loginUser, ProjectParam bizParam) {
		if(bizParam.getId() == null) {
			return null;
		}
		if (RoleTypeEnum.ADMIN.getCode().equals(loginUser.getRoleType())) {
			return po2Dto(super.query(bizParam));
		}
		ProjectMemberPO projectMember = projectMemberRepository
				.queryByLoginNameAndProjectId(loginUser.getLoginName(), bizParam.getId());
		if (projectMember == null) {
			return null;
		}
		return po2Dto(super.query(bizParam));
	}

	public Project queryWithExtendById(String id) {
		Project project = po2Dto(super.queryById(id));
		queryProjectExtend(project);
		return project;
	}

	public Project queryWithExtendById(LoginUser loginUser, String id) {
		ProjectParam projectParam = new ProjectParam();
		projectParam.setId(id);
		Project project = query(loginUser, projectParam);
		queryProjectExtend(project);
		return project;
	}

	public ProjectPO queryByProjectName(String projectName) {
		ProjectParam projectInfoParam = new ProjectParam();
		projectInfoParam.setProjectName(projectName);
		return super.query(projectInfoParam);
	}

	private void queryProjectExtend(Project project) {
		if(project == null) {
			return;
		}
		if (LanguageTypeEnum.JAVA.getCode().equals(project.getLanguageType())) {
			ProjectExtendJavaParam projectJavaInfoParam = new ProjectExtendJavaParam();
			projectJavaInfoParam.setProjectId(project.getId());
			ProjectExtendJavaPO projectExtendJavaPO = projectExtendJavaRepository.query(projectJavaInfoParam);
			ProjectExtendJava projectExtendJava = new ProjectExtendJava();
			BeanUtils.copyProperties(projectExtendJavaPO, projectExtendJava);
			project.setProjectExtend(projectExtendJava);
		}
	}

	public boolean update(LoginUser loginUser, ProjectParam bizParam) {
		if (!hasOperatingRights(loginUser, bizParam)) {
			LogUtils.throwException(logger, MessageCodeEnum.NO_ACCESS_RIGHT);
		}
		return super.updateById(bizParam);
	}

	public boolean delete(LoginUser loginUser, ProjectParam bizParam) {
		if (!hasOperatingRights(loginUser, bizParam)) {
			LogUtils.throwException(logger, MessageCodeEnum.NO_ACCESS_RIGHT);
		}
		return super.delete(bizParam.getId());
	}

	protected boolean hasOperatingRights(LoginUser loginUser, ProjectParam bizParam) {
		validateProject(bizParam.getId());
		ProjectPO e = queryById(bizParam.getId());
		if (e == null || !e.getId().equals(bizParam.getId())) {
			LogUtils.throwException(logger, MessageCodeEnum.RECORD_IS_INEXISTENCE);
		}

		if (RoleTypeEnum.ADMIN.getCode().equals(loginUser.getRoleType())) {
			return true;
		}
		ProjectMemberPO projectUser = projectMemberRepository
				.queryByLoginNameAndProjectId(loginUser.getLoginName(), bizParam.getId());
		if (projectUser == null || Objects.isNull(projectUser.getRoleType())) {
			return false;
		}
		if (!projectUser.getLoginName().equals(loginUser.getLoginName())) {
			return false;
		}
		String[] roleTypes = projectUser.getRoleType().split(",");
		Set<Integer> roleSet = new HashSet<>();
		for (String role : roleTypes) {
			roleSet.add(Integer.valueOf(role));
		}
		List<Integer> adminRole = Constants.ROLE_OF_OPERATE_PROJECT_USER.stream().filter(item -> roleSet.contains(item))
				.collect(Collectors.toList());
		return adminRole.size() > 0;
	}

	protected PageData<Project> pageData(IPage<ProjectPO> pageEntity) {
		PageData<Project> pageData = new PageData<>();
		pageData.setPageNum((int) pageEntity.getCurrent());
		pageData.setPageCount((int) pageEntity.getPages());
		pageData.setPageSize((int) pageEntity.getSize());
		pageData.setItemCount((int) pageEntity.getTotal());
		pageData.setItems(pos2Dtos(pageEntity.getRecords()));
		return pageData;
	}

	protected List<Project> pos2Dtos(List<ProjectPO> pos) {
		return pos.stream().map(e -> po2Dto(e)).collect(Collectors.toList());
	}

	protected Project po2Dto(ProjectPO e) {
		if (e == null) {
			return null;
		}
		Project dto = new Project();
		BeanUtils.copyProperties(e, dto);
		return dto;
	}

	protected PageData<Project> pageData(ProjectParam bizParam) {
		PageData<Project> pageData = new PageData<>();
		pageData.setPageNum(1);
		pageData.setPageCount(0);
		pageData.setPageSize(bizParam.getPageSize());
		pageData.setItemCount(0);
		pageData.setItems(null);
		return pageData;
	}

	@Override
	protected CustomizedBaseMapper<ProjectPO> getMapper() {
		return projectMapper;
	}

	@Override
	protected ProjectPO updateCondition(ProjectParam bizParam) {
		ProjectPO po = new ProjectPO();
		po.setId(bizParam.getId());
		return po;
	}

}
