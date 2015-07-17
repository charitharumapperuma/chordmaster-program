package com.fortyfourx.chordmaster.entity.dao;

import java.util.List;

import com.fortyfourx.chordmaster.entity.Artist;

/**
 * @author Charith Arumapperuma
 * <p>
 * The DAO interface for {@link Artist} entity. Holds methods to read, write and 
 * update {@code DATABASE.ARTIST} table.
 */
public interface ArtistDao {
	
	/**
	 * Returns an {@link Artist} that is identified by {@code id}. This method must 
	 * be implemented to execute an SQL query to select artist by id. Returns null 
	 * if no such artist with the given id is found. 
	 * <p>
	 * @param id		Id of the artist that needs to be retrieved.
	 * @return			One {@link Artist} instance or null.
	 */
	public Artist 		getArtistById(int id);
	
	/**
	 * Returns a {@link List} of {@link Artist} elements that matches its name or a 
	 * portion of the name to the given character sequence. This method must be 
	 * implemented to execute an SQL query to search elements by name. 
	 * <p>
	 * @param name		Character sequence to be searched for.
	 * @return			A {@link List} of {@link Artist} elements. 
	 */
	public List<Artist> getArtistsByName(String name);
	
	/**
	 * Returns all the artists stored in the database.
	 * <p>
	 * @return			A {@link List} of {@link Artist} elements.
	 */
	public List<Artist> getAllArtists();
	
	/**
	 * Returns URLs of all the artists stored in the database as a list of strings. 
	 * <p>
	 * @return			A {@link List} of {@link Artist} elements.
	 */
	public List<String> getAllArtistUrls();
	
	/**
	 * Returns the count of all artists stored in the database.
	 * <p>
	 * @return			An integer value of the count of artists.
	 */
	public int 			getAllArtistCount();
	
	/**
	 * Stores an artist to the database. Must implement INSERT query for the artist 
	 * table. Method also checks whether artist is already present in the database 
	 * and add any missing parameters of the {@link Artist} instance passed. Method 
	 * will add a new record and return the {@link Artist} instance in its original 
	 * state if it was not present in the database. If the object was present in the 
	 * database and the passed object got updated with database data, the newly 
	 * updated {@link Artist} object will be returned. In case of a failure to store 
	 * artist, {@code null} will be returned.
	 * <p>
	 * @param artist	Artist needed to be added to the database.
	 * @return			The same artist instance, or updated instance or {@code null}.
	 */
	public Artist 		addArtist(Artist artist);
	
	public int			getNextUnvalidatedArtistId();
}
