package com.fortyfourx.chordmaster.entity;

import java.io.Serializable;

public class Song implements Serializable {
	private static final long serialVersionUID = 1L;
	private int id;
	private String title;
	private String url;
	private String lyrics;
	private String key;
	private String beat;
	private Artist artist;

	public Song() {}
	
	public Song(int id, String title, String url, String lyrics, String key,
			String beat, Artist artist) {
		this.id = id;
		this.title = title;
		this.url = url;
		this.lyrics = lyrics;
		this.key = key;
		this.beat = beat;
		this.artist = artist;
	}

	public Song(int id, String title, String url) {
		this.title = title;
		this.url = url;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getLyrics() {
		return lyrics;
	}

	public void setLyrics(String lyrics) {
		this.lyrics = lyrics;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getBeat() {
		return beat;
	}

	public void setBeat(String beat) {
		this.beat = beat;
	}

	public Artist getArtist() {
		return artist;
	}

	public void setArtist(Artist artist) {
		this.artist = artist;
	}

}
