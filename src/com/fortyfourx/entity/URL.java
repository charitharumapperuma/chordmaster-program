package com.fortyfourx.entity;

public class URL {
	private String url;
	private int status;

	public URL(String url, int status) {
		this.url = url;
		this.status = status;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

}
