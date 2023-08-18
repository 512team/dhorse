package org.dhorse.rest.websocket.ssh;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;

import io.fabric8.kubernetes.client.KubernetesClient;

public class SSHContext {

	private String namespace;

	private String replicaName;

	private Closeable session;

	private KubernetesClient client;

	private Closeable watch;

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

	@SuppressWarnings("unchecked")
	public <T extends Closeable> T getSession() {
		return (T)session;
	}

	public void setSession(Closeable session) {
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

	@SuppressWarnings("unchecked")
	public <T extends Closeable> T getWatch() {
		return (T)watch;
	}

	public void setWatch(Closeable watch) {
		this.watch = watch;
	}

}
