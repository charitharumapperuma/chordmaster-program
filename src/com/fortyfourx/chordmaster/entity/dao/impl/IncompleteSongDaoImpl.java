package com.fortyfourx.chordmaster.entity.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fortyfourx.chordmaster.entity.IncompleteSong;
import com.fortyfourx.chordmaster.entity.dao.IncompleteSongDao;

public class IncompleteSongDaoImpl implements IncompleteSongDao {
	private Connection connection;
	private PreparedStatement statement;
	private ResultSet resultset;
	private String query;
	
	public IncompleteSongDaoImpl(Connection conn) {
		this.connection = conn;
	}
	
	@Override
	public IncompleteSong getIncompleteSong(String url) {
		query = "SELECT * FROM error WHERE error.url = ?;";
		try {
			statement = connection.prepareStatement(query);
			statement.setString(1, url);
			resultset = statement.executeQuery();
			if(resultset.next()){
				return new IncompleteSong(
								resultset.getInt("id"),
								resultset.getString("timestamp"),
								resultset.getString("url"),
								resultset.getString("exception")
							);
			}
		} catch (Exception e) {
			e.printStackTrace(); // TODO
		}
		return null;
	}

	@Override
	public List<String> getAllIncompleteSongUrls() {
		List<String> exceptionUrls = new ArrayList<String>();
		query = "SELECT url FROM incomplete_song;";
		try {
			statement = connection.prepareStatement(query);
			resultset = statement.executeQuery();
			while (resultset.next()) {
				exceptionUrls.add(resultset.getString("url"));
			}
			return exceptionUrls;
		} catch (Exception e) {
			e.printStackTrace(); // TODO
		}
		return null;
	}

	@Override
	public void addIncompleteSong(IncompleteSong incSong) {
		query = "INSERT INTO incomplete_song(url, exception) VALUES(?,?);";
		try {
			statement = connection.prepareStatement(query);
			statement.setString(1, incSong.getUrl());
			statement.setString(2, incSong.getException());
			statement.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace(); // TODO
		}
	}

	@Override
	public IncompleteSong removeIncompleteSong(IncompleteSong incSong) {
		IncompleteSong songError = null;
		query = "SELECT * FROM incomplete_song WHERE error.url = ?;";
		try {
			statement = connection.prepareStatement(query);
			statement.setString(1, incSong.getUrl());
			resultset = statement.executeQuery();
			int id;
			if(resultset.next()){
				id = resultset.getInt("id");
				songError = new IncompleteSong(
								id,
								resultset.getString("timestamp"),
								resultset.getString("url"),
								resultset.getString("exception")
							);
				query = "DELETE FROM error WHERE error.id = ?;";
				statement = connection.prepareStatement(query);
				statement.setInt(1, id);
				statement.execute();
			}
			
			return songError;
		} catch (Exception e) {
			e.printStackTrace(); // TODO
		}
		return null;
	}

}
