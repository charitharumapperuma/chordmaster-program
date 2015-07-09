package com.fortyfourx.entity.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.fortyfourx.entity.NewSong;
import com.fortyfourx.entity.dao.NewSongDao;

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
		ArtistDaoImpl artistDao = new ArtistDaoImpl(connection);
		
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
		ArtistDaoImpl artistDao = new ArtistDaoImpl(connection);
		
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
	public void addNewSong(NewSong newSong) {
		query = "SELECT count(*) FROM song_new WHERE url = ?;";
		try {
			statement = connection.prepareStatement(query);
			statement.setString(1, newSong.getUrl());
			resultset = statement.executeQuery();
			resultset.next();
			if (resultset.getInt(1) == 0) { 
				query = "INSERT INTO song_new(url) VALUES(?,?);";
				
				statement = connection.prepareStatement(query);
				statement.setString(1, newSong.getUrl());
				statement.setInt(2, newSong.getArtist().getId());
				statement.executeUpdate();
			}
		} catch (Exception e) {
			e.printStackTrace(); // TODO
		}
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
