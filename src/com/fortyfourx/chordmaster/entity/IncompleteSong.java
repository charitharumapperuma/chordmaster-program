package com.fortyfourx.chordmaster.entity;

import java.io.Serializable;

public class IncompleteSong implements Serializable {
	private static final long serialVersionUID = 1L;
	private int id;
	private String timestamp;
	private String url;
	private String exception;

	public IncompleteSong() {}
	
	public IncompleteSong(int id, String timestamp, String url, String exception) {
		this.id = id;
		this.timestamp = timestamp;
		this.url = url;
		this.exception = exception;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getException() {
		return exception;
	}

	public void setException(String exception) {
		this.exception = exception;
	}

}
