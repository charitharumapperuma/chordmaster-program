package com.fortyfourx.entity.dao;

import java.util.List;

import com.fortyfourx.entity.Artist;
import com.fortyfourx.entity.Song;

public interface SongDao {
	public Song getSongById(int id);
	public List<Song> getSongsByTitle(String title);
	public List<Song> getSongsByArtist(Artist artist);
	public List<Song> getAllSongs();
	public Song addSong(Song song);
}
