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
	
//	public static void deleteDirectory(final File directory) throws IOException {
//        Objects.requireNonNull(directory, "directory");
//        if (!directory.exists()) {
//            return;
//        }
//        if (!isSymlink(directory)) {
//            cleanDirectory(directory);
//        }
//        delete(directory);
//    }
//	
//	public static boolean isSymlink(final File file) {
//        return file != null && Files.isSymbolicLink(file.toPath());
//    }
//	
//	public static void cleanDirectory(final File directory) throws IOException {
//        final File[] files = listFiles(directory, null);
//
//        final List<Exception> causeList = new ArrayList<>();
//        for (final File file : files) {
//            try {
//                forceDelete(file);
//            } catch (final IOException ioe) {
//                causeList.add(ioe);
//            }
//        }
//
//        if (!causeList.isEmpty()) {
//            throw new IOExceptionList(directory.toString(), causeList);
//        }
//    }
//	
//	private static File[] listFiles(final File directory, final FileFilter fileFilter) throws IOException {
//        requireDirectoryExists(directory, "directory");
//        final File[] files = fileFilter == null ? directory.listFiles() : directory.listFiles(fileFilter);
//        if (files == null) {
//            // null if the directory does not denote a directory, or if an I/O error occurs.
//            throw new IOException("Unknown I/O error listing contents of directory: " + directory);
//        }
//        return files;
//    }
//	
//	private static File requireDirectoryExists(final File directory, final String name) {
//        requireExists(directory, name);
//        requireDirectory(directory, name);
//        return directory;
//    }
//	
//	private static File requireExists(final File file, final String fileParamName) {
//        Objects.requireNonNull(file, fileParamName);
//        if (!file.exists()) {
//            throw new IllegalArgumentException(
//                "File system element for parameter '" + fileParamName + "' does not exist: '" + file + "'");
//        }
//        return file;
//    }
//	
//	private static File requireDirectory(final File directory, final String name) {
//        Objects.requireNonNull(directory, name);
//        if (!directory.isDirectory()) {
//            throw new IllegalArgumentException("Parameter '" + name + "' is not a directory: '" + directory + "'");
//        }
//        return directory;
//    }
//	
//	public static File delete(final File file) throws IOException {
//        Objects.requireNonNull(file, "file");
//        Files.delete(file.toPath());
//        return file;
//    }
//	
//	public static void forceDelete(final File file) throws IOException {
//        Objects.requireNonNull(file, "file");
//        final Counters.PathCounters deleteCounters;
//        try {
//            deleteCounters = PathUtils.delete(file.toPath(), PathUtils.EMPTY_LINK_OPTION_ARRAY,
//                StandardDeleteOption.OVERRIDE_READ_ONLY);
//        } catch (final IOException e) {
//            throw new IOException("Cannot delete file: " + file, e);
//        }
//
//        if (deleteCounters.getFileCounter().get() < 1 && deleteCounters.getDirectoryCounter().get() < 1) {
//            // didn't find a file to delete.
//            throw new FileNotFoundException("File does not exist: " + file);
//        }
//    }
}
