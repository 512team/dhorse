package org.dhorse.infrastructure.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("deprecation")
public class FileUtils extends org.apache.commons.io.FileUtils {

	private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);

	public static void decompressTarGz(String sourceTarGzFile, String targetDir) {
		decompressTarGz(new File(sourceTarGzFile), new File(targetDir));
	}

	public static void decompressTarGz(File sourceTarGzFile, File targetDir) {
		try (TarArchiveInputStream tarArchiveInputStream = new TarArchiveInputStream(
				new GzipCompressorInputStream(Files.newInputStream(sourceTarGzFile.toPath())))) {
			TarArchiveEntry entry;
			// 将 tar 文件解压到 targetDir 目录下
			// 将 tar.gz文件解压成tar包,然后读取tar包里的文件元组，复制文件到指定目录
			while ((entry = tarArchiveInputStream.getNextTarEntry()) != null) {
				if (entry.isDirectory()) {
					continue;
				}
				File targetFile = new File(targetDir, entry.getName());
				File parent = targetFile.getParentFile();
				if (!parent.exists()) {
					parent.mkdirs();
				}
				// 将文件写出到解压的目录
				IOUtils.copy(tarArchiveInputStream, Files.newOutputStream(targetFile.toPath()));
			}
		} catch (Exception e) {
			logger.error("Failed to decompress file", e);
		}
	}

	public static void downloadFile(String fileUrl, File targetFile) {
		logger.info("Start to download file from {}", fileUrl);
		
		URLConnection conn = null;
		try {
			conn = new URL(fileUrl).openConnection();
		} catch (Exception e) {
			logger.error("Invalid file url", e);
		}
		
		conn.setConnectTimeout(5 * 1000);
		conn.setReadTimeout(10 * 60 * 1000);
		try (InputStream inStream = conn.getInputStream();
				FileOutputStream fs = new FileOutputStream(targetFile)){
			byte[] buffer = new byte[1024 * 1024];
			int length = 0;
			while ((length = inStream.read(buffer)) != -1) {
				fs.write(buffer, 0, length);
			}
			fs.flush();
		} catch (Exception e) {
			logger.error("Failed to download file", e);
		}
		logger.info("End to download file from {}", fileUrl);
	}
}
