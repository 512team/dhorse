package org.dhorse.infrastructure.utils;

import java.io.IOException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.dhorse.api.enums.MessageCodeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpUtils {

	private static final Logger logger = LoggerFactory.getLogger(HttpUtils.class);
	
	public static CloseableHttpResponse post(String url, String jsonParam) {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(5000)
                .setConnectTimeout(5000)
                .setSocketTimeout(5000)
                .build();
        HttpPost httpPost = new HttpPost(url);
        httpPost.setConfig(requestConfig);
        httpPost.setHeader("Content-Type", "application/json;charset=UTF-8");
        httpPost.setEntity(new StringEntity(jsonParam, "UTF-8"));
        try (CloseableHttpResponse response = createHttpClient(url).execute(httpPost)){
        	return response;
        } catch (IOException e) {
        	LogUtils.throwException(logger, MessageCodeEnum.HTT_POST_FAILURE);
        }
        return null;
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
