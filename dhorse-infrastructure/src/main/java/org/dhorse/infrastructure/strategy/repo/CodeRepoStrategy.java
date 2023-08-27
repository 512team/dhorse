package org.dhorse.infrastructure.strategy.repo;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.dhorse.api.response.PageData;
import org.dhorse.api.response.model.AppBranch;
import org.dhorse.api.response.model.AppTag;
import org.dhorse.api.response.model.GlobalConfigAgg.CodeRepo;
import org.dhorse.infrastructure.strategy.repo.param.BranchListParam;
import org.dhorse.infrastructure.strategy.repo.param.BranchPageParam;
import org.dhorse.infrastructure.utils.DeploymentContext;
import org.dhorse.infrastructure.utils.FileUtils;
import org.dhorse.infrastructure.utils.K8sUtils;
import org.dhorse.infrastructure.utils.ThreadPoolUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class CodeRepoStrategy {

	public static final Logger logger = LoggerFactory.getLogger(CodeRepoStrategy.class);
	
	void clearHistoryBranch(DeploymentContext context) {
		File[] hisBranchs = new File(localPathOfBranch(context)).getParentFile().listFiles();
		//异步清除历史代码
		ThreadPoolUtils.async(new Runnable() {
			@Override
			public void run() {
				for(File h : hisBranchs) {
					try {
						FileUtils.deleteDirectory(h);
					} catch (IOException e) {
						logger.error("Failed to clear local branch", e);
					}
				}
			}
		});
	}
	
	public boolean downloadBranch(DeploymentContext context) {
		clearHistoryBranch(context);
		return doDownloadBranch(context);
	}
	
	abstract boolean doDownloadBranch(DeploymentContext context);
	
	public abstract void mergeBranch(DeploymentContext context);
	
	String checkLocalPathOfBranch(DeploymentContext context) {
		String localPathOfBranch = localPathOfBranch(context);
		File pathFile = new File(localPathOfBranch);
		if (!pathFile.exists()) {
			logger.info("The local path of branch does not exist, and create it");
			if(pathFile.mkdirs()) {
				logger.info("Create local path of branch successfully");
			}else {
				logger.error("Failed to create local path of branch");
				return null;
			}
		}
		
		return localPathOfBranch;
	}
	
	String localPathOfBranch(DeploymentContext context) {
		return new StringBuilder()
				.append(context.getComponentConstants().getDataPath())
				.append(K8sUtils.APP_KEY)
				.append(File.separator)
				.append(context.getApp().getAppName())
				.append(File.separator)
				.append(context.getApp().getAppName())
				.append("-")
				.append(System.currentTimeMillis())
				.append(File.separator)
				.toString();
	}
	
	public abstract void createBranch(CodeRepo codeRepo, String codeRepoPath, String branchName, String orgBranchName);
	
	public abstract void deleteBranch(CodeRepo codeRepo, String codeRepoPath, String branchName);
	
	public abstract PageData<AppBranch> branchPage(CodeRepo codeRepo, BranchPageParam param);
	
	public abstract List<AppBranch> branchList(CodeRepo codeRepo, BranchListParam param);
	
	public abstract PageData<AppTag> tagPage(CodeRepo codeRepo, BranchPageParam param);
	
	public abstract void createTag(CodeRepo codeRepo, String codeRepoPath, String tagName, String branchName);
	
	public abstract void deleteTag(CodeRepo codeRepo, String codeRepoPath, String branchName);
}
