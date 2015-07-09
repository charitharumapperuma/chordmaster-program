package com.fortyfourx.chordmaster.exception;

public class PooledObjectNotFoundException extends Exception {

	public PooledObjectNotFoundException() {
	}

	public PooledObjectNotFoundException(String message) {
		super(message);
	}

	public PooledObjectNotFoundException(Throwable cause) {
		super(cause);
	}

	public PooledObjectNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public PooledObjectNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
