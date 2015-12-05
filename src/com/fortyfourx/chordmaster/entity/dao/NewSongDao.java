package com.fortyfourx.chordmaster.entity.dao;

import java.util.List;

import com.fortyfourx.chordmaster.entity.NewSong;

public interface NewSongDao {
	public NewSong getNewSong(String url);
	public List<NewSong> getAllNewSongs();
	public boolean addNewSong(NewSong newSong);
	public boolean addNewSongIgnoreVisited(NewSong newSong);
	public void removeNewSong(NewSong newSong);
}
