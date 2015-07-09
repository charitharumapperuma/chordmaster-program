package com.fortyfourx.chordmaster.exception;

public class PoolBusyException extends Exception {

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
