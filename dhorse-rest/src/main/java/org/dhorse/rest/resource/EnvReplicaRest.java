package org.dhorse.rest.resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.dhorse.api.enums.MessageCodeEnum;
import org.dhorse.api.param.app.env.replica.DownloadFileParam;
import org.dhorse.api.param.app.env.replica.EnvReplicaPageParam;
import org.dhorse.api.param.app.env.replica.EnvReplicaParam;
import org.dhorse.api.param.app.env.replica.EnvReplicaRebuildParam;
import org.dhorse.api.param.app.env.replica.QueryFilesParam;
import org.dhorse.api.param.app.env.replica.MetricsQueryParam;
import org.dhorse.api.response.PageData;
import org.dhorse.api.response.RestResponse;
import org.dhorse.api.response.model.EnvReplica;
import org.dhorse.api.response.model.Metrics;
import org.dhorse.api.response.model.MetricsView;
import org.dhorse.application.service.EnvReplicaApplicationService;
import org.dhorse.infrastructure.annotation.AccessNotLogin;
import org.dhorse.infrastructure.utils.LogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 
 * 环境副本
 * 
 * @author Dahai
 */
@RestController
@RequestMapping("/app/env/replica")
public class EnvReplicaRest extends AbstractRest {

	private static final Logger logger = LoggerFactory.getLogger(EnvReplicaRest.class);

	@Autowired
	private EnvReplicaApplicationService envReplicaApplicationService;
	
	@Autowired
	private HttpServletResponse response;

	/**
	 * 分页查询
	 * 
	 * @param envReplicaPageParam 分页参数
	 * @return 符合条件的分页数据
	 */
	@PostMapping("/page")
	public RestResponse<PageData<EnvReplica>> page(@CookieValue(name = "login_token", required = false) String loginToken,
			@RequestBody EnvReplicaPageParam envReplicaPageParam) {
		return success(
				envReplicaApplicationService.page(queryLoginUserByToken(loginToken), envReplicaPageParam));
	}

	/**
	 * 重建
	 * 
	 * @param rebuildParam 重建参数
	 * @return 无
	 */
	@PostMapping("/rebuild")
	public RestResponse<Void> rebuild(@CookieValue(name = "login_token", required = false) String loginToken,
			@RequestBody EnvReplicaRebuildParam rebuildParam) {
		return success(envReplicaApplicationService.rebuild(queryLoginUserByToken(loginToken), rebuildParam));
	}

	/**
	 * 查询文件
	 * 
	 * @param requestParam 查询文件参数模型
	 * @return 文件列表
	 */
	@PostMapping("/queryFiles")
	public RestResponse<List<String>> queryFiles(@CookieValue(name = "login_token", required = false) String loginToken,
			@RequestBody QueryFilesParam requestParam) {
		return success(
				envReplicaApplicationService.queryFiles(queryLoginUserByToken(loginToken), requestParam));
	}

	/**
	 * 下载文件
	 * 
	 * @param downloadFileParam 下载文件参数模型
	 * @return 数据文件列表
	 */
	@GetMapping("/downloadFile")
	public Void downloadFile(@CookieValue(name = "login_token", required = false) String loginToken, DownloadFileParam downloadFileParam) {
		response.setContentType("application/octet-stream");
		response.setHeader("Content-Disposition", "attachment;filename=" + downloadFileParam.getFileName());
		int length = 0;
		byte[] buffer = new byte[10 * 1024 * 1024];
		try (OutputStream out = response.getOutputStream();
				InputStream in = envReplicaApplicationService.downloadFile(queryLoginUserByToken(loginToken),
						downloadFileParam)) {
			while ((length = in.read(buffer)) != -1) {
				out.write(buffer, 0, length);
			}
			out.flush();
		} catch (IOException e) {
			LogUtils.throwException(logger, e, MessageCodeEnum.DOWNLOAD_FILE_FAILURE);
		}
		return null;
	}
	
	/**
	 * 下载日志
	 * 
	 * @param envReplicaParam 下载日志参数模型
	 * @return 日志文件
	 */
	@GetMapping("/downloadLog")
	public Void downloadLog(@CookieValue(name = "login_token", required = false) String loginToken,
			EnvReplicaParam envReplicaParam) {
		response.setContentType("application/octet-stream");
		response.setHeader("Content-Disposition", "attachment;filename=out.log");
		String context = envReplicaApplicationService.downloadLog(queryLoginUserByToken(loginToken), envReplicaParam);
		try (OutputStream out = response.getOutputStream()) {
			out.write(context.getBytes());
			out.flush();
		} catch (IOException e) {
			LogUtils.throwException(logger, e, MessageCodeEnum.DOWNLOAD_FILE_FAILURE);
		}
		return null;
	}

	/**
	 * 下载yaml
	 *
	 * @param envReplicaParam 下载yaml参数模型
	 * @return 日志文件
	 */
	@GetMapping("/downloadYaml")
	public Void downloadYaml(@CookieValue(name = "login_token", required = false) String loginToken,
			EnvReplicaParam envReplicaParam) {
		response.setContentType("application/octet-stream");
		response.setHeader("Content-Disposition", "attachment;filename="+envReplicaParam.getReplicaName()+".yaml");
		String context = envReplicaApplicationService.downloadYaml(queryLoginUserByToken(loginToken), envReplicaParam);
		try (OutputStream out = response.getOutputStream()) {
			out.write(context.getBytes());
			out.flush();
		} catch (IOException e) {
			LogUtils.throwException(logger, e, MessageCodeEnum.DOWNLOAD_FILE_FAILURE);
		}
		return null;
	}
	
	/**
	 * 查询副本的指标数据集
	 * 
	 * @param queryParam 分页参数
	 * @return 符合条件的数据集
	 */
	@PostMapping("/metrics/list")
	public RestResponse<MetricsView> metricsList(@CookieValue(name = "login_token", required = false) String loginToken,
			@RequestBody MetricsQueryParam queryParam) {
		return success(
				envReplicaApplicationService.metrics(queryLoginUserByToken(loginToken), queryParam));
	}
	
	/**
	 * 收集指标
	 * 
	 * @param addParam 指标参数
	 * @return 无
	 */
	@AccessNotLogin
	@PostMapping("/metrics/add")
	public RestResponse<Void> metricsAdd(@RequestBody List<Metrics> addParam) {
		return success(envReplicaApplicationService.metricsAdd(addParam));
	}
}