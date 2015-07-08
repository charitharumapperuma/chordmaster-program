package com.fortyfourx.chordmaster.extractor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fortyfourx.entity.Artist;
import com.fortyfourx.entity.Error;
import com.fortyfourx.entity.Song;
import com.fortyfourx.entity.URL;

public class DatabaseHandler {
	public static final String DRIVER = "com.mysql.jdbc.Driver";

	public static final String DATABASE = "jdbc:mysql://localhost/chordmaster.v2";
	public static final String USERNAME = "root";
	public static final String PASSWORD = "";

	private Connection connection;
	private PreparedStatement statement;
	private ResultSet resultset;
	private String query;

	public DatabaseHandler() {
		try {
			Class.forName(DatabaseHandler.DRIVER);
			connection = DriverManager.getConnection(DATABASE, USERNAME,
					PASSWORD);
		} catch (Exception e) {
			e.printStackTrace(); // TODO
		}
	}
	
	public void close() {
		try {
			if (statement != null)
				statement.close();
			if (connection != null)
				connection.close();
		} catch (Exception e) {
			e.printStackTrace(); // TODO
		}
	}
	
	/*----------------- START ARTIST CRUD --------------------*/
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
	
	public int getAllArtistsCount() {
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
	/*----------------- END ARTIST CRUD --------------------*/
	
	/*----------------- START SONG CRUD --------------------*/
	public Song getSongById(int id) {
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
					this.getArtistById(resultset.getInt("artist"))
				);
			}
		} catch (Exception e) {
			e.printStackTrace(); // TODO
		}
		return null;
	}

	public List<Song> getSongsByName(String title) {
		query = "SELECT * FROM song WHERE song.title LIKE %?%;";
		List<Song> songs = new ArrayList<Song>();
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
						this.getArtistById(resultset.getInt("artist"))
					)
				);
			}
			return songs;
		} catch (Exception e) {
			e.printStackTrace(); // TODO
		}
		return null;
	}

	public List<Song> getAllSongs() {
		query = "SELECT * FROM song;";
		List<Song> songs = new ArrayList<Song>();
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
						this.getArtistById(resultset.getInt("artist"))
					)
				);
			}
			return songs;
		} catch (Exception e) {
			e.printStackTrace(); // TODO
		}
		return null;
	}
	
	public List<Song> getSongsByArtist(Artist artist) {
		query = "SELECT * FROM song WHERE song.artist = ?";
		List<Song> songs = new ArrayList<Song>();
		
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
						this.getArtistById(resultset.getInt("artist"))
					)
				);
			}
			return songs;
		} catch (Exception e) {
			e.printStackTrace(); // TODO
		}
		return null;
	}
	
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
	/*----------------- END SONG CRUD --------------------*/
	

	/*---------------------- START URL CRUD ----------------------*/
	public URL getURL(String url) {
		query = "SELECT * FROM url WHERE url.url = ?;";
		try {
			statement = connection.prepareStatement(query);
			statement.setString(1, url);
			resultset = statement.executeQuery();
			if(resultset.next()){
				return new URL(resultset.getString("url"), resultset.getInt("status"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public List<URL> getAllUrls() {
		List<URL> urls = new ArrayList<URL>();
		query = "SELECT * FROM url;";
		try {
			statement = connection.prepareStatement(query);
			resultset = statement.executeQuery();
			while(resultset.next()) {
				urls.add(new URL(
							resultset.getString("url"),
							resultset.getInt("status")
						)
					);
			}
			return urls;
		} catch (Exception e) {
			e.printStackTrace(); // TODO
		}
		return null;
	}
	
	public List<String> getIncompleteUrls() {
		List<String> urls = new ArrayList<String>();
		query = "SELECT url FROM url WHERE url.status != ?;";
		try {
			statement = connection.prepareStatement(query);
			statement.setInt(1, Scraper.URL_STATUS_ALL);
			resultset = statement.executeQuery();
			while (resultset.next()) {
				urls.add(resultset.getString("url"));
			}
			return urls;
		} catch(Exception e) {
			e.printStackTrace(); // TODOs
		}
		return null;
	}
	
	public int getUrlStatus(String url) {
		query = "SELECT status FROM url WHERE url.url = ?;";
		try {
			statement = connection.prepareStatement(query);
			statement.setString(1, url);
			resultset = statement.executeQuery();
			if(resultset.next()) {
				return resultset.getInt("status");
			}
		} catch(Exception e) {
			e.printStackTrace(); // TODO
		}
		return 0;
	}

	public String addUrl(String url) {
		query = "SELECT count(*) FROM url WHERE url.url = ?;";
		try {
			statement = connection.prepareStatement(query);
			statement.setString(1, url);
			resultset = statement.executeQuery();
			resultset.next();
			if (resultset.getInt(1) == 0) { 
				query = "INSERT INTO url(url) VALUES(?);";
				
				statement = connection.prepareStatement(query);
				statement.setString(1, url);
				statement.executeUpdate();
			}
			return url;
		} catch (Exception e) {
			e.printStackTrace(); // TODO
		}
		return null;
	}
	
	public void updateUrl(String url, int status) {
		query = "UPDATE url SET url.status = ? WHERE url.url = ?;";
		try {
			statement = connection.prepareStatement(query);
			statement.setInt(1, status);
			statement.setString(2, url);
			statement.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace(); // TODO
		}
	}
	/*--------------------- END URL CRUD -----------------*/
	
	/*----------------- START ERROR CRUD -------------*/
	public Error getError(String url) {
		query = "SELECT * FROM error WHERE error.url = ?;";
		try {
			statement = connection.prepareStatement(query);
			statement.setString(1, url);
			resultset = statement.executeQuery();
			if(resultset.next()){
				return new Error(
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

	public List<String> getAllErrorUrls() {
		List<String> exceptionUrls = new ArrayList<String>();
		query = "SELECT url FROM error;";
		try {
			statement = connection.prepareStatement(query);
			resultset = statement.executeQuery();
			while (resultset.next()) {
				exceptionUrls.add(resultset.getString("url"));
			}
			// Delete retrieved records
			// Error records are saved to management purposes.
			// so, Truncating table is omitted.
			// query = "TRUNCATE error;";
			// statement = connection.prepareStatement(query);
			// statement.execute();
			
			return exceptionUrls;
		} catch (Exception e) {
			e.printStackTrace(); // TODO
		}
		return null;
	}
	
	public void addError(String url, String exception) {
		Date dt = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String currentTime = sdf.format(dt);
		
		query = "INSERT INTO error(timestamp, url, exception) VALUES(?,?,?);";
		try {
			statement = connection.prepareStatement(query);
			statement.setString(1, currentTime);
			statement.setString(2, url);
			statement.setString(3, exception);
			statement.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace(); // TODO
		}
	}
	
	public Error removeError(String url) {
		Error error = null;
		query = "SELECT * FROM error WHERE error.url = ?;";
		try {
			statement = connection.prepareStatement(query);
			statement.setString(1, url);
			resultset = statement.executeQuery();
			int id;
			if(resultset.next()){
				id = resultset.getInt("id");
				error = new Error(
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
			
			return error;
		} catch (Exception e) {
			e.printStackTrace(); // TODO
		}
		return null;
	}
	/*-------------------- END EXCEPTION CRUD --------------------*/
}
