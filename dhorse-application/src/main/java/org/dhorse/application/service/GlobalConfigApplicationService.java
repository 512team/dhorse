package org.dhorse.application.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import javax.net.ssl.SSLContext;

import org.apache.commons.io.FileUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.dhorse.api.enums.GlobalConfigItemTypeEnum;
import org.dhorse.api.enums.ImageRepoTypeEnum;
import org.dhorse.api.enums.MessageCodeEnum;
import org.dhorse.api.param.global.GlolabConfigDeletionParam;
import org.dhorse.api.param.global.GlolabConfigPageParam;
import org.dhorse.api.result.PageData;
import org.dhorse.api.vo.GlobalConfigAgg.BaseGlobalConfig;
import org.dhorse.api.vo.GlobalConfigAgg.CodeRepo;
import org.dhorse.api.vo.GlobalConfigAgg.ImageRepo;
import org.dhorse.api.vo.GlobalConfigAgg.Ldap;
import org.dhorse.api.vo.GlobalConfigAgg.Maven;
import org.dhorse.api.vo.GlobalConfigAgg.TraceTemplate;
import org.dhorse.infrastructure.param.GlobalConfigParam;
import org.dhorse.infrastructure.repository.po.GlobalConfigPO;
import org.dhorse.infrastructure.utils.JsonUtils;
import org.dhorse.infrastructure.utils.LogUtils;
import org.dhorse.infrastructure.utils.ThreadPoolUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * 
 * 全局配置应用服务
 * 
 * @author 天地之怪
 */
@Service
public class GlobalConfigApplicationService extends DeployApplicationService {

	private static final Logger logger = LoggerFactory.getLogger(GlobalConfigApplicationService.class);

	public Void addOrUpdateMaven(Maven mavenConf) {
		mavenConf.setItemType(GlobalConfigItemTypeEnum.MAVEN.getCode());
		addOrUpdateGlobalConfig(mavenConf);
		//如果有新增或者修改maven配置，则重新拉取本地仓库文件
		ThreadPoolUtils.async(new MavenRepo(mavenConf));
		return null;
	}

	private Void addOrUpdateGlobalConfig(BaseGlobalConfig baseConfig) {
		GlobalConfigParam param = new GlobalConfigParam();
		param.setId(baseConfig.getId());
		param.setItemType(baseConfig.getItemType());
		GlobalConfigPO globalConfigPO = globalConfigRepository.query(param);
		param.setItemValue(JsonUtils.toJsonString(baseConfig, "id", "pageSize", "itemType"));
		if(globalConfigPO == null) {
			globalConfigRepository.add(param);
		}else {
			globalConfigRepository.update(param);
		}
		return null;
	}
	
	private class MavenRepo implements Callable<Void> {

		private Maven mavenConf;

		public MavenRepo(Maven mavenConf) {
			this.mavenConf = mavenConf;
		}

		@Override
		public Void call() throws Exception {
			String localPathName = componentConstants.getDataPath() + "project/tmp/";
			File localPathOfPom = new File(localPathName);
			if (!localPathOfPom.exists()) {
				localPathOfPom.mkdirs();
			}
			buildTmpProject(localPathName);
			doMavenPack(mavenConf, localPathName);
			deleteTmpProject(localPathName);
			return null;
		}

		private void buildTmpProject(String localPath) {
			Resource resource = new PathMatchingResourcePatternResolver()
					.getResource(ResourceUtils.CLASSPATH_URL_PREFIX + "maven/pom.xml");
			try (InputStream in = resource.getInputStream();
					FileOutputStream out = new FileOutputStream(localPath + "pom.xml")) {
				byte[] buffer = new byte[1024 * 1024];
				int offset = 0;
				while ((offset = in.read(buffer)) != -1) {
					out.write(buffer, 0, offset);
				}
			} catch (IOException e) {
				logger.error("Failed to build tmp project", e);
			}
		}
		
		private void deleteTmpProject(String localPath) {
			try {
				FileUtils.deleteDirectory(new File(localPath));
			} catch (IOException e) {
				logger.error("Failed to delete tmp project", e);
			}
		}
	}
	
	public Void addOrUpdateCodeRepo(CodeRepo codeRepo) {
		codeRepo.setItemType(GlobalConfigItemTypeEnum.CODEREPO.getCode());
		return addOrUpdateGlobalConfig(codeRepo);
	}
	
	public Void addOrUpdateImageRepo(ImageRepo imageRepo) {
		if(ImageRepoTypeEnum.HARBOR.getValue().equals(imageRepo.getType())) {
			createProject(imageRepo);
		}
		imageRepo.setItemType(GlobalConfigItemTypeEnum.IMAGEREPO.getCode());
		return addOrUpdateGlobalConfig(imageRepo);
	}
	
	private void createProject(ImageRepo imageRepo) {
        String uri = "api/v2.0/projects";
        if(!imageRepo.getUrl().endsWith("/")) {
        	uri = "/" + uri;
        }
        HttpPost httpPost = new HttpPost(imageRepo.getUrl() + uri);
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(5000)
                .setConnectTimeout(5000)
                .setSocketTimeout(5000)
                .build();
        httpPost.setConfig(requestConfig);
        httpPost.setHeader("Content-Type", "application/json;charset=UTF-8");
        httpPost.setHeader("Authorization", "Basic "+ Base64.getUrlEncoder().encodeToString((imageRepo.getAuthUser() + ":" + imageRepo.getAuthPassword()).getBytes()));
        ObjectNode objectNode = JsonUtils.getObjectMapper().createObjectNode();
        objectNode.put("project_name", "dhorse");
        //1：共有类型
        objectNode.put("public", 1);
        httpPost.setEntity(new StringEntity(objectNode.toString(),"UTF-8"));
        try (CloseableHttpResponse response = createHttpClient(imageRepo.getUrl()).execute(httpPost)){
            if (response.getStatusLine().getStatusCode() != 201
            		&& response.getStatusLine().getStatusCode() != 409) {
            	LogUtils.throwException(logger, response.getStatusLine().getReasonPhrase(),
            			MessageCodeEnum.IMAGE_REPO_PROJECT_FAILURE);
            }
        } catch (IOException e) {
        	LogUtils.throwException(logger, e, MessageCodeEnum.IMAGE_REPO_PROJECT_FAILURE);
        }
	}
	
	private CloseableHttpClient createHttpClient(String url) {
		if(!url.startsWith("https")) {
			return HttpClients.createDefault();
		}
		try {
			SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
				// 信任所有
				public boolean isTrusted(X509Certificate[] chain, String authType) {
					return true;
				}
			}).build();
			SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext);
			return HttpClients.custom().setSSLSocketFactory(sslsf).build();
		} catch (Exception e) {
			LogUtils.throwException(logger, e, MessageCodeEnum.SSL_CLIENT_FAILURE);
		}
		return HttpClients.createDefault();
	}
	
	public Void addOrUpdateLdap(Ldap ldap) {
		ldap.setItemType(GlobalConfigItemTypeEnum.LDAP.getCode());
		return addOrUpdateGlobalConfig(ldap);
	}
	
	public PageData<TraceTemplate> traceTemplatePage(GlolabConfigPageParam pageParam) {
		GlobalConfigParam bizParam = new GlobalConfigParam();
		bizParam.setPageNum(pageParam.getPageNum());
		bizParam.setPageSize(pageParam.getPageSize());
		bizParam.setItemType(pageParam.getItemType());
		IPage<GlobalConfigPO> pagePO = globalConfigRepository.page(bizParam);
		if(pagePO.getTotal() == 0) {
			return zeroPageData(pageParam.getPageSize());
		}
		List<TraceTemplate> resultPage = pagePO.getRecords().stream().map(e ->{
			TraceTemplate template = JsonUtils.parseToObject(e.getItemValue(), TraceTemplate.class);
			template.setId(e.getId());
			template.setItemType(e.getItemType());
			return template;
		}).collect(Collectors.toList());
		return this.pageData(pagePO, resultPage);
	}
	
	public Void addTraceTemplate(TraceTemplate taceTemplate) {
		GlobalConfigParam param = new GlobalConfigParam();
		param.setItemType(GlobalConfigItemTypeEnum.TRACE_TEMPLATE.getCode());
		param.setItemValue(JsonUtils.toJsonString(taceTemplate, "id", "pageSize", "itemType"));
		globalConfigRepository.add(param);
		return null;
	}
	
	public Void updateTraceTemplate(TraceTemplate taceTemplate) {
		if(taceTemplate.getId() == null) {
			LogUtils.throwException(logger, MessageCodeEnum.TRACE_TEMPLATE_ID_IS_EMPTY);
		}
		GlobalConfigParam param = new GlobalConfigParam();
		param.setId(taceTemplate.getId());
		param.setItemType(GlobalConfigItemTypeEnum.TRACE_TEMPLATE.getCode());
		param.setItemValue(JsonUtils.toJsonString(taceTemplate, "id", "pageSize", "itemType"));
		globalConfigRepository.update(param);
		return null;
	}
	
	public Void delete(GlolabConfigDeletionParam deleteParam) {
		globalConfigRepository.delete(deleteParam.getId());
		return null;
	}
}
