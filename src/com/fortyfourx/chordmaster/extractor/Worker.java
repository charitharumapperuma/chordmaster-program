package com.fortyfourx.chordmaster.extractor;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Worker {
	/**
	 * -1 = Testing 
	 *  0 = From beginning
	 *  1 = Completing missed links 
	 *  2 = Handling error pages 
	 *  3 = Artist Validator
	 */
	public static final int EXECUTION_MODE = 3;
	public static final int BROWSER_POOL_SIZE = 3;
	public static final long MAX_KEEP_ALIVE_TIME = 60; // in minutes
	public static final int MAX_QUEUE_LIMIT = 100;
	public static final String ARTISTS_PAGE = "/artists.php";
	public static final String SEARCH_ARTIST_PAGE = "/search_artist.php";
	public static final String SONG_VIEW_PAGE = "/song_view.php";
	public static final String DETAILS_FOLDER_PATH = "./song/details/";
	public static final String LYRICS_FOLDER_PATH = "./song/lyrics/";
	public static final String ROOT_URL = "http://www.chords-lanka.com/artists.php";
	public static final String TEST_URL = "http://www.chords-lanka.com/song_view.php?song_id=1000";

	public static BrowserPool browserPool;
	public static UrlPool urlPool;
	private ExecutorService executor;
	private BlockingQueue<Runnable> queue;
	private DatabaseHandler dbhandler;

	public void handle() {
		Runnable scraper;

		String url;
		while (true) {
			if (this.queue.size() < Worker.MAX_QUEUE_LIMIT) {
				if (Worker.urlPool.hasNext()) {
					synchronized (Worker.urlPool) {
						url = Worker.urlPool.next();
					}

					scraper = new Scraper(url);
					executor.execute(scraper);
				}
			}

			// Close the loop if the pool becomes idle for more than specified
			// time
			if (Worker.browserPool.isExpired()) {
				break;
			}
		}
	}

	public void validate() {
		Runnable validator;

		String url;
		while (true) {
			if (this.queue.size() < Worker.MAX_QUEUE_LIMIT) {
				if (Worker.urlPool.hasNext()) {
					synchronized (Worker.urlPool) {
						url = Worker.urlPool.next();
					}

					validator = new Validator(url);
					executor.execute(validator);
				}
			}

			// Close the loop if the pool becomes idle for more than specified
			// time
			if (Worker.browserPool.isExpired()) {
				break;
			}
		}
	}
	
	// Execution mode -1
	public void test() {
		System.out.println("Testing started...");
		System.out.println("EXECUTION MODE # = " + Worker.EXECUTION_MODE);

		// Set starting URL for the process
		synchronized (Worker.urlPool) {
			Worker.urlPool.add(Worker.TEST_URL);
		}

		this.handle();

		System.out.println("Testing completed...");
	}

	// Execution mode 0
	public void browseAll() {
		System.out.println("MainHandler started...");
		System.out
				.println("EXECUTION MODE # = " + Worker.EXECUTION_MODE);

		// Set starting URL for the process
		synchronized (Worker.urlPool) {
			Worker.urlPool.add(Worker.ROOT_URL);
		}

		this.handle();

		System.out.println("MainHandler completed...");
	}

	// Execution mode 1
	public void browseIncomplete() {
		System.out.println("MissingPageHandler started...");
		System.out.println("EXECUTION MODE # = " + Worker.EXECUTION_MODE);

		// Set starting URL for the process
		synchronized (Worker.urlPool) {
			Worker.urlPool.addAll(dbhandler.getIncompleteUrls());
		}

		this.handle();

		System.out.println("MissingPageHandler completed...");
	}

	// Execution mode 2
	public void browseErrors() {
		System.out.println("ErrorHandler started...");
		System.out.println("EXECUTION MODE # = " + Worker.EXECUTION_MODE);

		synchronized (Worker.urlPool) {
			urlPool.addAll(dbhandler.getAllErrorUrls());
		}

		this.handle();

		System.out.println("ErrorHandler completed...");
	}

	// Execution mode 3
	public void validateArtist() {
		System.out.println("Artist Validator started...");
		System.out.println("EXECUTION MODE # = " + Worker.EXECUTION_MODE);

		synchronized (Worker.urlPool) {
			urlPool.add(Validator.ARTISTS_URL);
		}

		this.validate();

		System.out.println("Artist Validator completed...");
	}

	// Execution mode 4
	public void browseByArtists() {
		System.out.println("BrowseByArtists started...");
		System.out.println("EXECUTION MODE # = " + Worker.EXECUTION_MODE);

		synchronized (Worker.urlPool) {
			urlPool.addAll(dbhandler.getAllArtistUrls());
		}

		this.handle();

		System.out.println("BrowseByArtists completed...");
	}
	
	public static void main(String[] args) {
		System.out.println("Worker started...");

		long startTime = System.currentTimeMillis();

		Worker worker = new Worker();

		worker.dbhandler = new DatabaseHandler();
		worker.queue = new LinkedBlockingQueue<Runnable>();
		worker.executor = new ThreadPoolExecutor(Worker.BROWSER_POOL_SIZE,
				Worker.BROWSER_POOL_SIZE,
				Worker.MAX_KEEP_ALIVE_TIME, TimeUnit.MINUTES, worker.queue);

		Worker.browserPool = new BrowserPool(
				Worker.BROWSER_POOL_SIZE);
		Worker.urlPool = new UrlPool();

		// Handle different processing methods
		switch (Worker.EXECUTION_MODE) {
		case -1:
			worker.test();
			break;
		case 0:
			worker.browseAll();
			break;
		case 1:
			worker.browseIncomplete();
			break;
		case 2:
			worker.browseErrors();
			break;
		case 3:
			worker.validateArtist();
			break;
		}

		Worker.browserPool.closeAll();
		worker.dbhandler.close();
		worker.executor.shutdown();

		System.out.println("Worker completed...");

		// log execution time
		long endTime = System.currentTimeMillis();
		long execTime = startTime - endTime;
		long minutes = TimeUnit.MILLISECONDS.toMinutes(execTime);
		long seconds = TimeUnit.MILLISECONDS.toSeconds(execTime)
				- TimeUnit.MINUTES.toSeconds(minutes);
		System.out.println("Process ended succesfully in " + minutes
				+ " minutes, " + seconds + " seconds...");
	}

}