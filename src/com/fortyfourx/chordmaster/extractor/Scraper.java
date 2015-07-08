package com.fortyfourx.chordmaster.extractor;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * @author Charith Arumapperuma
 *
 * <i>Error logging example - http://stackoverflow.com/questions/14606293/java-logging-exceptions-how-to-log-as-much-information-as-possible</i>
 * <i>Element screenshots example - http://stackoverflow.com/questions/13832322/how-to-capture-the-screenshot-of-only-a-specific-element-using-selenium-webdrive</i>
 * */
public class Scraper implements Runnable {
	public static final String ARTISTS_PAGE_ARTISTS_CSS_SELECTOR = ".artits_names > a";
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
	private DatabaseHandler dbhandler;
	private String activeUrl;

	/**
	 * {@link Scraper} constructor. Stores parameter (@code url} to {@link #activeUrl}.
	 * <p>
	 * @param 	url			The URL to be scraped
	 */
	public Scraper(String url) {
		this.activeUrl = url;
	}
	
	/**
	 * Checks whether the element referenced by {@code cssSelector} is available 
	 * in the current web page which the driver is navigated to. 
	 * <p>
	 * @param	cssSelector	CSS selector String for the element need to be checked.
	 * @return 				true or false depending on whether element is exists or not.
	 */
	public boolean hasElement(String cssSelector) {
		try {
			// Raises an exception when there is no such element.
			driver.findElement(By.cssSelector(cssSelector));
		} catch (Exception e) {
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
	 * Searches the WebElement that contains song lyrics.
	 * The search uses (@link {@link #SONG_PAGE_LYRICS_CSS_SELECTOR} array to find WebElement by CSS selector.
	 * <p>
	 * @return				WebElement that contains the lyrics or null if there is no such element.
	 * <p>
	 * @see #hasElement(String)
	 * @see #isElementEmpty(WebElement)
	 */
	public WebElement getLyricsElement() {
		WebElement element = null;
		// Loop all CSS selectors of Scraper.SONG_PAGE_LYRICS_SELECTOR_CSS.
		for(String selector : Scraper.SONG_PAGE_LYRICS_CSS_SELECTOR) {
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
	 * Reads all artists from the web page in the URL specified by {@link #activeUrl} field. 
	 * Uses {@link #ARTISTS_PAGE_ARTISTS_CSS_SELECTOR} to select elements by CSS selector.
	 * Stores scraped data to <b>DATABASE.ARTIST</b> table.
	 * Finally, add all URLs to {@link SystemHandler#urlPool}.
	 * If exceptions are raised, current time, {@link #activeUrl} and the exception will be stored into <b>DATABASE.ERROR</b> table.
	 * <p>
	 * @see SystemHandler
	 * @see DatabaseHandler
	 * @see Artist
	 */
	public void readAllArtists() {
		try {
			// Get all elements that Scraper.ARTISTS_PAGE_ARTISTS_CSS_SELECTOR satisfies.
			List<WebElement> artistAnchors = driver.findElements(By.cssSelector(Scraper.ARTISTS_PAGE_ARTISTS_CSS_SELECTOR));
			// Loop all elements (<a> tags).
			for (WebElement artistAnchor : artistAnchors) {
				// Add new artist to the database
				dbhandler.addArtist(
					new Artist(
						artistAnchor.getText(), 
						artistAnchor.getAttribute("href")
					)
				);
				// Add new artist's URL to URL pool
				synchronized (SystemHandler.urlPool) {
					SystemHandler.urlPool.add(artistAnchor.getAttribute("href"));
				}
			}
		} catch (Exception e) {
			// Log error for maintenance purposes
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			String exceptionDetails = sw.toString();
			dbhandler.addError(activeUrl, exceptionDetails);
		}
	}
	
	/**
	 * Reads all song URLs from the web page in the URL specified by {@link #activeUrl} field. 
	 * Uses {@link #ARTIST_PAGE_SONGS_CSS_SELECTOR} to select elements by CSS selector. 
	 * Stores scraped URL to <b>DATABASE.URL</b> table. 
	 * Add all URLs that satisfy any of the following conditions to {@link SystemHandler#urlPool}, 
	 * <ul>
	 * <li>Should not be in the database already.</li>
	 * <li>If URL is present in the database it should not have status 3.</li>
	 * <li>The URL is present in the errors table.</li>
	 * </ul>
	 * <p>
	 * If exceptions are raised, current time, {@link #activeUrl} and the exception will be stored into <b>DATABASE.ERROR</b> table. 
	 * <p>
	 * @see SystemHandler
	 * @see DatabaseHandler
	 * @see Url
	 */
	public void readAllSongs() {
		try {
			// Get all elements that Scraper.ARTIST_PAGE_SONGS_CSS_SELECTOR satisfies. 
			List<WebElement> songAnchors = driver.findElements(By.cssSelector(Scraper.ARTIST_PAGE_SONGS_CSS_SELECTOR));
			
			String songAnchorUrl;
			// Loop all song elements (<a> tags).
			for (WebElement songAnchor : songAnchors) {
				// Get URL of each <a> tag
				songAnchorUrl = songAnchor.getAttribute("href");
				if ((dbhandler.getURL(songAnchorUrl) == null) || (dbhandler.getUrlStatus(songAnchorUrl) != 3) || (dbhandler.removeError(songAnchorUrl) != null)) {
					// If the conditions (see java doc of Scraper.readAllSongs()) are met, add to URL pool
					synchronized (SystemHandler.urlPool) {
						SystemHandler.urlPool.add(songAnchor.getAttribute("href"));
					}
				}
				// Add to DATABASE.URL table
				dbhandler.addUrl(songAnchor.getAttribute("href"));
			}
		} catch (Exception e) {
			// Log error for maintenance purposes
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			String exceptionDetails = sw.toString();
			dbhandler.addError(activeUrl, exceptionDetails);
		}
	}
	
	/**
	 * Reads song details, lyrics and capture screenshots 
	 * from the web page in the URL specified by {@link #activeUrl} field. 
	 * Uses {@link #getLyricsElement()} to select lyrics by CSS selector. 
	 * Stores scraped URL to <b>DATABASE.SONG</b> table. 
	 * If there are new artists found in this process, those are added to <b>DATABASE.ARTIST</b> table. 
	 * <p>
	 * If exceptions are raised, current time, {@link #activeUrl} and the exception will be stored into <b>DATABASE.ERROR</b> table. 
	 * <p>
	 * @see SystemHandler
	 * @see DatabaseHandler
	 * @see Artist
	 */
	public void readSong() {
		int status = 0; // 0=nothing, 1=text, 2=text+details image, 3=text+details image+lyrics image
		try {
			// Get all elements that Scraper.ARTIST_PAGE_SONGS_CSS_SELECTOR satisfies.
			List<WebElement> songDetailsTableRows = driver.findElements(By.cssSelector(Scraper.SONG_PAGE_DETAILS_CSS_SELECTOR));
			// Get lyrics.
			WebElement lyrics = this.getLyricsElement();
			
			// Current process status output.
			System.out.println("Reading song in page " + activeUrl + " using browser " + driver.hashCode() + "...");
		
			// Add new song to the database.
			dbhandler.addSong(
					new Song(
						Integer.parseInt(activeUrl.split("=")[1]), 
						songDetailsTableRows.get(1).findElement(By.cssSelector("td:last-child")).getText().split("-")[1].trim(), 
						activeUrl, 
						lyrics.getAttribute("outerHTML"), 
						songDetailsTableRows.get(3).findElement(By.cssSelector("td:last-child")).getText().split("-")[1].trim(), 
						songDetailsTableRows.get(2).findElement(By.cssSelector("td:last-child")).getText().split("-")[1].trim(), 
						dbhandler.addArtist(
								new Artist(
									songDetailsTableRows.get(0).findElement(By.cssSelector("td:last-child")).getText().split("-")[1].split("\\(")[0].trim(), 
									songDetailsTableRows.get(0).findElement(By.cssSelector("td:last-child > i > a")).getAttribute("href")
								)
							)
					)
				);
			// Set status to 1 since song is successfully saved to database.
			status = 1;
			captureSongDetails();
			// Set status to 2 since song details are captured successfully.
			status = 2;
			captureSongLyrics();
			// Set status to 3 since all information about song is saved.
			status = 3;
			
			// Current process status output.
			System.out.println("completed reading song in page " + activeUrl + "...");
		} catch (Exception e) {
			// Log error for maintenance purposes
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			String exceptionDetails = sw.toString();
			dbhandler.addError(activeUrl, exceptionDetails);
		} finally {
			// Set URL status
			dbhandler.updateUrl(activeUrl, status); // UPDATE
		}
	}
	
	/**
	 * Captures a screenshot of the song details element. 
	 * First, it will capture a full screenshot of the site and then read dimensions 
	 * and location of the details element. The original screenshot is then cropped to 
	 * generate the element screenshot.
	 */
	public void captureSongDetails() {
		// Get 
		WebElement detailsTable = driver.findElement(By.cssSelector(Scraper.SONG_PAGE_DETAILS_GROUP_CSS_SELECTOR));
		File screenshot = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
		try {
			BufferedImage fullScreenshot = ImageIO.read(screenshot);
			Point point = detailsTable.getLocation();
			int width = detailsTable.getSize().getWidth();
			int height = detailsTable.getSize().getHeight();
			BufferedImage eleScreenshot= fullScreenshot.getSubimage(point.getX(), point.getY(), width, height);
			ImageIO.write(eleScreenshot, "png", screenshot);
			FileUtils.copyFile(screenshot, new File(SystemHandler.DETAILS_FOLDER_PATH + Integer.parseInt(activeUrl.split("=")[1]) + ".png"));
		} catch (IOException e) {
			e.printStackTrace(); // TODO
		}
	}
	
	// Screenshot reference: http://stackoverflow.com/questions/13832322/how-to-capture-the-screenshot-of-only-a-specific-element-using-selenium-webdrive
public boolean captureSongLyrics() {
		WebElement lyrics = this.getLyricsElement();
		File screenshot = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
		try {
			BufferedImage fullScreenshot = ImageIO.read(screenshot);
			Point point = lyrics.getLocation();
			int width = lyrics.getSize().getWidth();
			int height = lyrics.getSize().getHeight();
			BufferedImage eleScreenshot= fullScreenshot.getSubimage(point.getX(), point.getY(), width, height);
			ImageIO.write(eleScreenshot, "png", screenshot);
			FileUtils.copyFile(screenshot, new File(SystemHandler.LYRICS_FOLDER_PATH + Integer.parseInt(activeUrl.split("=")[1]) + ".png"));
			
			return true;
		} catch (IOException e) {
			e.printStackTrace(); // TODO
		}
		
		return false;
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
				readAllArtists();
			} else if(activeUrl.contains(SystemHandler.SEARCH_ARTIST_PAGE)) {
				readAllSongs();
			} else if(activeUrl.contains(SystemHandler.SONG_VIEW_PAGE)) {
				readSong();
			}
			
			this.dbhandler.close();
			
			synchronized (SystemHandler.browserPool) {
				SystemHandler.browserPool.push(driver);
			}
		}
	}
	
}
