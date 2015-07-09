package com.fortyfourx.chordmaster.exception;

public class PoolFullException extends Exception {

	public PoolFullException() {
	}

	public PoolFullException(String message) {
		super(message);
	}

	public PoolFullException(Throwable cause) {
		super(cause);
	}

	public PoolFullException(String message, Throwable cause) {
		super(message, cause);
	}

	public PoolFullException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
