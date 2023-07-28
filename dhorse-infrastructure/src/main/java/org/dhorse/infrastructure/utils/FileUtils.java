package org.dhorse.infrastructure.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("deprecation")
public class FileUtils extends org.apache.commons.io.FileUtils {

	private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);

	public static void unZip(String orgFileName, String parentPath) {
		logger.info("Start to unzip file: {}", orgFileName);
		try (ZipArchiveInputStream in = new ZipArchiveInputStream(Files
				.newInputStream(Paths.get(orgFileName)))) {
			ArchiveEntry entry;
			while ((entry = in.getNextEntry()) != null) {
				if (entry.isDirectory()) {
					continue;
				}
				File targetFile = new File(parentPath, entry.getName());
				File parent = targetFile.getParentFile();
				if (!parent.exists()) {
					parent.mkdirs();
				}
				try(OutputStream out = Files.newOutputStream(targetFile.toPath())){
					IOUtils.copy(in, out);
				}catch (Exception ex) {
                	logger.error("Failed to unzip entry", ex);
                }
			}
		} catch (Exception e) {
			logger.error("Failed to unzip", e);
		}
		logger.info("End to unzip file: {}", orgFileName);
	}
	
	public static void unTarGz(String orgFileName, String parentPath) {
		unTarGz(new File(orgFileName), new File(parentPath));
	}

	public static void unTarGz(File sourceTarGzFile, File parentPath) {
		try (TarArchiveInputStream in = new TarArchiveInputStream(
				new GzipCompressorInputStream(Files.newInputStream(sourceTarGzFile.toPath())))) {
			ArchiveEntry entry;
			while ((entry = in.getNextEntry()) != null) {
				if (entry.isDirectory()) {
					continue;
				}
				File targetFile = new File(parentPath, entry.getName());
				File parent = targetFile.getParentFile();
				if (!parent.exists()) {
					parent.mkdirs();
				}
				try(OutputStream out = Files.newOutputStream(targetFile.toPath())){
					IOUtils.copy(in, out);
				}catch (Exception ex) {
                	logger.error("Failed to unTarGz entry", ex);
                }
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
			logger.error("File url invalid", e);
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
