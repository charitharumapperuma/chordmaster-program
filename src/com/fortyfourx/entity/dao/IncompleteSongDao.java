package com.fortyfourx.entity.dao;

import java.util.List;

import com.fortyfourx.entity.IncompleteSong;

public interface IncompleteSongDao {
	public IncompleteSong getIncompleteSong(String url);
	public List<String> getAllIncompleteSongUrls();
	public void addIncompleteSong(IncompleteSong error);
	public IncompleteSong removeIncompleteSong(IncompleteSong error);
}
