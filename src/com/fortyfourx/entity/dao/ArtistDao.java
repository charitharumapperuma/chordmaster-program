package com.fortyfourx.entity.dao;

import java.util.List;

import com.fortyfourx.entity.Artist;

public interface ArtistDao {
	public Artist 		getArtistById(int id);
	public List<Artist> getArtistsByName(String name);
	public List<Artist> getAllArtists();
	public List<String> getAllArtistsUrls();
	public int 			getAllArtistsCount();
	public Artist 		addArtist(Artist artist);
}
