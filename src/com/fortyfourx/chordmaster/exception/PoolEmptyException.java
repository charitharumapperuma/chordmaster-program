package com.fortyfourx.chordmaster.exception;

public class PoolEmptyException extends Exception {

	public PoolEmptyException() {
	}

	public PoolEmptyException(String message) {
		super(message);
	}

	public PoolEmptyException(Throwable cause) {
		super(cause);
	}

	public PoolEmptyException(String message, Throwable cause) {
		super(message, cause);
	}

	public PoolEmptyException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
