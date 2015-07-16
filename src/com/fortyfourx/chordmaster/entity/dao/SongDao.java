package com.fortyfourx.chordmaster.entity.dao;

import java.util.List;
import java.util.Map;

import com.fortyfourx.chordmaster.entity.Artist;
import com.fortyfourx.chordmaster.entity.Song;

/**
 * @author Charith Arumapperuma
 * <p>
 * The DAO interface for {@link Song} entity. Holds methods to read, write and update 
 * {@code DATABASE.SONG} table.
 */
public interface SongDao {
	/**
	 * Returns the total number of songs in the {@code DATABASE.SONG} table.
	 * @return			Number of songs as integer.
	 */
	public int 			getAllSongsCount();
	
	/**
	 * Returns a {@link Song} that is identified by {@code id}. This method must be 
	 * implemented to execute an SQL query to select song by id. Returns null if no 
	 * such song with the given id is found. 
	 * <p>
	 * @param id		Id of the song that needs to be retrieved.
	 * @return			One {@link Song} instance or null.
	 */
	public Song			getSongById(int id);
	
	/**
	 * Returns a {@link List} of {@link Song} instances that matches its title or a 
	 * portion of the title to the given character sequence. This method must be 
	 * implemented to execute an SQL query to search elements by title. 
	 * <p>
	 * @param name		Character sequence to be searched for.
	 * @return			A {@link List} of {@link Song} elements. 
	 */
	public List<Song>	getSongsByTitle(String title);
	
	/**
	 * Returns a {@link List} of {@link Song} instances that the {@link Artist} passed 
	 * as the parameter has sung. This method must be implemented to execute SQL query 
	 * to search songs by artist id.
	 * <p>
	 * @param artist	{@link Artist} that needed to be searched.
	 * @return			A {@link List} of {@link Song} elements.
	 */
	public List<Song>	getSongsByArtist(Artist artist);
	
	/**
	 * Returns a {@link Map} of count as Integer per {@link Artist}. The count specifies the 
	 * amount of songs available by the artist in the key.
	 * @return			A {@link Map} of {@link Artist}, Integer pairs.
	 */
	public Map<Artist, Integer> getAllSongsPerArtist();
	
	/**
	 * Returns all the songs stored in the database as a {@link List} of {@link Song} instances.
	 * <p>
	 * @return			A {@link List} of {@link Song} instances.
	 */
	public List<Song>	getAllSongs();

	/**
	 * Stores a song to the database. Must implement INSERT query for the song table. 
	 * Method also checks whether song is already present in the database and if 
	 * present, add any missing parameters of the {@link Song} instance passed.
	 * Method returns the {@link Song} instance in its original state if it was not 
	 * present in the database. If the instance was present in the database and the 
	 * passed instance got updated with database data, the newly updated {@link Song} 
	 * instance will be returned. In case of a failure to store song, {@code null} 
	 * will be returned.
	 * <p>
	 * @param song		Song instance needed to be added to the database.
	 * @return			The same song instance, or updated instance or {@code null}.
	 */
	public Song			addSong(Song song);

}
