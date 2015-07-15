package com.fortyfourx.chordmaster.entity;

import java.io.Serializable;

public class Artist implements Serializable {
	private static final long serialVersionUID = 1L;
	private int id;
	private String name;
	private String url;

	public Artist() {} 
	
	public Artist(String name, String url) {
		this.id= 0;
		this.name = name;
		this.url = url;
	}
	
	public Artist(int id, String name, String url) {
		this.id = id;
		this.name = name;
		this.url = url;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}