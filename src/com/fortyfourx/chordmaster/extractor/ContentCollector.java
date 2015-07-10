package com.fortyfourx.chordmaster.extractor;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.jasper.tagplugins.jstl.core.Url;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.fortyfourx.chordmaster.entity.Artist;
import com.fortyfourx.chordmaster.entity.IncompleteSong;
import com.fortyfourx.chordmaster.entity.NewSong;
import com.fortyfourx.chordmaster.entity.Song;
import com.fortyfourx.chordmaster.entity.dao.ArtistDao;
import com.fortyfourx.chordmaster.entity.dao.IncompleteSongDao;
import com.fortyfourx.chordmaster.entity.dao.NewSongDao;
import com.fortyfourx.chordmaster.entity.dao.SongDao;
import com.fortyfourx.chordmaster.entity.dao.impl.ArtistDaoImpl;
import com.fortyfourx.chordmaster.entity.dao.impl.IncompleteSongDaoImpl;
import com.fortyfourx.chordmaster.entity.dao.impl.NewSongDaoImpl;
import com.fortyfourx.chordmaster.entity.dao.impl.SongDaoImpl;
import com.fortyfourx.chordmaster.exception.PoolEmptyException;
import com.fortyfourx.chordmaster.exception.PoolFullException;
import com.fortyfourx.chordmaster.exception.PooledObjectNotFoundException;

/**
 * @author Charith Arumapperuma
 * <p>
 * Handles all data manipulation tasks done for web sites. Reading web site content, 
 * taking screenshots and passing these data to DatabaseHandler to store are done 
 * here. 
 * <p>
 * <i>Error logging example - http://stackoverflow.com/questions/14606293/java-logging-exceptions-how-to-log-as-much-information-as-possible</i>
 * <i>Element screenshots example - http://stackoverflow.com/questions/13832322/how-to-capture-the-screenshot-of-only-a-specific-element-using-selenium-webdrive</i>
 * */
public class ContentCollector implements Runnable {
	// CSS selectors.
	public static final String ARTISTS_PAGE_ARTISTS_CSS_SELECTOR = ".artits_names > a";
	public static final String ARTIST_PAGE_ARTIST_CSS_SELECTOR = "#right > h3";
	public static final String ARTIST_PAGE_SONGS_CSS_SELECTOR = "#search_results_display > a";
	public static final String SONG_PAGE_DETAILS_CSS_SELECTOR = "#song_details > table > tbody > tr";
	public static final String SONG_PAGE_DETAILS_GROUP_CSS_SELECTOR = "#song_details > table";
	public static final String[] SONG_PAGE_LYRICS_CSS_SELECTOR = {
		"#song_display > p.songs_lyrics",
		"#song_display > pre[transpose-ref$=\"_inParts\"]",
		"#song_display_tabs > pre",
		"#song_display > pre",
		"#sinhala_display",
		"#song_display_tabs > p.songs_lyrics"
	};
	
	private WebDriver driver;
	private Connection connection;
	private String activeUrl;

	/**
	 * {@link ContentCollector} constructor.
	 * <p>
	 * @param 	url			The URL to visit.
	 */
	public ContentCollector(String url) {
		this.activeUrl = url;
	}
	
	/**
	 * Checks whether the element referenced by {@code cssSelector} is available in 
	 * the current web page which the driver is navigated to. 
	 * <p>
	 * @param	cssSelector	CSS selector String for the element need to be checked.
	 * @return 				true or false depending on whether element is exists or not.
	 */
	public boolean hasElement(String cssSelector) {
		try {
			driver.findElement(By.cssSelector(cssSelector));
		} catch (Exception e) {
			// Raises an exception when there is no such element.
			return false;
		}
		return true;
	}
	
	/**
	 * Checks whether the WebElement in {@code element} has HTML content inside.
	 * <p>
	 * @param	element		WebElement that needs to be checked.
	 * @return				true or false depending on whether {@code element} has content inside or not.
	 */
	public boolean isElementEmpty(WebElement element) {
		if(element.getText().isEmpty()) {
			return true;
		}
		return false;
	}
	
	/**
	 * Searches the WebElement that contains song lyrics. The search uses 
	 * {@link #SONG_PAGE_LYRICS_CSS_SELECTOR} array to find WebElement by 
	 * CSS selector.
	 * <p>
	 * @return				WebElement that contains the lyrics or null if there is no such element.
	 * <p>
	 * @see #hasElement(String)
	 * @see #isElementEmpty(WebElement)
	 */
	public WebElement getLyricsElement() {
		WebElement element = null;
		// Loop all CSS selectors of Scraper.SONG_PAGE_LYRICS_SELECTOR_CSS.
		for(String selector : ContentCollector.SONG_PAGE_LYRICS_CSS_SELECTOR) {
			if(this.hasElement(selector)) {
				// If there is an element from current CSS selector, get it and check whether it is empty or not.
				element = driver.findElement(By.cssSelector(selector));
				if(this.isElementEmpty(element)) {
					// If the found element is empty, continue to the next one.
					element = null;
					continue;
				} else {
					// If the element is not empty, it is the required element. Break the loop.
					break;
				}
			}
		}
		// Return value will be null if element is not found.
		return element;
	}
	
	/**
	 * Common exception logging procedure. Exception is saved to database with a 
	 * unique id, current time, error occurred URL and exception text itself. 
	 * @param e				Raised Exception.
	 */
	public void logException(Exception e) {
		// Log error for maintenance purposes
		IncompleteSongDao incompleteSongDao = new IncompleteSongDaoImpl(connection);
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		String exceptionDetails = sw.toString();
		incompleteSongDao.addIncompleteSong(new IncompleteSong(-1, null, activeUrl, exceptionDetails));
	}
	
	/**
	 * Reads all artists from the web page in the URL specified by {@link #activeUrl} 
	 * field. Uses {@link #ARTISTS_PAGE_ARTISTS_CSS_SELECTOR} to select elements by 
	 * CSS selector. Stores scraped data to <b>DATABASE.ARTIST</b> table. Finally, 
	 * add all URLs to {@link Worker#urlPool}. If exceptions are raised, current 
	 * time, {@link #activeUrl} and the exception will be stored into <b>DATABASE.ERROR</b> 
	 * table.
	 * <p>
	 * @see Worker
	 * @see DatabaseHandler
	 * @see Artist
	 */
	public void readAllArtists() {
		ArtistDao artistDao = new ArtistDaoImpl(connection);
		try {
			// Get all elements that Scraper.ARTISTS_PAGE_ARTISTS_CSS_SELECTOR satisfies.
			List<WebElement> artistAnchors = driver.findElements(By.cssSelector(ContentCollector.ARTISTS_PAGE_ARTISTS_CSS_SELECTOR));
			// Loop all elements (<a> tags).
			for (WebElement artistAnchor : artistAnchors) {
				// Add new artist to the database
				artistDao.addArtist(
					new Artist(
						artistAnchor.getText(), 
						artistAnchor.getAttribute("href")
					)
				);
				// Add new artist's URL to URL pool
				SingletonProgramCache.getInstance().addArtistUrl(artistAnchor.getAttribute("href"));
			}
		} catch (Exception e) {
			this.logException(e);
		}
	}
	
	/**
	 * Reads all song URLs from the web page in the URL specified by {@link #activeUrl} 
	 * field. Uses {@link #ARTIST_PAGE_SONGS_CSS_SELECTOR} to select elements by CSS 
	 * selector. Stores scraped URL to <b>DATABASE.URL</b> table. Add all URLs that 
	 * satisfy any of the following conditions to {@link Worker#urlPool}, 
	 * <ul>
	 * <li>Should not be in the database already.</li>
	 * <li>If URL is present in the database it should not have status 3.</li>
	 * <li>The URL is present in the errors table.</li>
	 * </ul>
	 * <p>
	 * If exceptions are raised, current time, {@link #activeUrl} and the exception 
	 * will be stored into <b>DATABASE.ERROR</b> table. 
	 * <p>
	 * @see Worker
	 * @see DatabaseHandler
	 * @see Url
	 */
	public void readAllSongs() {
		NewSongDao newSongDao = new NewSongDaoImpl(connection);
		try {
			String artist = driver.findElement(By.cssSelector(ContentCollector.ARTIST_PAGE_ARTIST_CSS_SELECTOR)).getText().split("\\(")[0].trim();
			// Get all elements that Scraper.ARTIST_PAGE_SONGS_CSS_SELECTOR satisfies. 
			List<WebElement> songAnchors = driver.findElements(By.cssSelector(ContentCollector.ARTIST_PAGE_SONGS_CSS_SELECTOR));
			
			String songAnchorUrl;
			// Loop all song elements (<a> tags).
			for (WebElement songAnchor : songAnchors) {
				// Get URL of each <a> tag
				songAnchorUrl = songAnchor.getAttribute("href");
				if (newSongDao.getNewSong(songAnchorUrl) == null) {
					// If the conditions (see java doc of Scraper.readAllSongs()) are met, add to URL pool
					SingletonProgramCache.getInstance().addArtistUrl(songAnchor.getAttribute("href"));
				}
				// Add to DATABASE.SONG_NEW table
				newSongDao.addNewSong(
							new NewSong(
								songAnchor.getAttribute("href"), 
								new Artist(
										artist, 
										activeUrl
									)
								)
						);
			}
		} catch (Exception e) {
			this.logException(e);
		}
	}
	
	/**
	 * Reads song details, lyrics and capture screenshots from the web page in the 
	 * URL specified by {@link #activeUrl} field. Uses {@link #getLyricsElement()} 
	 * to select lyrics by CSS selector. Stores scraped URL to <b>DATABASE.SONG</b> 
	 * table. If there are new artists found in this process, those are added to 
	 * <b>DATABASE.ARTIST</b> table. 
	 * <p>
	 * If exceptions are raised, current time, {@link #activeUrl} and the exception 
	 * will be stored into <b>DATABASE.ERROR</b> table. 
	 * <p>
	 * @see Worker
	 * @see DatabaseHandler
	 * @see Artist
	 */
	public void readSong() {
		SongDao songDao = new SongDaoImpl(connection);
		ArtistDao artistDao = new ArtistDaoImpl(connection);
		try {
			// Get all elements that Scraper.ARTIST_PAGE_SONGS_CSS_SELECTOR satisfies.
			List<WebElement> songDetailsTableRows = driver.findElements(By.cssSelector(ContentCollector.SONG_PAGE_DETAILS_CSS_SELECTOR));
			// Get lyrics.
			WebElement lyrics = this.getLyricsElement();
			
			// Current process status output.
			System.out.println("Reading song in page " + activeUrl + " using browser " + driver.hashCode() + "...");
		
			// Add new song to the database.
			songDao.addSong(
					new Song(
						Integer.parseInt(activeUrl.split("=")[1]), 
						songDetailsTableRows.get(1).findElement(By.cssSelector("td:last-child")).getText().split("-")[1].trim(), 
						activeUrl, 
						lyrics.getAttribute("outerHTML"), 
						songDetailsTableRows.get(3).findElement(By.cssSelector("td:last-child")).getText().split("-")[1].trim(), 
						songDetailsTableRows.get(2).findElement(By.cssSelector("td:last-child")).getText().split("-")[1].trim(), 
						artistDao.addArtist(
								new Artist(
									songDetailsTableRows.get(0).findElement(By.cssSelector("td:last-child")).getText().split("-")[1].split("\\(")[0].trim(), 
									songDetailsTableRows.get(0).findElement(By.cssSelector("td:last-child > i > a")).getAttribute("href")
								)
							)
					)
				);
			
			// Current process status output.
			System.out.println("completed reading song in page " + activeUrl + "...");
		} catch (Exception e) {
			this.logException(e);
		}
	}
	
	/**
	 * Captures a screenshot of the song details group and song lyrics. First, it will 
	 * capture a full screenshot of the site and then read dimensions and location of 
	 * the details element. The original screenshot is then cropped to generate the 
	 * element screenshot.
	 */
	public void captureElements() {
		// Get song details table and lyrics element.
		WebElement detailsTable = driver.findElement(By.cssSelector(ContentCollector.SONG_PAGE_DETAILS_GROUP_CSS_SELECTOR));
		WebElement lyrics = this.getLyricsElement();
		
		// Create File to store cropped image before saving.
		File screenshot = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
		
		try {
			// Convert file to a image.
			BufferedImage fullScreenshot = ImageIO.read(screenshot);
		
			// Get element location and dimensions.
			Point detailsPoint = detailsTable.getLocation();
			Point lyricsPoint = lyrics.getLocation();
			int detailsWidth = detailsTable.getSize().getWidth();
			int detailsHeight = detailsTable.getSize().getHeight();
			int lyricsWidth = lyrics.getSize().getWidth();
			int lyricsHeight = lyrics.getSize().getHeight();
			
			// Crop images.
			BufferedImage detailsScreenshot = fullScreenshot.getSubimage(detailsPoint.getX(), detailsPoint.getY(), detailsWidth, detailsHeight);
			BufferedImage lyricsScreenshot = fullScreenshot.getSubimage(lyricsPoint.getX(), lyricsPoint.getY(), lyricsWidth, lyricsHeight);
			
			// Write cropped images to a file.
			ImageIO.write(detailsScreenshot, "png", screenshot);
			FileUtils.copyFile(screenshot, new File(Worker.DETAILS_FOLDER_PATH + Integer.parseInt(activeUrl.split("=")[1]) + ".png"));
			ImageIO.write(lyricsScreenshot, "png", screenshot);
			FileUtils.copyFile(screenshot, new File(Worker.LYRICS_FOLDER_PATH + Integer.parseInt(activeUrl.split("=")[1]) + ".png"));
		} catch (IOException e) {
			e.printStackTrace(); // TODO
		}
	}
	
	/**
	 * Run method to handle this Runnable. Uses a WebDriver borrowed from 
	 * {@link browserPool} to visit web pages. Also uses a {@link DatabaseHandler} 
	 * to access database functions. The tasks are assigned by filtering the value 
	 * of {@link ContentCollector#activeUrl}.  
	 * <p>
	 * @see Worker
	 * @see DatabaseHandler
	 */
	@Override
	public void run() {
		try {
			// Get a WebDriver from browser pool.
			driver = SingletonWebDriverPool.getInstance().pop();
			// Get a Connection from connection pool.
			connection = SingletonDatabaseConnectionPool.getInstance().pop();
			
			// Visit URL.
			driver.navigate().to(activeUrl);
			
			if (activeUrl.contains(Worker.ARTISTS_PAGE)) {
				// Example URL - http://www.chords-lanka.com/artists.php
				readAllArtists();
			} else if(activeUrl.contains(Worker.SEARCH_ARTIST_PAGE)) {
				// Example URL - http://www.chords-lanka.com/search_artist.php?artist=Sunil%20Edirisinghe
				readAllSongs();
			} else if(activeUrl.contains(Worker.SONG_VIEW_PAGE)) {
				// Example URL - http://http://www.chords-lanka.com/song_view.php?song_id=21
				readSong();
			}
			
			// Return database Connection instance.
			SingletonDatabaseConnectionPool.getInstance().push(this.connection);
			
			// Return WebDriver instance.
			SingletonWebDriverPool.getInstance().push(this.driver);
			
		} catch (PoolEmptyException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (PooledObjectNotFoundException e) {
			e.printStackTrace();
		} catch (PoolFullException e) {
			e.printStackTrace();
		}
	}
}