package org.dhorse.infrastructure.utils;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.SSLContext;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;
import org.dhorse.api.enums.MessageCodeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

public class HttpUtils {

	private static final Logger logger = LoggerFactory.getLogger(HttpUtils.class);
	
	public static boolean pingDHorseServer(String ipWithPort) {
		String pingUrl = "http://" + ipWithPort + "/system/ping";
		try {
			return get(pingUrl, 1000) == 200;
		}catch(Exception e) {
			logger.error("Failed to call " + pingUrl, e);
			return false;
		}
	}
	
	public static int get(String url) {
		return get(url, 2000, null);
	}
	
	public static int get(String url, int timeout) {
		return get(url, timeout, null);
	}
	
	public static int get(String url, int timeout, Map<String, Object> cookies) {
        try (CloseableHttpResponse response = doGet(url, timeout, cookies)){
        	return response.getStatusLine().getStatusCode();
        } catch (IOException e) {
        	LogUtils.throwException(logger, MessageCodeEnum.HTT_GET_FAILURE);
        }
        return -1;
	}
	
	public static String getResponse(String url) {
        return getResponse(url, null);
	}
	
	public static String getResponse(String url, Map<String, Object> cookies) {
        try (CloseableHttpResponse response = doGet(url, 2000, cookies)){
        	return EntityUtils.toString(response.getEntity());
        } catch (IOException e) {
        	LogUtils.throwException(logger, MessageCodeEnum.HTT_GET_FAILURE);
        }
        return null;
	}
	
	private static CloseableHttpResponse doGet(String url, int timeout, Map<String, Object> cookies)
			throws ClientProtocolException, IOException {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(timeout)
                .setConnectTimeout(timeout)
                .setSocketTimeout(timeout)
                .build();
        HttpGet method = new HttpGet(url);
        method.setConfig(requestConfig);
        if(!CollectionUtils.isEmpty(cookies)) {
        	String cookieStr = "";
        	for(Entry<String, Object> c : cookies.entrySet()) {
        		cookieStr = cookieStr + c.getKey() + "=" + c.getValue() + ";";
        	}
        	method.setHeader("Cookie", cookieStr);
        }
        return createHttpClient(url).execute(method);
	}
	
	public static String postResponse(String url, String jsonParam, Map<String, Object> cookie) {
        try (CloseableHttpResponse response = doPost(url, jsonParam, cookie)){
        	return EntityUtils.toString(response.getEntity());
        } catch (IOException e) {
        	LogUtils.throwException(logger, e, MessageCodeEnum.HTT_POST_FAILURE);
        }
        return null;
	}
	
	public static String postResponse(String url, Map<String, Object> param) {
        try (CloseableHttpResponse response = doPost(url, JsonUtils.toJsonString(param), null)){
        	return EntityUtils.toString(response.getEntity());
        } catch (IOException e) {
        	LogUtils.throwException(logger, e, MessageCodeEnum.HTT_POST_FAILURE);
        }
        return null;
	}
	
	public static int post(String url, Map<String, Object> param) {
		return post(url, JsonUtils.toJsonString(param), null);
	}
	
	public static int post(String url, String jsonParam) {
		return post(url, jsonParam, null);
	}
	
	public static int post(String url, String jsonParam, Map<String, Object> cookies) {
        try (CloseableHttpResponse response = doPost(url, jsonParam, cookies)){
        	return response.getStatusLine().getStatusCode();
        } catch (IOException e) {
        	LogUtils.throwException(logger, e, MessageCodeEnum.HTT_POST_FAILURE);
        }
        return -1;
	}
	
	public static CloseableHttpResponse doPost(String url, String jsonParam,
			Map<String, Object> cookies) throws ClientProtocolException, IOException {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(2000)
                .setConnectTimeout(2000)
                .setSocketTimeout(2000)
                .build();
        HttpPost httpPost = new HttpPost(url);
        httpPost.setConfig(requestConfig);
        httpPost.setHeader("Content-Type", "application/json;charset=UTF-8");
        if(!CollectionUtils.isEmpty(cookies)) {
        	String cookieStr = "";
        	for(Entry<String, Object> c : cookies.entrySet()) {
        		cookieStr = cookieStr + c.getKey() + "=" + c.getValue() + ";";
        	}
        	httpPost.setHeader("Cookie", cookieStr);
        }
        if(!StringUtils.isBlank(jsonParam)) {
        	httpPost.setEntity(new StringEntity(jsonParam, "UTF-8"));
        }
        return createHttpClient(url).execute(httpPost);
	}
	
	public static CloseableHttpClient createHttpClient(String url) {
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
			SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
			return HttpClients.custom().setSSLSocketFactory(sslsf).build();
		} catch (Exception e) {
			LogUtils.throwException(logger, e, MessageCodeEnum.SSL_CLIENT_FAILURE);
		}
		
		return HttpClients.createDefault();
	}
}
