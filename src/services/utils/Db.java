package services.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

/**
 * Class for handling database access
 * @author Marc Frincu
 * @since 2010
 *
 */
public class Db {
	
	private static Logger logger = Logger.getLogger(Db.class
			.getPackage().getName());
	
	private Connection conn = null;
	private Statement stmt = null;

	private String username, password, database;

	/**
	 * Constructor
	 * 
	 * @param database
	 * @param username
	 * @param password
	 * @throws Exception
	 */
	protected Db(String database, String username, String password)
			throws Exception {

		this.username = username;
		this.password = password;
		this.database = database;

		Class.forName("org.postgresql.Driver");
		conn = DriverManager.getConnection("jdbc:postgresql:" + database,
				username, password);
		stmt = conn.createStatement();
	}

	private void restoreConnection() throws SQLException {
		Db.logger.debug("Connection lost/closed. Restoring database connection");
		this.conn = DriverManager.getConnection("jdbc:postgresql:"
				+ this.database, this.username, this.password);
		this.stmt = conn.createStatement();
	}
	
	/**
	 * Executes the given SQL statement
	 * 
	 * @param statement
	 * @throws Exception
	 */
	public synchronized void executeStatement(String statement)
			throws Exception {
		if (this.conn.isClosed()) {
			this.restoreConnection();
			// throw new Exception("Attempting to use a closed connection");
		}
		Db.logger.debug(statement);
		this.stmt.executeUpdate(statement);
		this.conn.commit();
		this.conn.close();
	}

	/**
	 * Returns the first row resulted from a given query
	 * 
	 * @param query
	 * @return the first row as a String or null in case of failure
	 * @throws Exception
	 */
	public synchronized String getFirst(String query) throws Exception {
				
		if (this.conn.isClosed()) {
			this.restoreConnection();
			// throw new Exception("Attempting to use a closed connection");
		}
		Db.logger.debug(query);
		ResultSet result = this.stmt.executeQuery(query);
		this.conn.close();
		if (result.next()) {
			return result.getString(1);
		}
		return null;
	}

	/**
	 * Returns the ResultSet associated with a given query
	 * 
	 * @param query
	 * @return a ResultSet
	 * @throws Exception
	 */
	public synchronized ResultSet getQuery(String query) throws Exception {
		if (this.conn.isClosed()) {
			this.restoreConnection();
			//throw new Exception("Attempting to use a closed connection");
		}
		Db.logger.debug(query);
		ResultSet rs = this.stmt.executeQuery(query);
		this.conn.close();
		return rs;
	}

	/**
	 * Returns the database connection
	 * 
	 * @return the database connection
	 */
	public Connection getConnection() {
		return this.conn;
	}
}
