package org.dhorse.rest.resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.dhorse.api.enums.MessageCodeEnum;
import org.dhorse.api.param.project.env.replica.DownloadFileParam;
import org.dhorse.api.param.project.env.replica.EnvReplicaPageParam;
import org.dhorse.api.param.project.env.replica.EnvReplicaParam;
import org.dhorse.api.param.project.env.replica.EnvReplicaRebuildParam;
import org.dhorse.api.param.project.env.replica.QueryFilesParam;
import org.dhorse.api.result.PageData;
import org.dhorse.api.result.RestResponse;
import org.dhorse.api.vo.EnvReplica;
import org.dhorse.application.service.EnvReplicaApplicationService;
import org.dhorse.infrastructure.exception.ApplicationException;
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
@RequestMapping("/project/env/replica")
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
	public RestResponse<PageData<EnvReplica>> page(@CookieValue("login_token") String loginToken,
			@RequestBody EnvReplicaPageParam envReplicaPageParam) {
		try {
			return success(
					envReplicaApplicationService.page(queryLoginUserByToken(loginToken), envReplicaPageParam));
		} catch (ApplicationException e) {
			return this.error(e);
		}
	}

	/**
	 * 重建
	 * 
	 * @param rebuildParam 重建参数
	 * @return 无
	 */
	@PostMapping("/rebuild")
	public RestResponse<Void> rebuild(@CookieValue("login_token") String loginToken,
			@RequestBody EnvReplicaRebuildParam rebuildParam) {
		try {
			return success(envReplicaApplicationService.rebuild(queryLoginUserByToken(loginToken), rebuildParam));
		} catch (ApplicationException e) {
			return this.error(e);
		}
	}

	/**
	 * 查询文件
	 * 
	 * @param requestParam 查询文件参数模型
	 * @return 文件列表
	 */
	@PostMapping("/queryFiles")
	public RestResponse<List<String>> queryFiles(@CookieValue("login_token") String loginToken,
			@RequestBody QueryFilesParam requestParam) {
		try {
			return success(
					envReplicaApplicationService.queryFiles(queryLoginUserByToken(loginToken), requestParam));
		} catch (ApplicationException e) {
			return this.error(e);
		}
	}

	/**
	 * 下载文件
	 * 
	 * @param downloadFileParam 下载文件参数模型
	 * @return 数据文件列表
	 */
	@GetMapping("/downloadFile")
	public Void downloadFile(@CookieValue("login_token") String loginToken, DownloadFileParam downloadFileParam) {
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
	public Void downloadLog(@CookieValue("login_token") String loginToken,
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
}