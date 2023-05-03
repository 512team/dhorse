package org.dhorse.agent;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpUtils {

	private static final Logger logger = LoggerFactory.getLogger(HttpUtils.class);

	public static boolean sendPost(String url, String param) {
		BufferedWriter out = null;
		HttpURLConnection conn = null;
		try {
			conn = (HttpURLConnection) new URL(url).openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setUseCaches(false);
			conn.setInstanceFollowRedirects(true);
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.connect();
			out = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
			out.write(param);
			out.flush();
			if (HttpURLConnection.HTTP_OK != conn.getResponseCode()) {
				logger.error("Failed to send metrics, {} response code is {}", url, conn.getResponseCode());
				return false;
			}
		} catch (Exception e) {
			logger.error("Failed to send metrics", e);
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					logger.error("Failed to close writer", e);
				}
			}
			if(conn != null) {
				conn.disconnect();
			}
		}
		return true;
	}
}
