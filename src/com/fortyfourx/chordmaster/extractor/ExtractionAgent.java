package com.fortyfourx.chordmaster.extractor;

import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.fortyfourx.chordmaster.exception.PoolBusyException;
import com.fortyfourx.chordmaster.pool.SingletonDatabaseConnectionPool;
import com.fortyfourx.chordmaster.pool.SingletonProgramCache;
import com.fortyfourx.chordmaster.pool.SingletonWebDriverPool;

public class ExtractionAgent {
	public static final int		CORE_POOL_SIZE	= 7;
	public static final int		MAX_POOL_SIZE	= 7;
	public static final int		KEEP_ALIVE_TIME	= 60;
	public static final int 	MAX_QUEUE_LIMIT = 100;
	
	public static final String	ARTISTS_PAGE_URL				= "http://www.chords-lanka.com/artists.php";
	public static final String	TEST_PAGE_URL					= "http://www.chords-lanka.com/song_view.php?song_id=1000";
	public static final String	ARTISTS_PAGE_IDENTIFIER			= "/artists.php";
	public static final String	SEARCH_ARTIST_PAGE_IDENTIFIER	= "/search_artist.php";
	public static final String	SONG_VIEW_PAGE_IDENTIFIER		= "/song_view.php";
	
	public static final String	DETAILS_DIR_PATH				= "./song/details/";
	public static final String	LYRICS_DIR_PATH					= "./song/lyrics/";
	
	private BlockingQueue<Runnable> queue;
	private ExecutorService executor;
	
	public static void main(String[] args) {
		// To track execution time.
		long startTime = System.currentTimeMillis();
		
		// Status output.
		System.out.println("Agent started...");
		
		ExtractionAgent agent = new ExtractionAgent();
		
		agent.queue = new LinkedBlockingQueue<Runnable>();
		agent.executor = new ThreadPoolExecutor(
								ExtractionAgent.CORE_POOL_SIZE,
								ExtractionAgent.MAX_POOL_SIZE,
								ExtractionAgent.KEEP_ALIVE_TIME,
								TimeUnit.MINUTES,
								agent.queue);
		
		String url;
		ContentCollector collector;
		
		//SingletonProgramCache.getInstance().addCommonUrl(ExtractionAgent.ARTISTS_PAGE_URL);
		SingletonProgramCache.getInstance().addCommonUrl(ExtractionAgent.ARTISTS_PAGE_URL);
		
		while (true) {
			
			// Get next URL for execution.
			try {
				if (agent.queue.size() < ExtractionAgent.MAX_QUEUE_LIMIT) {
					url = SingletonProgramCache.getInstance().nextCommonUrl();
					collector = new ContentCollector(url);
					agent.executor.execute(collector);
				}
			} catch (IndexOutOfBoundsException ie) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			if (SingletonWebDriverPool.getInstance().isExpired()) {
				System.out.println("SingletonWebDriverPool is expired.");
				if (SingletonProgramCache.getInstance().isEmpty()) {
					System.out.println("SystemCache is empty.");
					if (SingletonWebDriverPool.getInstance().isIdle()) {
						System.out.println("SingletonWebDriverPool is idle");
						break;
					}
				}
			}
		}
		
		// Closing pools.
		try {
			SingletonDatabaseConnectionPool.getInstance().closeAll();
			SingletonWebDriverPool.getInstance().closeAll();
		} catch (ClassNotFoundException | PoolBusyException | SQLException e) {
			e.printStackTrace();
		}
		
		agent.executor.shutdown();
		
		// Status output.
		System.out.println("Agent started...");		
		
		// Log execution time.
		long endTime = System.currentTimeMillis();
		long execTime = startTime - endTime;
		long minutes = TimeUnit.MILLISECONDS.toMinutes(execTime);
		long seconds = TimeUnit.MILLISECONDS.toSeconds(execTime) - TimeUnit.MINUTES.toSeconds(minutes);
		System.out.println("Process ended succesfully in " + minutes + " minutes, " + seconds + " seconds...");
	}
}
