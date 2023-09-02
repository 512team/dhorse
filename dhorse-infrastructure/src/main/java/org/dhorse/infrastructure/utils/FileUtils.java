package org.dhorse.infrastructure.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.IOUtils;
import org.dhorse.api.enums.MessageCodeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("deprecation")
public class FileUtils extends org.apache.commons.io.FileUtils {

	private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);

	public static String unZip(String zipPath, String destParentPath) {
		logger.info("Start to decompress zip file");
	    File destFile = new File(destParentPath);
	    if (!destFile.exists()) {
	        destFile.mkdirs();
	    }
	    String destPath = null;
	    try(ZipFile zip = new ZipFile(zipPath)){
	    	Enumeration<? extends ZipEntry> entries = zip.entries();
		    for (int i = 0; entries.hasMoreElements(); i++) {
		        ZipEntry entry = entries.nextElement();
		        String curEntryName = entry.getName();
		        int endIndex = curEntryName.lastIndexOf('/');
		        String outPath = (destParentPath + curEntryName).replaceAll("\\*", "/");
		        if(i == 0) {
		        	destPath = outPath;
		        }
		        if (endIndex != -1) {
		            File file = new File(outPath.substring(0, outPath.lastIndexOf("/")));
		            if (!file.exists()) {
		                file.mkdirs();
		            }
		        }
	
		        File outFile = new File(outPath);
		        if (outFile.isDirectory()) {
		            continue;
		        }
		        
		        byte[] buffer = new byte[1024 * 1024];
		        int len;
		        try(InputStream in = zip.getInputStream(entry);
				        OutputStream out = new FileOutputStream(outPath)){
			        while ((len = in.read(buffer)) > 0) {
			            out.write(buffer, 0, len);
			        }
		        }catch (Exception ex) {
                	LogUtils.throwException(logger, ex, MessageCodeEnum.DECOMPRESSION_FILE_FAILURE);
                }
		    }
	    }catch(Exception e) {
	    	LogUtils.throwException(logger, e, MessageCodeEnum.DECOMPRESSION_FILE_FAILURE);
	    }
	    logger.info("End to decompress zip file");
	    return destPath;
	}
	
	public static String unTarGz(String orgFileName, String parentPath) {
		return unTarGz(new File(orgFileName), new File(parentPath));
	}

	public static String unTarGz(File sourceTarGzFile, File parentPath) {
		logger.info("Start to decompress gz file");
		String destPath = null;
		try (TarArchiveInputStream in = new TarArchiveInputStream(
				new GzipCompressorInputStream(Files.newInputStream(sourceTarGzFile.toPath())))) {
			ArchiveEntry entry;
			for (int i = 0; (entry = in.getNextEntry()) != null; i++) {
				File targetFile = new File(parentPath, entry.getName());
				if(i == 0) {
					destPath = targetFile.getPath();
			    }
				if (entry.isDirectory()) {
					continue;
				}
				File parent = targetFile.getParentFile();
				if (!parent.exists()) {
					parent.mkdirs();
				}
				try(OutputStream out = Files.newOutputStream(targetFile.toPath())){
					IOUtils.copy(in, out);
				}catch (Exception ex) {
					LogUtils.throwException(logger, ex, MessageCodeEnum.DECOMPRESSION_FILE_FAILURE);
                }
			}
		} catch (Exception e) {
			LogUtils.throwException(logger, e, MessageCodeEnum.DECOMPRESSION_FILE_FAILURE);
		}
		logger.info("End to decompress gz file");
		return destPath;
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
			LogUtils.throwException(logger, e, MessageCodeEnum.DOWNLOAD_FAILURE);
		}
		logger.info("End to download file");
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
