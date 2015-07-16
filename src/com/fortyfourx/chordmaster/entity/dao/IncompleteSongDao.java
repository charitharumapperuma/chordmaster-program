package com.fortyfourx.chordmaster.entity.dao;

import java.util.List;

import com.fortyfourx.chordmaster.entity.IncompleteSong;

public interface IncompleteSongDao {
	public IncompleteSong			getIncompleteSong(String url);
	public List<String>				getAllIncompleteSongUrls();
	public List<IncompleteSong>		getAllIncompleteSongs();
	public void						addIncompleteSong(IncompleteSong error);
	public IncompleteSong			removeIncompleteSong(IncompleteSong error);
}
