package com.fortyfourx.chordmaster.pool;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;

import com.fortyfourx.chordmaster.exception.PoolBusyException;
import com.fortyfourx.chordmaster.exception.PoolEmptyException;
import com.fortyfourx.chordmaster.exception.PoolFullException;
import com.fortyfourx.chordmaster.exception.PooledObjectNotFoundException;

/**
 * @author Charith Arumapperuma
 * <p>
 * Uses singleton design pattern to implement web driver pool. Holds methods to create web driver
 * pool. All web drivers in the pool are execution-ready. 
 * <p>
 * <i>Singleton Design Pattern Example - http://crunchify.com/thread-safe-and-a-fast-singleton-implementation-in-java/<i>
 */
public class SingletonWebDriverPool {
	// WebDriverPool constants.
	public static final int		POOL_SIZE 			= 10;
	public static final long	TIMEOUT				= 60000;
	public static final String	FIREFOX_PROFILE_DIR = "C:/Users/Charith Arumapperuma/AppData/Roaming/Mozilla/Firefox/Profiles/285nppoq.default";

	// Only instance of this class.
	private static SingletonWebDriverPool instance;
	
	// Pools.
	private List<WebDriver> idlePool;
	private List<WebDriver> busyPool;
	
	// Other parameters.
	private FirefoxProfile profile;
	private long lastActivityTime;
	
	/**
	 * Prevents creation of new instances from other objects.
	 * <p>
	 * @throws ClassNotFoundException
	 */
	private SingletonWebDriverPool() {
		// Creating connection idlePool.
		idlePool = new ArrayList<WebDriver>();
		busyPool = new ArrayList<WebDriver>();
		
		// Use browser's default profile.
		profile = new FirefoxProfile(new File(SingletonWebDriverPool.FIREFOX_PROFILE_DIR));

		// Status output.
		System.out.println("Creating " + SingletonWebDriverPool.POOL_SIZE + " Firefox browser(s)...");
		
		// Loop creating web driver objects and storing them in idlePool.
		for (int i = 0; i < SingletonWebDriverPool.POOL_SIZE; i++) {
			// Add a new web driver to the idlePool.
			this.idlePool.add(new FirefoxDriver(profile));
		}
		
		// Set last activity time to measure pool idle time.
		this.lastActivityTime = System.currentTimeMillis();
	}
	
	/**
	 * Return the only instance of {@link SingletonWebDriverPool} class.
	 * <p>
	 * @return			return already created class instance.
	 * <p>
	 * @throws ClassNotFoundException JDBC library not found. Further setup of the system must be halted.
	 */
	public static SingletonWebDriverPool getInstance() {
		if (instance == null) {
			synchronized (SingletonDatabaseConnectionPool.class) {
				if (instance == null) {
					instance = new SingletonWebDriverPool();
				}
			}
		}
		
		// Set last activity time to measure pool idle time.
		// instance.lastActivityTime = System.currentTimeMillis();
				
		return instance;
	}
	
	/**
	 * Gets next available Connection object from the pool. If there are no more objects in the pool 
	 * throw a new exception.
	 * <p>
	 * @return 				a {@link WebDriver} object.
	 * <p>
	 * Further processing must be halted when following Exception is raised.
	 * @throws PoolEmptyException is thrown when there are no idle connections available in the pool. 
	 */
	public synchronized WebDriver pop() throws PoolEmptyException {
		if (!idlePool.isEmpty()) {
			WebDriver driver;
			driver = this.idlePool.remove(0);
			this.busyPool.add(driver);
			
			// Status output.
			System.out.println("Started using browser " + driver.hashCode());
			
			// Set last activity time to measure pool idle time.
			this.lastActivityTime = System.currentTimeMillis();
			
			return driver;
		} else {
			throw new PoolEmptyException("SingletonWebDriverPool.pool is empty or all Connections are busy.");
		}
	}
	

	/**
	 * Put back the web driver element borrowed for execution. If the returned object is not in the 
	 * busy pool, an exception is thrown. If the idle pool is full, an exception is thrown.
	 * <p>
	 * @param driver			The object needed to be returned to the pool.
	 * <p>
	 * Further processing must be halted when following exceptions are raised.
	 * @throws PooledObjectNotFoundException	The object is not in the pool.
	 * @throws PoolFullException 				The pool is full due to an some program logic error.
	 */
	public synchronized void push(WebDriver driver) throws PooledObjectNotFoundException, PoolFullException {
		int objId = this.busyPool.indexOf(driver);
		if (objId == -1) {
			throw new PooledObjectNotFoundException("Pooled object not found in SingletonWebDriverPool.pool.");
		} else {
			// Remove object from busy pool.
			this.busyPool.remove(objId);
			
			// If the idle pool is not full, add the object to the idle pool.
			if (this.idlePool.size() < SingletonWebDriverPool.POOL_SIZE) {
				this.idlePool.add(driver);
				
				// Status output.
				System.out.println("Completed using browser " + driver.hashCode());
			} else {
				throw new PoolFullException("WebDriverPool is full.");
			}	
		}
		
		// Set last activity time to measure pool idle time.
		this.lastActivityTime = System.currentTimeMillis();
	}
	
	/**
	 * Closes all WebDriver instances in the pool. It is essential to complete all 
	 * browser tasks before closing. If all web drivers are not idle, an exception 
	 * is thrown. 
	 * @throws PoolBusyException 				There are active pool elements.
	 */
	public synchronized void closeAll() throws PoolBusyException {
		if(this.busyPool.isEmpty()) {
			for(WebDriver driver:idlePool) {
				driver.close();
			}
		} else {
			throw new PoolBusyException(busyPool.size() + " WebDriver instance" + (busyPool.size() > 1 ? "s are" : " is") + " still busy.");
		}
	}
	
	/**
	 * Returns true if all web drivers are in the idle list now. I.e. {@link #busyPool} is empty}.
	 * @return			true or false depending on the size of busy pool.
	 */
	public boolean isIdle() {
		if(this.busyPool.isEmpty()) {
			return true;
		}
		return false;
	}
	
	/**
	 * Checks whether the web driver pool has been idle for more than the specified time.
	 * If timeout is reached, return true. otherwise return false.
	 * @return			true or false depending on timeout status.
	 */
	public boolean isExpired() {
		if ((System.currentTimeMillis() - this.lastActivityTime) > SingletonWebDriverPool.TIMEOUT) {
			return true;
		}
		return false;
	}
}
