package com.wirelust.cfmock.exceptions;

/**
 * Date: 16-Jun-2016
 *
 * @author T. Curran
 */
public class CFMockException extends RuntimeException {

	public CFMockException(Throwable cause) {
		super.initCause(cause);
	}

	public CFMockException(String message) {
		super(message);
	}

}
