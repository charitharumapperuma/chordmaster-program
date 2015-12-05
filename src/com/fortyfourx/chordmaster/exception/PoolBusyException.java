package com.fortyfourx.chordmaster.exception;

public class PoolBusyException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1172224432339754334L;

	public PoolBusyException() {
	}

	public PoolBusyException(String message) {
		super(message);
	}

	public PoolBusyException(Throwable cause) {
		super(cause);
	}

	public PoolBusyException(String message, Throwable cause) {
		super(message, cause);
	}

	public PoolBusyException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
