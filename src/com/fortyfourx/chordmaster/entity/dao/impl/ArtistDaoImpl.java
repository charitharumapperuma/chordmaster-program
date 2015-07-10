package com.fortyfourx.chordmaster.entity.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.fortyfourx.chordmaster.entity.Artist;
import com.fortyfourx.chordmaster.entity.dao.ArtistDao;

public class ArtistDaoImpl implements ArtistDao {
	private Connection connection;
	private PreparedStatement statement;
	private ResultSet resultset;
	private String query;
	
	public ArtistDaoImpl(Connection conn) {
		this.connection = conn;
	}
	
	@Override
	public Artist getArtistById(int id) {
		query = "SELECT * FROM artist WHERE artist.id = ?;";
		
		try {
			statement = connection.prepareStatement(query);
			statement.setInt(1, id);
			resultset = statement.executeQuery(query);
			if(resultset.next()) {
				return new Artist(resultset.getInt("id"), resultset.getString("name"), resultset.getString("url"));
			}
		} catch (Exception e) {
			e.printStackTrace(); // TODO
		}
		return null;
	}

	@Override
	public List<Artist> getArtistsByName(String name) {
		query = "SELECT * FROM artist WHERE artist.name LIKE %?%;";
		List<Artist> artists = new ArrayList<Artist>();
		try {
			statement = connection.prepareStatement(query);
			statement.setString(0, name);
			resultset = statement.executeQuery(query);
			while(resultset.next()) {
				artists.add(new Artist(resultset.getInt("id"), resultset.getString("name"), resultset.getString("url")));
			}
			return artists;
		} catch (Exception e) {
			e.printStackTrace(); // TODO
		}
		return null;
	}

	@Override
	public List<Artist> getAllArtists() {
		query = "SELECT * FROM artist;";
		List<Artist> artists = new ArrayList<Artist>();
		try {
			statement = connection.prepareStatement(query);
			resultset = statement.executeQuery(query);
			while(resultset.next()) {
				artists.add(new Artist(resultset.getInt("id"), resultset.getString("name"), resultset.getString("url")));
			}
			return artists;
		} catch (Exception e) {
			e.printStackTrace(); // TODO
		}
		return null;
	}

	@Override
	public List<String> getAllArtistUrls() {
		query = "SELECT url FROM artist;";
		List<String> artists = new ArrayList<String>();
		try {
			statement = connection.prepareStatement(query);
			resultset = statement.executeQuery(query);
			while(resultset.next()) {
				artists.add(resultset.getString("url"));
			}
			return artists;
		} catch (Exception e) {
			e.printStackTrace(); // TODO
		}
		return null;
	}

	@Override
	public int getAllArtistCount() {
		query = "SELECT COUNT(*) FROM artist;";
		int count;
		try {
			statement = connection.prepareStatement(query);
			resultset = statement.executeQuery(query);
			resultset.next();
			count = resultset.getInt(1);
			return count;
		} catch (Exception e) {
			e.printStackTrace(); // TODO
		}
		return 0;
	}

	@Override
	public Artist addArtist(Artist artist) {
		query = "SELECT id FROM artist WHERE artist.name = ?;";
		try {
			statement = connection.prepareStatement(query);
			statement.setString(1, artist.getName());
			resultset = statement.executeQuery();
			if(!resultset.next()) {
				query = "INSERT INTO artist(name,url) VALUES(?,?);";
				
				statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
				statement.setString(1, artist.getName());
				statement.setString(2, artist.getUrl());
				statement.executeUpdate();
				resultset = statement.getGeneratedKeys();
				resultset.next();
				artist.setId(resultset.getInt(1));
			} else {
				artist.setId(resultset.getInt("id"));
			}
			return artist;
		} catch (Exception e) {
			e.printStackTrace(); // TODO
		}
		return null;
	}

}
