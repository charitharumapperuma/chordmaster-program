package com.fortyfourx.chordmaster.entity.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.fortyfourx.chordmaster.entity.NewSong;
import com.fortyfourx.chordmaster.entity.dao.ArtistDao;
import com.fortyfourx.chordmaster.entity.dao.NewSongDao;

public class NewSongDaoImpl implements NewSongDao {
	private Connection connection;
	private PreparedStatement statement;
	private ResultSet resultset;
	private String query;
	
	public NewSongDaoImpl(Connection conn) {
		this.connection = conn;
	}
	
	@Override
	public NewSong getNewSong(String url) {
		ArtistDao artistDao = new ArtistDaoImpl(connection);
		
		query = "SELECT * FROM song_new WHERE url.url = ?;";
		try {
			statement = connection.prepareStatement(query);
			statement.setString(1, url);
			resultset = statement.executeQuery();
			if(resultset.next()){
				return new NewSong(
						resultset.getString("url"), 
						artistDao.getArtistById(resultset.getInt("artist"))
					);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public List<NewSong> getAllNewSongs() {
		ArtistDao artistDao = new ArtistDaoImpl(connection);
		
		List<NewSong> newSongs = new ArrayList<NewSong>();
		query = "SELECT * FROM song_new;";
		try {
			statement = connection.prepareStatement(query);
			resultset = statement.executeQuery();
			while(resultset.next()) {
				newSongs.add(new NewSong(
							resultset.getString("url"),
							artistDao.getArtistById(resultset.getInt("id"))
						)
					);
			}
			return newSongs;
		} catch (Exception e) {
			e.printStackTrace(); // TODO
		}
		return null;
	}

	@Override
	public boolean addNewSong(NewSong newSong) {
		query = "SELECT count(*) FROM song_new WHERE url = ?;";
		try {
			statement = connection.prepareStatement(query);
			statement.setString(1, newSong.getUrl());
			resultset = statement.executeQuery();
			resultset.next();
			if (resultset.getInt(1) == 0) { 
				query = "INSERT INTO song_new(url, artist) VALUES(?,?);";
				
				statement = connection.prepareStatement(query);
				statement.setString(1, newSong.getUrl());
				statement.setInt(2, newSong.getArtist().getId());
				statement.executeUpdate();
				
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace(); // TODO
		}
		return false;
	}

	@Override
	public boolean addNewSongIgnoreVisited(NewSong newSong) {
		query = "SELECT count(*) FROM song WHERE song.url = ?";
		
		try {
			statement = connection.prepareStatement(query);
			statement.setString(1, newSong.getUrl());
			resultset = statement.executeQuery();
			resultset.next();
			if (resultset.getInt(1) == 0) { 
				this.addNewSong(newSong);
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace(); // TODO
		}
		return false;
	}
	
	@Override
	public void removeNewSong(NewSong newSong) {
		query = "DELETE FROM song_new WHERE url = ?";
		try {
			statement = connection.prepareStatement(query);
			statement.setString(1, newSong.getUrl());
			statement.execute();
		} catch (Exception e) {
			e.printStackTrace(); // TODO
		}
	}

}
