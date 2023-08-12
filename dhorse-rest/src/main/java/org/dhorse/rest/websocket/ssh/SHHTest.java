package org.dhorse.rest.websocket.ssh;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.fabric8.kubernetes.client.dsl.ExecListener;
import io.fabric8.kubernetes.client.dsl.ExecWatch;

/**
 * This sample code is Java equivalent to `kubectl exec my-pod -- ls /`. It
 * assumes that a Pod with specified name exists in the cluster.
 */
public class SHHTest {
	private static final Logger logger = LoggerFactory.getLogger(SHHTest.class);
	private static final CountDownLatch execLatch = new CountDownLatch(1);

	private static String token = "eyJhbGciOiJSUzI1NiIsImtpZCI6IlM1VENpZ25KRHFpT3Yxc2RKZDVTVDE2TGZkcXNKbnNja3l0UDFzeWtWb0UifQ.eyJpc3MiOiJrdWJlcm5ldGVzL3NlcnZpY2VhY2NvdW50Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9uYW1lc3BhY2UiOiJrdWJlLXN5c3RlbSIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VjcmV0Lm5hbWUiOiJkaG9yc2UtYWRtaW4tdG9rZW4tOTVtN2oiLCJrdWJlcm5ldGVzLmlvL3NlcnZpY2VhY2NvdW50L3NlcnZpY2UtYWNjb3VudC5uYW1lIjoiZGhvcnNlLWFkbWluIiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9zZXJ2aWNlLWFjY291bnQudWlkIjoiN2Q2OThmYjktMDdjOC00OThhLWJlZjItOTRiY2Y0NGY0MmQyIiwic3ViIjoic3lzdGVtOnNlcnZpY2VhY2NvdW50Omt1YmUtc3lzdGVtOmRob3JzZS1hZG1pbiJ9.NB33Fwbuhce21netxqzvU-gl2FTdavLS4_pSc19TmgKq8hAvIMZ0LoowDyocvpQFsVzjZ9XTVujq9sSQes2A1HYskWYB0Aj7T6Y9jKH_jsL7DWHJs_TVVQVsKoUkI1Muvvw4ozfo___jofPoEksium0RW3a5x0HuAOoby8clY5WYxjCpgNolKvGBcbCX7ozaps0tDO2cYm8yM7EOMqBbk-tPddwPmnC_s5p4mh6l9RF1AEjHGeq6fkFQFjEaPxqNBxXFEegkqqnS21iQJHtKS9w6wal6AirsG6iJPk0AMs74CbDMxD0QZKIuccqDxhcoHS4bcrGGBarPLusGgj5TdQ";
	
	public static void main(String[] args) {
		Config config = new ConfigBuilder()
				 .withTrustCerts(true)
				 .withMasterUrl("https://192.168.109.137:6443")
				 .withOauthToken(token)
				 .build();
		try (final KubernetesClient k8s = new KubernetesClientBuilder().withConfig(config).build()){
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			ByteArrayOutputStream error = new ByteArrayOutputStream();

			ExecWatch execWatch = k8s.pods().inNamespace("default")
					.withName("hello-1-qa-dhorse-7849ddf747-snzbk")
					.writingOutput(out)
					.writingError(error)
					.usingListener(new MyPodExecListener())
					.exec("/bin/sh");

			boolean latchTerminationStatus = execLatch.await(5, TimeUnit.SECONDS);
			if (!latchTerminationStatus) {
				logger.warn("Latch could not terminate within specified time");
			}
			logger.info("Exec Output: {} ", out);
			
			execWatch.getInput();
			
			execWatch.close();
		} catch (InterruptedException ie) {
			Thread.currentThread().interrupt();
			logger.warn("Interrupted while waiting for the exec: {}", ie.getMessage());
		}
	}

	private static class MyPodExecListener implements ExecListener {
		@Override
		public void onOpen() {
			logger.info("Shell was opened");
		}

		@Override
		public void onFailure(Throwable t, Response failureResponse) {
			logger.info("Some error encountered");
			execLatch.countDown();
		}

		@Override
		public void onClose(int i, String s) {
			logger.info("Shell Closing");
			execLatch.countDown();
		}
	}
}
