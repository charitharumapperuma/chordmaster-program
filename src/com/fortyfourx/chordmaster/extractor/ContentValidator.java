package com.fortyfourx.chordmaster.extractor;

import java.sql.Connection;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.fortyfourx.chordmaster.entity.dao.SongDao;
import com.fortyfourx.chordmaster.entity.dao.impl.SongDaoImpl;
import com.fortyfourx.chordmaster.exception.PoolEmptyException;
import com.fortyfourx.chordmaster.exception.PoolFullException;
import com.fortyfourx.chordmaster.exception.PooledObjectNotFoundException;

public class ContentValidator implements Runnable {
	public static final String ARTISTS_URL = "http://www.chords-lanka.com/artists.php";
	
	private WebDriver driver;
	private Connection connection;
	private String activeUrl;
	
	public ContentValidator(String url) {
		this.activeUrl = url;
	}
	
	public void validateArtists() {
		SongDao songDao = new SongDaoImpl(connection);
		try {
			int artistsCountReal = driver.findElements(By.cssSelector("span.artits_names > a")).size();
			int artistsCountStored = songDao.getAllSongsCount();
			
			if(artistsCountReal > artistsCountStored) {
				System.out.println("There are " + (artistsCountReal - artistsCountStored) + " new artists...");
			} else {
				System.out.println("All artists are stored...");
			}
		} catch (Exception e) {
			e.printStackTrace(); // TODO
		}
	}

	@Override
	public void run() {
		// Get a WebDriver from browser pool.
		try {
			driver = SingletonWebDriverPool.getInstance().pop();
			// Get a connection from connection pool.
			connection = SingletonDatabaseConnectionPool.getInstance().pop();
			
			// Visit URL.
			driver.navigate().to(activeUrl);
			
			if (activeUrl.contains(ExtractionAgent.ARTISTS_PAGE_IDENTIFIER)) {
				validateArtists();
			} else if(activeUrl.contains(ExtractionAgent.SEARCH_ARTIST_PAGE_IDENTIFIER)) {
				//readAllSongs();
			} else if(activeUrl.contains(ExtractionAgent.SONG_VIEW_PAGE_IDENTIFIER)) {
				//readSong();
			}
			
			// Return database Connection instance.
			SingletonDatabaseConnectionPool.getInstance().push(this.connection);
			
			// Return WebDriver instance.
			SingletonWebDriverPool.getInstance().push(this.driver);
		} catch (PoolEmptyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (PooledObjectNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (PoolFullException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
}
