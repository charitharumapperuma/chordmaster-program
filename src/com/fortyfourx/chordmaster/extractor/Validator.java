package com.fortyfourx.chordmaster.extractor;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class Validator implements Runnable {
	public static final String ARTISTS_URL = "http://www.chords-lanka.com/artists.php";
	
	private WebDriver driver;
	private DatabaseHandler dbhandler;
	private String activeUrl;
	
	public Validator(String url) {
		this.activeUrl = url;
	}
	
	public void validateArtists() {
		try {
			int artistsCountReal = driver.findElements(By.cssSelector("span.artits_names > a")).size();
			int artistsCountStored = this.dbhandler.getAllArtistsCount();
			
			if(artistsCountReal == artistsCountStored) {
				System.out.println("All artists are stored...");
			} else {
				System.out.println("There are " + (artistsCountReal - artistsCountStored) + " new artists...");
			}
		} catch (Exception e) {
			e.printStackTrace(); // TODO
		}
	}

	@Override
	public void run() {
		synchronized (SystemHandler.browserPool) {
			driver = SystemHandler.browserPool.pop();
		}
		
		// driver can be empty if BrowserPool.idlePool is empty
		if (driver != null) {
			dbhandler = new DatabaseHandler();
			
			driver.navigate().to(activeUrl);
			
			if (activeUrl.contains(SystemHandler.ARTISTS_PAGE)) {
				validateArtists();
			} else if(activeUrl.contains(SystemHandler.SEARCH_ARTIST_PAGE)) {
				//readAllSongs();
			} else if(activeUrl.contains(SystemHandler.SONG_VIEW_PAGE)) {
				//readSong();
			}
			
			this.dbhandler.close();
			
			synchronized (SystemHandler.browserPool) {
				SystemHandler.browserPool.push(driver);
			}
		}
	}
}
