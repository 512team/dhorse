package org.dhorse.infrastructure.strategy.repo;

import java.util.List;

import org.dhorse.api.response.PageData;
import org.dhorse.api.vo.AppBranch;
import org.dhorse.api.vo.AppTag;
import org.dhorse.api.vo.GlobalConfigAgg.CodeRepo;
import org.dhorse.infrastructure.strategy.repo.param.BranchListParam;
import org.dhorse.infrastructure.strategy.repo.param.BranchPageParam;
import org.dhorse.infrastructure.utils.DeployContext;

public class GitHubCodeRepoStrategy extends CodeRepoStrategy {

	@Override
	public boolean doDownloadCode(DeployContext context) {
		return true;
	}

	@Override
	public void mergeBranch(DeployContext context) {

	}

	@Override
	public void createBranch(CodeRepo codeRepo, String codeRepoPath, String branchName, String orgBranchName) {

	}

	@Override
	public void deleteBranch(CodeRepo codeRepo, String codeRepoPath, String branchName) {

	}

	@Override
	public PageData<AppBranch> branchPage(CodeRepo codeRepo, BranchPageParam param) {
		return null;
	}

	@Override
	public List<AppBranch> branchList(CodeRepo codeRepo, BranchListParam param) {
		return null;
	}

	@Override
	public PageData<AppTag> tagPage(CodeRepo codeRepo, BranchPageParam param) {
		return null;
	}

	@Override
	public void createTag(CodeRepo codeRepo, String codeRepoPath, String tagName, String branchName) {
		
	}

	@Override
	public void deleteTag(CodeRepo codeRepo, String codeRepoPath, String branchName) {
		
	}

	/**
	 * 由于github对接口访问有严格的速率限制，导致无法使用该功能
	 * @param codeRepo
	 * @return
	 * @throws IOException
	 * 
	 * 依赖：
	 * <dependency>
	 *	<groupId>org.kohsuke</groupId>
	 *	<artifactId>github-api</artifactId>
	 *	</dependency>
	 */
//	public GitHub gitHubApi(CodeRepo codeRepo) throws IOException {
//		GitHub github = new GitHubBuilder().withPassword("username", "password").build();
//		GHTree tree = github.getRepository("owner/repo").getTreeRecursive("master", 1);
//		List<GHTreeEntry> entrys = tree.getTree();
//		for(GHTreeEntry entry : entrys) {
//			byte[] buffer = new byte[1024 * 1024];
//			int length = 0;
//			File file = new File("e:/" + entry.getPath());
//			if ("blob".equals(entry.getType())) {
//				if (!file.exists()) {
//					file.createNewFile();
//				}
//				try(InputStream inputStream = entry.readAsBlob();
//						FileOutputStream outStream = new FileOutputStream(file)){
//					while ((length = inputStream.read(buffer)) != -1) {
//						outStream.write(buffer, 0, length);
//					}
//				}
//			} else if ("tree".equals(entry.getType()) && !file.exists()) {
//				file.mkdir();
//			}
//		}
//		
//		return github;
//	}
}
