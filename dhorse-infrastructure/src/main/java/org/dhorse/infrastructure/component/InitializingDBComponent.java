package org.dhorse.infrastructure.component;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.dhorse.infrastructure.exception.SysException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;

@Component
public class InitializingDBComponent implements InitializingBean {

	private static final Logger logger = LoggerFactory.getLogger(InitializingDBComponent.class);

	@Autowired
	private SqlSessionFactory sqlSessionFactory;

	@Autowired
	private ComponentConstants componentConstants;

	@Override
	public void afterPropertiesSet() throws Exception {
		checkDataPath();
		File userVersionFile = userVersionFile();
		String userVersion = loadUserVersion(userVersionFile);
		List<String> sqls = parseSqlOfHighVersion(userVersion);
		if(CollectionUtils.isEmpty(sqls)) {
			return;
		}
		initSchema(sqls);
		storeUserVersion(userVersionFile);
	}
	
	private void checkDataPath() throws Exception {
		File dataPath = Paths.get(componentConstants.getDataPath()).toFile();
		if (!dataPath.exists()) {
			logger.info("Dhorse data path does not exist and create it, path={}",
					componentConstants.getDataPath());
			if (dataPath.mkdirs()) {
				logger.info("Create dhorse data path successfully");
			} else {
				throw new SysException("Failed to create dhorse data path");
			}
		} else if (!dataPath.isDirectory()) {
			throw new SysException("Dhorse data path does not exist");
		} else {
			logger.info("The path of dhorse exists and does not need to be created, path={}",
					componentConstants.getDataPath());
		}

		File versionFilePath = Paths.get(dataPath.getAbsolutePath() + File.separator + "version").toFile();
		if (!versionFilePath.exists()) {
			logger.info("Dhorse version file does not exist and create it");
			if (versionFilePath.createNewFile()) {
				logger.info("Create dhorse version file successfully");
			} else {
				throw new SysException("Failed to create dhorse version");
			}
		} else if (!versionFilePath.isFile()) {
			throw new SysException("Dhorse version file does not exist");
		} else {
			logger.info("The version file of dhorse exists and does not need to be created");
		}
	}

	private File userVersionFile() {
		return new File(componentConstants.getDataPath() + "version");
	}

	/**
	 * 
	 * 读取用户的当前版本，初始版本号是0.0.0
	 * @return 版本号
	 */
	private String loadUserVersion(File userVersionFile) throws Exception {
		if (!userVersionFile.exists()) {
			return "0.0.0";
		}
		String userVersion = null;
		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(new FileInputStream(userVersionFile), StandardCharsets.UTF_8))) {
			userVersion = reader.readLine();
		}
		if (!StringUtils.isBlank(userVersion)) {
			return userVersion;
		}
		return "0.0.0";
	}

	private List<String> parseSqlOfHighVersion(String userVersion) throws Exception {
		String sqlFilePath = "sql/h2/*";
		if(componentConstants.getMysql().isEnable()) {
			sqlFilePath = "sql/mysql/*";
		}
		Resource[] resources = new PathMatchingResourcePatternResolver()
				.getResources(ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + sqlFilePath);
		List<String> sqls = new ArrayList<>();
		for (Resource resource : resources) {
			if (resource.getFilename().compareTo(userVersion) <= 0) {
				continue;
			}
			try (BufferedReader reader = new BufferedReader(
					new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
				logger.info("Start to parse {} version db schema", resource.getFilename());
				sqls.addAll(parseSqlOfVersion(reader));
				logger.info("Parsed {} version db schema successfully", resource.getFilename());
			}
		}
		return sqls;
	}

	private List<String> parseSqlOfVersion(BufferedReader reader) throws Exception {
		List<String> sqls = new ArrayList<>();
		String line = null;
		StringBuilder oneSql = new StringBuilder();
		while ((line = reader.readLine()) != null) {
			line = line.replace("\r\n", "").replace("\t", "");
			if (StringUtils.isBlank(line)) {
				continue;
			}
			oneSql.append(line);
			if (!line.endsWith(";")) {
				continue;
			}
			oneSql.deleteCharAt(oneSql.length() - 1);
			sqls.add(oneSql.toString());
			oneSql = new StringBuilder();
		}
		return sqls;
	}

	private void initSchema(List<String> sqls) throws Exception {
		SqlSession sqlSession = sqlSessionFactory.openSession();
		Connection connection = sqlSession.getConnection();
		Statement statement = connection.createStatement();
		try {
			logger.info("Start to initialize version db schema");
			for (String sql : sqls) {
				statement.execute(sql);
			}
			logger.info("Initialized version db schema successfully");
		} catch (Exception e) {
			logger.error("Failed to init data schema", e);
			throw e;
		} finally {
			statement.close();
			connection.close();
		}
	}

	/**
	 * 
	 * 记录最新的版本号
	 */
	private void storeUserVersion(File userVersionFile) throws Exception {
		if (!userVersionFile.exists()) {
			if (userVersionFile.createNewFile()) {
				logger.info("Create version file successfully");
			} else {
				logger.info("Filed to create version file");
				throw new SysException("Filed to create version file");
			}
		}
		try (BufferedWriter writer = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(userVersionFile), StandardCharsets.UTF_8))) {
			writer.write(componentConstants.getVersion());
		}
	}
}