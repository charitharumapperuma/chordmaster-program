package com.fortyfourx.entity.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.fortyfourx.entity.Artist;
import com.fortyfourx.entity.Song;
import com.fortyfourx.entity.dao.SongDao;

public class SongDaoImpl implements SongDao{
	private Connection connection;
	private PreparedStatement statement;
	private ResultSet resultset;
	private String query;
	
	public SongDaoImpl(Connection conn) {
		this.connection = conn;
	}
	
	@Override
	public Song getSongById(int id) {
		ArtistDaoImpl artistDao = new ArtistDaoImpl(this.connection);
		
		query = "SELECT * FROM song WHERE song.id = ?;";
		try {
			statement = connection.prepareStatement(query);
			statement.setInt(1, id);
			resultset = statement.executeQuery(query);
			if(resultset.next()) {
				return new Song(
					resultset.getInt("id"), 
					resultset.getString("title"), 
					resultset.getString("url"),
					resultset.getString("lyrics"), 
					resultset.getString("keynote"), 
					resultset.getString("beat"),
					artistDao.getArtistById(resultset.getInt("artist"))
				);
			}
		} catch (Exception e) {
			e.printStackTrace(); // TODO
		}
		return null;
	}

	@Override
	public List<Song> getSongsByTitle(String title) {
		ArtistDaoImpl artistDao = new ArtistDaoImpl(this.connection);
		List<Song> songs = new ArrayList<Song>();
		
		query = "SELECT * FROM song WHERE song.title LIKE %?%;";
		try {
			statement = connection.prepareStatement(query);
			statement.setString(0, title);
			resultset = statement.executeQuery(query);
			while(resultset.next()) {
				songs.add(new Song(
						resultset.getInt("id"), 
						resultset.getString("title"), 
						resultset.getString("url"),
						resultset.getString("lyrics"), 
						resultset.getString("keynote"), 
						resultset.getString("beat"),
						artistDao.getArtistById(resultset.getInt("artist"))
					)
				);
			}
			return songs;
		} catch (Exception e) {
			e.printStackTrace(); // TODO
		}
		return null;
	}

	@Override
	public List<Song> getSongsByArtist(Artist artist) {
		ArtistDaoImpl artistDao = new ArtistDaoImpl(this.connection);
		List<Song> songs = new ArrayList<Song>();

		query = "SELECT * FROM song WHERE song.artist = ?";
		try {
			statement = connection.prepareStatement(query);
			statement.setInt(1, artist.getId());
			resultset = statement.executeQuery();
			while (resultset.next()) {
				songs.add(
					new Song(
						resultset.getInt("id"), 
						resultset.getString("title"), 
						resultset.getString("url"),
						resultset.getString("lyrics"), 
						resultset.getString("keynote"), 
						resultset.getString("beat"),
						artistDao.getArtistById(resultset.getInt("artist"))
					)
				);
			}
			return songs;
		} catch (Exception e) {
			e.printStackTrace(); // TODO
		}
		return null;
	}

	@Override
	public List<Song> getAllSongs() {
		ArtistDaoImpl artistDao = new ArtistDaoImpl(this.connection);
		List<Song> songs = new ArrayList<Song>();
		
		query = "SELECT * FROM song;";
		try {
			statement = connection.prepareStatement(query);
			resultset = statement.executeQuery(query);
			while(resultset.next()) {
				songs.add(
					new Song(
						resultset.getInt("id"), 
						resultset.getString("title"), 
						resultset.getString("url"),
						resultset.getString("lyrics"), 
						resultset.getString("keynote"), 
						resultset.getString("beat"),
						artistDao.getArtistById(resultset.getInt("artist"))
					)
				);
			}
			return songs;
		} catch (Exception e) {
			e.printStackTrace(); // TODO
		}
		return null;
	}

	@Override
	public Song addSong(Song song) {
		query = "SELECT count(*) FROM song WHERE song.id = ?;";
		try {
			statement = connection.prepareStatement(query);
			statement.setInt(1, song.getId());
			resultset = statement.executeQuery();
			resultset.next();
			if (resultset.getInt(1) == 0) { 
				query = "INSERT INTO song(id,title,url,lyrics,keynote,beat,artist) VALUES(?,?,?,?,?,?,?);";
				
				statement = connection.prepareStatement(query);
				statement.setInt(1, song.getId());
				statement.setString(2, song.getTitle());
				statement.setString(3, song.getUrl());
				statement.setString(4, song.getLyrics());
				statement.setString(5, song.getKey());
				statement.setString(6, song.getBeat());
				statement.setInt(7, song.getArtist().getId());
				statement.executeUpdate();
			}
			return song;
		} catch (Exception e) {
			e.printStackTrace(); // TODO
		}
		return null;
	}

}