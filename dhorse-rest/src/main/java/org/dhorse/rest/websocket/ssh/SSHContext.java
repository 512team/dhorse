package org.dhorse.rest.websocket.ssh;

import java.io.ByteArrayOutputStream;

import org.springframework.web.socket.WebSocketSession;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.ExecWatch;

public class SSHContext {

	private String namespace;

	private String replicaName;

	private WebSocketSession session;

	private KubernetesClient client;

	private ExecWatch watch;

	private ByteArrayOutputStream baos = new ByteArrayOutputStream();

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public String getReplicaName() {
		return replicaName;
	}

	public void setReplicaName(String replicaName) {
		this.replicaName = replicaName;
	}

	public WebSocketSession getSession() {
		return session;
	}

	public void setSession(WebSocketSession session) {
		this.session = session;
	}

	public KubernetesClient getClient() {
		return client;
	}

	public void setClient(KubernetesClient client) {
		this.client = client;
	}

	public ByteArrayOutputStream getBaos() {
		return baos;
	}

	public void setBaos(ByteArrayOutputStream baos) {
		this.baos = baos;
	}

	public ExecWatch getWatch() {
		return watch;
	}

	public void setWatch(ExecWatch watch) {
		this.watch = watch;
	}

}
