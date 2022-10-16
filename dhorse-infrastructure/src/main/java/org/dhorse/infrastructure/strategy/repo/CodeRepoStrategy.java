package org.dhorse.infrastructure.strategy.repo;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.dhorse.api.result.PageData;
import org.dhorse.api.vo.GlobalConfigAgg.CodeRepo;
import org.dhorse.api.vo.ProjectBranch;
import org.dhorse.infrastructure.strategy.repo.param.BranchListParam;
import org.dhorse.infrastructure.strategy.repo.param.BranchPageParam;
import org.dhorse.infrastructure.utils.DeployContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class CodeRepoStrategy {

	public static final Logger logger = LoggerFactory.getLogger(CodeRepoStrategy.class);
	
	void clearOldBranch(DeployContext context) {
		File pathFile = new File(localPathOfBranch(context));
		try {
			FileUtils.deleteDirectory(pathFile.getParentFile());
		} catch (IOException e) {
			logger.error("Failed to clear project code", e);
		}
	}
	
	public boolean downloadBranch(DeployContext context) {
		clearOldBranch(context);
		return doDownloadBranch(context);
	}
	
	abstract boolean doDownloadBranch(DeployContext context);
	
	public abstract void mergeBranch(DeployContext context);
	
	String checkLocalPathOfBranch(DeployContext context) {
		String localPathOfBranch = localPathOfBranch(context);
		File pathFile = new File(localPathOfBranch);
		if (!pathFile.exists()) {
			logger.info("The local path of branch does not exsit, and create it");
			if(pathFile.mkdirs()) {
				logger.info("Create local path of branch successfully");
			}else {
				logger.error("Failed to create local path of branch");
				return null;
			}
		}
		
		return localPathOfBranch;
	}
	
	String localPathOfBranch(DeployContext context) {
		return new StringBuilder()
				.append(context.getComponentConstants().getDataPath())
				.append(File.separator)
				.append("project")
				.append(File.separator)
				.append(context.getProject().getProjectName())
				.append(File.separator)
				.append(context.getProject().getProjectName())
				.append("-")
				.append(System.currentTimeMillis())
				.append(File.separator)
				.toString();
	}
	
	public abstract void createBranch(CodeRepo codeRepo, String codeRepoPath, String branchName);
	
	public abstract void deleteBranch(CodeRepo codeRepo, String codeRepoPath, String branchName);
	
	public abstract PageData<ProjectBranch> branchPage(CodeRepo codeRepo, BranchPageParam param);
	
	public abstract List<ProjectBranch> branchList(CodeRepo codeRepo, BranchListParam param);
}
