package com.fortyfourx.chordmaster.entity;

import java.io.Serializable;

/**
 * @author Charith Arumapperuma Holds information of newly detected songs, which
 *         have not yet visited to collect data that is necessary to convert it
 *         into a {@link Song}.
 * @see Song
 * @see Artist
 */
public class NewSong implements Serializable {
	private static final long serialVersionUID = 1L;
	private String url;
	private Artist artist;

	public NewSong(String url, Artist artist) {
		this.url = url;
		this.artist = artist;
	}
	
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Artist getArtist() {
		return artist;
	}

	public void setArtist(Artist artist) {
		this.artist = artist;
	}
}
