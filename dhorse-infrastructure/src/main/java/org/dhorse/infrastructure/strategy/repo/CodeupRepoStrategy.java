package org.dhorse.infrastructure.strategy.repo;

import java.io.File;
import java.io.FileOutputStream;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.dhorse.api.enums.CodeRepoTypeEnum;
import org.dhorse.api.enums.MessageCodeEnum;
import org.dhorse.api.response.PageData;
import org.dhorse.api.response.model.AppBranch;
import org.dhorse.api.response.model.AppTag;
import org.dhorse.api.response.model.GlobalConfigAgg.CodeRepo;
import org.dhorse.api.response.model.GlobalConfigAgg.CodeRepo.Codeup;
import org.dhorse.infrastructure.strategy.repo.param.BranchListParam;
import org.dhorse.infrastructure.strategy.repo.param.BranchPageParam;
import org.dhorse.infrastructure.utils.DateUtils;
import org.dhorse.infrastructure.utils.DeploymentContext;
import org.dhorse.infrastructure.utils.LogUtils;

import com.aliyun.auth.credentials.Credential;
import com.aliyun.auth.credentials.provider.StaticCredentialProvider;
import com.aliyun.sdk.gateway.pop.models.Response;
import com.aliyun.sdk.service.devops20210625.AsyncClient;
import com.aliyun.sdk.service.devops20210625.models.CreateBranchRequest;
import com.aliyun.sdk.service.devops20210625.models.CreateMergeRequestRequest;
import com.aliyun.sdk.service.devops20210625.models.CreateMergeRequestResponse;
import com.aliyun.sdk.service.devops20210625.models.CreateTagRequest;
import com.aliyun.sdk.service.devops20210625.models.DeleteBranchRequest;
import com.aliyun.sdk.service.devops20210625.models.DeleteTagRequest;
import com.aliyun.sdk.service.devops20210625.models.GetFileBlobsRequest;
import com.aliyun.sdk.service.devops20210625.models.GetFileBlobsResponse;
import com.aliyun.sdk.service.devops20210625.models.GetRepositoryRequest;
import com.aliyun.sdk.service.devops20210625.models.GetRepositoryResponse;
import com.aliyun.sdk.service.devops20210625.models.ListRepositoryBranchesRequest;
import com.aliyun.sdk.service.devops20210625.models.ListRepositoryBranchesResponse;
import com.aliyun.sdk.service.devops20210625.models.ListRepositoryTagsRequest;
import com.aliyun.sdk.service.devops20210625.models.ListRepositoryTagsResponse;
import com.aliyun.sdk.service.devops20210625.models.ListRepositoryTreeRequest;
import com.aliyun.sdk.service.devops20210625.models.ListRepositoryTreeResponse;
import com.aliyun.sdk.service.devops20210625.models.MergeMergeRequestRequest;
import com.aliyun.sdk.service.devops20210625.models.MergeMergeRequestResponse;

import darabonba.core.client.ClientOverrideConfiguration;

/**
 * 
 * Codeup(阿里云)代码仓库
 * 
 * @author Dahai 2024-9-21 20:14:08
 */
public class CodeupRepoStrategy extends CodeRepoStrategy {

	@Override
	@SuppressWarnings("unchecked")
	public boolean doDownloadBranch(DeploymentContext context) {
		
		logger.info("Start to download branch from Codeup");
		
		if(context.getGlobalConfigAgg().getCodeRepo() != null
				&& !CodeRepoTypeEnum.CODEUP.getValue().equals(
						context.getGlobalConfigAgg().getCodeRepo().getType())) {
			LogUtils.throwException(logger, MessageCodeEnum.CODE_REPO_IS_EMPTY);
		}
		
		String branchName = context.getBranchName();
		Codeup codeup = context.getGlobalConfigAgg().getCodeRepo().getCodeup();
		String codeRepoPath = context.getApp().getCodeRepoPath();
		String localPathOfBranch = localPathOfBranch(context);
		context.setLocalPathOfBranch(localPathOfBranch);
		
		AsyncClient client = client(codeup);
		Long repositoryId = repositoryId(codeup, codeRepoPath, client);
		try {
			ListRepositoryTreeRequest request = ListRepositoryTreeRequest.builder()
	                .organizationId(codeup.getOrganizationId())
	                .repositoryId(repositoryId)
	                .refName(branchName)
	                .type("RECURSIVE")
	                .build();
			CompletableFuture<ListRepositoryTreeResponse> future = client.listRepositoryTree(request);
			List<Map<String, Object>> fileList = (List<Map<String, Object>>)response(future).get("result");
			
			GetFileBlobsRequest fileRequest = null;
			for (Map<String, Object> tree : fileList) {
				File file = new File(localPathOfBranch + tree.get("path"));
				if ("blob".equals(tree.get("type"))) {
					if (!file.exists()) {
						if(!file.getParentFile().exists()) {
							file.getParentFile().mkdirs();
						}
						file.createNewFile();
					}
					fileRequest = GetFileBlobsRequest.builder()
			                .organizationId(codeup.getOrganizationId())
			                .repositoryId(repositoryId)
			                .filePath((String)tree.get("path"))
			                .ref(branchName)
			                .build();
					CompletableFuture<GetFileBlobsResponse> future2 = client.getFileBlobs(fileRequest);
					try(FileOutputStream out = new FileOutputStream(file)){
						Map<String, Object> result = (Map<String, Object>)response(future2).get("result");
						out.write(((String)result.get("content")).getBytes());
					}
				} else if ("tree".equals(tree.get("type")) && !file.exists()) {
					file.mkdirs();
				}
			}
		} catch (Exception e) {
			logger.error("Failed to download branch", e);
			return false;
		} finally {
			client.close();
		}
		logger.info("End to download branch from Codeup");
		
		return true;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void mergeBranch(DeploymentContext context) {
		Codeup codeup = context.getGlobalConfigAgg().getCodeRepo().getCodeup();
		String codeRepoPath = context.getApp().getCodeRepoPath();
		AsyncClient client = client(codeup);
		Long repositoryId = repositoryId(codeup, codeRepoPath, client);
		CreateMergeRequestRequest request = CreateMergeRequestRequest.builder()
				.organizationId(codeup.getOrganizationId())
				.repositoryId(repositoryId)
                .targetProjectId(repositoryId)
                .sourceProjectId(repositoryId)
                .sourceBranch(context.getBranchName())
                .targetBranch("master")
                .title("dhorse merge")
                .createFrom("WEB")
                .build();
		try {
			CompletableFuture<CreateMergeRequestResponse> future = client.createMergeRequest(request);
			Map<String, Object> response = response(future);
			Map<String, Object> result = (Map<String, Object>)response.get("result");
			MergeMergeRequestRequest request2 = MergeMergeRequestRequest.builder()
					.organizationId(codeup.getOrganizationId())
					.repositoryId(repositoryId)
	                .mergeType("no-fast-forward")
	                .localId((Long)result.get("localId"))
	                .mergeType("no-fast-forward")
	                .build();
	        CompletableFuture<MergeMergeRequestResponse> future2 = client.mergeMergeRequest(request2);
	        future2.get();
		} catch (Exception e) {
			logger.error("Failed to merge branch from Codeup", e);
		} finally {
			client.close();
		}
	}

	@Override
	public void createBranch(CodeRepo codeRepo, String codeRepoPath, String branchName, String orgBranchName) {
		Codeup codeup = codeRepo.getCodeup();
		AsyncClient client = client(codeup);
		Long repositoryId = repositoryId(codeup, codeRepoPath, client);
		try {
			CreateBranchRequest request = CreateBranchRequest.builder()
	                .repositoryId(repositoryId.toString())
	                .organizationId(codeup.getOrganizationId())
	                .branchName(branchName)
	                .ref(orgBranchName)
	                .build();
			client.createBranch(request).get();
		} catch (Exception e) {
			logger.error("Failed to create branch from Codeup", e);
		} finally {
			client.close();
		}
	}

	@Override
	public void deleteBranch(CodeRepo codeRepo, String codeRepoPath, String branchName) {
		Codeup codeup = codeRepo.getCodeup();
		AsyncClient client = client(codeup);
		Long repositoryId = repositoryId(codeup, codeRepoPath, client);
		try {
			DeleteBranchRequest request = DeleteBranchRequest.builder()
	                .repositoryId(repositoryId)
	                .organizationId(codeup.getOrganizationId())
	                .branchName(branchName)
	                .build();
			client.deleteBranch(request).get();
		} catch (Exception e) {
			logger.error("Failed to delete branch from Codeup", e);
		} finally {
			client.close();
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public PageData<AppBranch> branchPage(CodeRepo codeRepo, BranchPageParam param) {
		logger.info("Start to query branch page from Codeup");
		
		Codeup codeup = codeRepo.getCodeup();
		Map<String, Object> response = null;
		AsyncClient client = client(codeup);
		Long repositoryId = repositoryId(codeup, param.getAppIdOrPath(), client);
		try {
			ListRepositoryBranchesRequest request = ListRepositoryBranchesRequest.builder()
	                .repositoryId(repositoryId)
	                .organizationId(codeup.getOrganizationId())
	                .page(param.getPageNum().longValue())
	                .pageSize(param.getPageSize().longValue())
	                .build();
	        CompletableFuture<ListRepositoryBranchesResponse> future = client.listRepositoryBranches(request);
	        response = response(future);
		} catch (Exception e) {
			logger.error("Failed to download branch from Codeup", e);
		} finally {
			client.close();
		}
		
		PageData<AppBranch> pageData = new PageData<>();
        Long dataCount = (Long)response.get("total");
		if (dataCount == 0) {
			pageData.setPageNum(1);
			pageData.setPageCount(0);
			pageData.setPageSize(param.getPageSize());
			pageData.setItemCount(0);
			return pageData;
		}
		
		Long pageCount = dataCount / param.getPageSize();
		pageCount = dataCount % param.getPageSize() == 0 ? pageCount : pageCount + 1;
		List<Map<String, Object>> result = (List<Map<String, Object>>)response.get("result");
		pageData.setItems(result.stream().map(e ->{
			AppBranch appBranch = new AppBranch();
			appBranch.setBranchName((String)e.get("name"));
			String dateStr = (String)((Map<String, Object>)e.get("commit")).get("committedDate");
			appBranch.setUpdateTime(DateUtils.parse(dateStr));
			appBranch.setCommitMessage((String)((Map<String, Object>)e.get("commit")).get("message"));
			return appBranch;
		}).collect(Collectors.toList()));
		pageData.setPageNum(param.getPageNum());
		pageData.setPageCount(pageCount.intValue());
		pageData.setPageSize(param.getPageSize());
		pageData.setItemCount(dataCount.intValue());
		
		logger.info("End to query branch page from Codeup");
		return pageData;
	}

	@Override
	public List<AppBranch> branchList(CodeRepo codeRepo, BranchListParam param) {
		BranchPageParam pageParam = new BranchPageParam();
		pageParam.setAppIdOrPath(param.getAppIdOrPath());
		pageParam.setPageNum(1);
		pageParam.setPageSize(100);
		PageData<AppBranch> page = branchPage(codeRepo, pageParam);
		return page.getItems();
	}

	@Override
	@SuppressWarnings("unchecked")
	public PageData<AppTag> tagPage(CodeRepo codeRepo, BranchPageParam param) {
		
		logger.info("Start to query tag page from Codeup");
		
		Codeup codeup = codeRepo.getCodeup();
		Map<String, Object> response = null;
		AsyncClient client = client(codeup);
		Long repositoryId = repositoryId(codeup, param.getAppIdOrPath(), client);
		try {
			ListRepositoryTagsRequest request = ListRepositoryTagsRequest.builder()
	                .repositoryId(repositoryId)
	                .organizationId(codeup.getOrganizationId())
	                .page(param.getPageNum().longValue())
	                .pageSize(param.getPageSize().longValue())
	                .build();
	        CompletableFuture<ListRepositoryTagsResponse> future = client.listRepositoryTags(request);
	        response = response(future);
		} catch (Exception e) {
			logger.error("Failed to download tag from Codeup", e);
		} finally {
			client.close();
		}
		
		PageData<AppTag> pageData = new PageData<>();
        Long dataCount = (Long)response.get("total");
		if (dataCount == 0) {
			pageData.setPageNum(1);
			pageData.setPageCount(0);
			pageData.setPageSize(param.getPageSize());
			pageData.setItemCount(0);
			return pageData;
		}
		
		Long pageCount = dataCount / param.getPageSize();
		pageCount = dataCount % param.getPageSize() == 0 ? pageCount : pageCount + 1;
		List<Map<String, Object>> result = (List<Map<String, Object>>)response.get("result");
		pageData.setItems(result.stream().map(e ->{
			AppTag appTag = new AppTag();
			appTag.setTagName((String)e.get("name"));
			String dateStr = (String)((Map<String, Object>)e.get("commit")).get("committedDate");
			appTag.setUpdateTime(DateUtils.parse(dateStr));
			appTag.setCommitMessage((String)((Map<String, Object>)e.get("commit")).get("message"));
			return appTag;
		}).collect(Collectors.toList()));
		pageData.setPageNum(param.getPageNum());
		pageData.setPageCount(pageCount.intValue());
		pageData.setPageSize(param.getPageSize());
		pageData.setItemCount(dataCount.intValue());
		
		logger.info("End to query tag page from Codeup");
		return pageData;
	}

	@Override
	public void createTag(CodeRepo codeRepo, String codeRepoPath, String tagName, String branchName) {
		Codeup codeup = codeRepo.getCodeup();
		AsyncClient client = client(codeup);
		Long repositoryId = repositoryId(codeup, codeRepoPath, client);
		try {
			CreateTagRequest request = CreateTagRequest.builder()
	                .repositoryId(repositoryId)
	                .organizationId(codeup.getOrganizationId())
	                .tagName(tagName)
	                .ref(branchName)
	                .build();
			client.createTag(request).get();
		} catch (Exception e) {
			logger.error("Failed to create tag from Codeup", e);
		} finally {
			client.close();
		}
	}

	@Override
	public void deleteTag(CodeRepo codeRepo, String codeRepoPath, String tagName) {
		Codeup codeup = codeRepo.getCodeup();
		AsyncClient client = client(codeup);
		Long repositoryId = repositoryId(codeup, codeRepoPath, client);
		try {
			DeleteTagRequest request = DeleteTagRequest.builder()
	                .repositoryId(repositoryId)
	                .organizationId(codeup.getOrganizationId())
	                .tagName(tagName)
	                .build();
			client.deleteTag(request).get();
		} catch (Exception e) {
			logger.error("Failed to delete tag from Codeup", e);
		} finally {
			client.close();
		}
	}
	
	@SuppressWarnings("unchecked")
	private Long repositoryId(Codeup codeup, String codeRepoPath, AsyncClient client) {
		String identity = codeup.getOrganizationId() + "/" + codeRepoPath;
		GetRepositoryRequest request = GetRepositoryRequest.builder()
				.organizationId(codeup.getOrganizationId())
				.identity(identity)
				.build();
		CompletableFuture<GetRepositoryResponse> response = client.getRepository(request);
		Long repositoryId = (Long)((Map<String, Object>)response(response).get("repository")).get("id");
		return repositoryId;
	}
	
	private AsyncClient client(Codeup codeup) {
		StaticCredentialProvider provider = StaticCredentialProvider
				.create(Credential.builder()
						.accessKeyId(codeup.getAccessKey())
						.accessKeySecret(codeup.getAccessKeySecret())
						.build());
		AsyncClient client = AsyncClient.builder()
				.region("cn-hangzhou")
				.credentialsProvider(provider)
				.overrideConfiguration(
						ClientOverrideConfiguration.create()
								.setEndpointOverride("devops.cn-hangzhou.aliyuncs.com")
								.setConnectTimeout(Duration.ofSeconds(5)))
				.build();
		return client;
	}

	@SuppressWarnings("unchecked")
	private Map<String,Object> response(CompletableFuture<? extends Response> future) {
		Response resp = null;
		try {
			resp = future.get();
		} catch (Exception e) {
			logger.error("Failed to parse Codeup response", e);
		}
		Map<String, Object> map = resp.toMap();
		Map<String, Object> body = (Map<String, Object>)map.get("body");
		if((int)map.get("statusCode") != 200) {
			logger.error("Codeup response has error,"
					+ "requestId={}, errorCode={}, errorMessage={}",
					body.get("requestId"),
					body.get("errorCode"),
					body.get("errorMessage"));
		}
		return body;
	}
}