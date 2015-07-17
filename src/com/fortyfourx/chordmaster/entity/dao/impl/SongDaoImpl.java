package com.fortyfourx.chordmaster.entity.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fortyfourx.chordmaster.entity.Artist;
import com.fortyfourx.chordmaster.entity.Song;
import com.fortyfourx.chordmaster.entity.dao.ArtistDao;
import com.fortyfourx.chordmaster.entity.dao.SongDao;

public class SongDaoImpl implements SongDao{
	private Connection connection;
	private PreparedStatement statement;
	private ResultSet resultset;
	private String query;
	
	public SongDaoImpl(Connection conn) {
		this.connection = conn;
	}
	
	@Override
	public int getAllSongsCount() {
		query = "SELECT COUNT(*) FROM song;";
		try {
			statement = connection.prepareStatement(query);
			resultset = statement.executeQuery();
			resultset.next();
			return resultset.getInt(1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	@Override
	public Song getSongById(int id) {
		ArtistDao artistDao = new ArtistDaoImpl(this.connection);
		
		query = "SELECT * FROM song WHERE song.id = ?;";
		try {
			statement = connection.prepareStatement(query);
			statement.setInt(1, id);
			resultset = statement.executeQuery();
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
		ArtistDao artistDao = new ArtistDaoImpl(this.connection);
		List<Song> songs = new ArrayList<Song>();
		
		query = "SELECT * FROM song WHERE song.title LIKE %?%;";
		try {
			statement = connection.prepareStatement(query);
			statement.setString(1, title);
			resultset = statement.executeQuery();
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
		List<Song> songs = new ArrayList<Song>();

		query = "SELECT * FROM song WHERE song.artist = ?;";
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
						artist
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
	public Map<Artist, Integer> getAllSongsPerArtist() {
		ArtistDao artistDao = new ArtistDaoImpl(this.connection);
		Map<Artist, Integer> counts = new HashMap<>();
		
		query = "SELECT artist, count(*) as 'count' from song GROUP BY artist;";
		try {
			statement = connection.prepareStatement(query);
			resultset = statement.executeQuery();
			while(resultset.next()) {
				counts.put(
						artistDao.getArtistById(resultset.getInt("artist")),
						resultset.getInt("count")
					);
			}
			return counts;
		} catch (Exception e) {
			e.printStackTrace(); // TODO
		}
		return null;
	}
	
	@Override
	public List<Song> getAllSongs() {
		ArtistDao artistDao = new ArtistDaoImpl(this.connection);
		List<Song> songs = new ArrayList<Song>();
		
		query = "SELECT * FROM song;";
		try {
			statement = connection.prepareStatement(query);
			resultset = statement.executeQuery();
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

	@Override
	public int getNextUnvalidatedSongId() {
		int id;

		query = "SELECT id FROM song WHERE id NOT IN (SELECT song AS id FROM song_validated) LIMIT 1;";
		try {
			statement = connection.prepareStatement(query);
			resultset = statement.executeQuery();
			if(resultset.next()) {
				id = resultset.getInt("id");
				return id;
			}
		} catch (Exception e) {
			e.printStackTrace(); // TODO
		}
		return -1;
	}

	@Override
	public int[] getAllValidatedSongIds() {
		query = "SELECT song AS id FROM song_validated;";
		
		List<Integer> idList = new ArrayList<Integer>();
		int[] idArray = null;
		try {
			statement = connection.prepareStatement(query);
			resultset = statement.executeQuery();
			while(resultset.next()) {
				idList.add(resultset.getInt("id"));
			}
			idArray = new int[idList.size()];
			for(int i=0; i<idList.size(); i++) {
				idArray[i] = idList.get(i);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return idArray;
	}

	@Override
	public int getAllValidatedSongCount() {
		int count = 0;
		
		query = "SELECT COUNT(*) FROM song_validated;";
		try {
			statement = connection.prepareStatement(query);
			resultset = statement.executeQuery();
			if(resultset.next()) {
				count = resultset.getInt(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
		return count;
	}
	
}
