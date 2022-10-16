package org.dhorse.infrastructure.exception;

public class SysException extends Exception {

	private static final long serialVersionUID = 1L;

	public SysException() {
		super();
	}
	
	public SysException(String message) {
        super(message);
    }
}
