package org.dhorse.infrastructure.strategy.repo;

public class AliCloudeImageRepo {

//	/**
//	 * 使用AK&SK初始化账号Client
//	 * 
//	 * @param accessKeyId
//	 * @param accessKeySecret
//	 * @return Client
//	 * @throws Exception
//	 */
//	public static com.aliyun.cr20181201.Client createClient(String accessKeyId, String accessKeySecret)
//			throws Exception {
//		com.aliyun.teaopenapi.models.Config config = new com.aliyun.teaopenapi.models.Config()
//				// 必填，您的 AccessKey ID
//				.setAccessKeyId(accessKeyId)
//				// 必填，您的 AccessKey Secret
//				.setAccessKeySecret(accessKeySecret);
//		// 访问的域名
//		config.endpoint = "cr.cn-hangzhou.aliyuncs.com";
//		return new com.aliyun.cr20181201.Client(config);
//	}
//
//	public static void main(String[] args_) throws Exception {
//		// 请确保代码运行环境设置了环境变量 ALIBABA_CLOUD_ACCESS_KEY_ID 和
//		// ALIBABA_CLOUD_ACCESS_KEY_SECRET。
//		// 工程代码泄露可能会导致 AccessKey 泄露，并威胁账号下所有资源的安全性。以下代码示例使用环境变量获取 AccessKey
//		// 的方式进行调用，仅供参考，建议使用更安全的 STS
//		// 方式，更多鉴权访问方式请参见：https://help.aliyun.com/document_detail/378657.html
//		com.aliyun.cr20181201.Client client = AliCloudeImageRepo.createClient("LTAI5tHZ5CiQdmmLz6BnkLKj",
//				"dk3UcPZujVl0oOWC9V7FGivGLEWNmS");
//		com.aliyun.cr20181201.models.GetInstanceRequest getInstanceRequest = new com.aliyun.cr20181201.models.GetInstanceRequest()
//				.setInstanceId("5176.8351553.0.i3.5c7e1991cAqBwJ");//需要企业认证，才会有实例Id
//		com.aliyun.teautil.models.RuntimeOptions runtime = new com.aliyun.teautil.models.RuntimeOptions();
//		try {
//			// 复制代码运行请自行打印 API 的返回值
//			GetInstanceResponse r = client.getInstanceWithOptions(getInstanceRequest, runtime);
//			r.getBody();
//		} catch (TeaException error) {
//			error.printStackTrace();
//		} catch (Exception _error) {
//			_error.printStackTrace();
//		}
//	}
}
