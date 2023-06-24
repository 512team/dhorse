package org.dhorse.infrastructure.model;

import java.io.Serializable;

/**
 * JsonPath模型
 */
public class JsonPatch implements Serializable {

	private static final long serialVersionUID = 1L;

	private String op;

	private String path;

	private String value;

	public String getOp() {
		return op;
	}

	public void setOp(String op) {
		this.op = op;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}