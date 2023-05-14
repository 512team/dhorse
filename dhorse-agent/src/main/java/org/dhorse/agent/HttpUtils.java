package org.dhorse.agent;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpUtils {

	public static boolean post(String url, String param) {
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
			conn.setConnectTimeout(100);
			conn.setReadTimeout(100);
			conn.connect();
			out = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
			out.write(param);
			out.flush();
			if (HttpURLConnection.HTTP_OK != conn.getResponseCode()) {
				System.out.println(String.format("Failed to send metrics, %s response code is %s", url, conn.getResponseCode()));
				return false;
			}
		} catch (Exception e) {
			System.out.println(String.format("Failed to send metrics, message: %s", e));
			return false;
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					System.out.println(String.format("Failed to close writer, message: %s", e));
				}
			}
			if(conn != null) {
				conn.disconnect();
			}
		}
		return true;
	}
}
