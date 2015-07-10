package com.fortyfourx.chordmaster.extractor;

import java.util.ArrayList;
import java.util.List;

import com.fortyfourx.chordmaster.entity.Artist;
import com.fortyfourx.chordmaster.entity.NewSong;

/**
 * @author Charith Arumapperuma
 * <p>
 * Uses singleton design pattern to implement program cache which holds cached data 
 * and methods that are necessary to program's functionality. Mainly, cached 
 * {@link Artist} and {@link NewSong} {@link List}s are managed to collect data from 
 * Internet.
 * <p>
 * <i>Singleton Design Pattern Example - http://crunchify.com/thread-safe-and-a-fast-singleton-implementation-in-java/<i>
 */
public class SingletonProgramCache {
	// Only instance of this class.
	private static SingletonProgramCache instance;
	
	// Class properties.
	private List<String> artistUrls; // Holds all artist urls before checking.
	private List<String> songUrls; // Holds all song urls before checking.
	private List<NewSong> newSongs; // Holds all identified new songs before data collecting.

	/**
	 * Prevents creation of new instances from other objects.
	 */
	private SingletonProgramCache() {
		artistUrls = new ArrayList<String>();
		songUrls = new ArrayList<String>();
		newSongs = new ArrayList<NewSong>();
	}
	
	/**
	 * Return the only instance of {@link SingletonProgramCache} class.
	 * <p>
	 * @return			return already created class instance.
	 */
	public SingletonProgramCache getInstance() {
		if (instance == null) {
			synchronized (SingletonDatabaseConnectionPool.class) {
				if (instance == null) {
					instance = new SingletonProgramCache();
				}
			}
		}
				
		return instance;
	}
	
	/**
	 * Adds one URL to {@link #artistUrls}.
	 * @param url			URL to be added as a String.
	 */
	public void addArtistUrl(String url) {
		this.artistUrls.add(url);
	}
	
	/**
	 * Returns the top most element in {@link #artistUrls} list.
	 * @return				Next artist URL in the cache as String.
	 * @throws IndexOutOfBoundsException Requesting new elements must be stopped.
	 */
	public synchronized String nextArtistUrl() throws IndexOutOfBoundsException {
		return this.artistUrls.remove(0);
	}
	
	/**
	 * Adds one URL to {@link #songUrls}.
	 * @param url			URL to be added as a String.
	 */
	public void addSongUrl(String url) {
		this.songUrls.add(url);
	}

	/**
	 * Returns the top most element in {@link #songUrls} list.
	 * @return				Next song URL in the cache as String.
	 * @throws IndexOutOfBoundsException Requesting new elements must be stopped.
	 */
	public synchronized String nextSongUrl() throws IndexOutOfBoundsException{
		return this.songUrls.remove(0);
	}
	
	/**
	 * Adds one {@link NewSong} to {@link #songUrls}.
	 * @param newSong		{@link NewSong} to be added.
	 */
	public void addNewSong(NewSong newSong) {
		this.newSongs.add(newSong);
	}
	
	/**
	 * Return the top most element in {@link #newSongs} list.
	 * @return				Next {@link NewSong} object in the cache.
	 * @throws IndexOutOfBoundsException Requesting new elements must be stopped.
	 */
	public synchronized NewSong nextNewSong() throws IndexOutOfBoundsException {
		return this.newSongs.remove(0);
	}
}
