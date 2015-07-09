package com.fortyfourx.chordmaster.extractor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.fortyfourx.chordmaster.exception.PoolEmptyException;
import com.fortyfourx.chordmaster.exception.PoolFullException;
import com.fortyfourx.chordmaster.exception.PooledObjectNotFoundException;

/**
 * @author Charith Arumapperuma
 * <p>
 * Uses singleton design pattern to implement database connection pool. Holds methods to create 
 * connection pool. All connections in the pool are execution-ready. 
 * <p>
 * <i>Singleton Design Pattern Example - http://crunchify.com/thread-safe-and-a-fast-singleton-implementation-in-java/<i>
 */
public class SingletonDatabaseConnectionPool {
	// DatabaseConnectionPool constants.
	public static final int		POOL_SIZE 		  = 10;
	public static final String	DATABASE_DRIVER   = "com.mysql.jdbc.Driver";
	public static final String	DATABASE_HOST     = "jdbc:mysql://localhost/chordmaster.v2";
	public static final String	DATABASE_USERNAME = "root";
	public static final String	DATABASE_PASSWORD = "";

	// Only instance of this class.
	private static SingletonDatabaseConnectionPool instance;
	
	// Pools.
	private List<Connection> idlePool;
	private List<Connection> busyPool;

	/**
	 * Prevents creation of new instances from other objects.
	 * <p>
	 * @throws ClassNotFoundException
	 */
	private SingletonDatabaseConnectionPool() throws ClassNotFoundException {
		// Creating connection idlePool.
		idlePool = new ArrayList<>();
		busyPool = new ArrayList<>();
		
		// Try to get the driver.
		Class.forName(DatabaseHandler.DRIVER);
		
		// Loop creating connection objects and storing them in idlePool.
		for (int i = 0; i < SingletonDatabaseConnectionPool.POOL_SIZE; i++) {
			try {
				// Add a new connection to the idlePool.
				this.idlePool.add(DriverManager.getConnection(
						SingletonDatabaseConnectionPool.DATABASE_HOST, 
						SingletonDatabaseConnectionPool.DATABASE_USERNAME, 
						SingletonDatabaseConnectionPool.DATABASE_PASSWORD));
			} catch (SQLException sqle) {
				// If a connection failed to create, decrease the counter to ensure specified amount of connections.
				i--;
				System.err.println("Failed to create a connection at index = " + i);
			}
		}
	}

	/**
	 * Return the only instance of {@link SingletonDatabaseConnectionPool} class.
	 * <p>
	 * @return			return already created class instance.
	 * <p>
	 * @throws ClassNotFoundException JDBC library not found. Further setup of the system must be halted.
	 */
	public static SingletonDatabaseConnectionPool getInstance() throws ClassNotFoundException {
		if (instance == null) {
			synchronized (SingletonDatabaseConnectionPool.class) {
				if (instance == null) {
					instance = new SingletonDatabaseConnectionPool();
				}
			}
		}
		return instance;
	}
	
	/**
	 * Gets next available Connection object from the pool. 
	 * If there are no more objects in the pool throw a new exception.
	 * <p>
	 * @return 				a {@link Connection} object.
	 * <p>
	 * Further processing must be halted when following Exception is raised.
	 * @throws PoolEmptyException is thrown when there are no idle connections available in the pool. 
	 */
	public Connection pop() throws PoolEmptyException {
		if (!idlePool.isEmpty()) {
			Connection conn;
			conn = this.idlePool.remove(0);
			this.busyPool.add(conn);
			return conn;
		} else {
			throw new PoolEmptyException("SingletonDatabaseConnectionPool.pool is empty or all Connections are busy.");
		}
	}

	/**
	 * Put back the connection object element borrowed for execution. 
	 * If the returned object is not in the busy pool, an exception is thrown. 
	 * If the idle pool is full, an exception is thrown.
	 * <p>
	 * @param connection		The object needed to be returned to the pool.
	 * <p>
	 * Further processing must be halted when following exceptions are raised.
	 * @throws PooledObjectNotFoundException	The object is not in the pool.
	 * @throws PoolFullException 				The pool is full due to an some program logic error.
	 */
	public void push(Connection connection) throws PooledObjectNotFoundException, PoolFullException {
		int objId = this.busyPool.indexOf(connection);
		if (objId == -1) {
			throw new PooledObjectNotFoundException("Pooled object not found in SingletonDatabaseConnectionPool.pool.");
		} else {
			Connection conn = this.busyPool.remove(objId);
			if (this.busyPool.size() == SingletonDatabaseConnectionPool.POOL_SIZE) {
				this.idlePool.add(conn);
			} else {
				throw new PoolFullException("SingletonDatabaseConnectionPool.pool is full.");
			}
		}
	}
}
