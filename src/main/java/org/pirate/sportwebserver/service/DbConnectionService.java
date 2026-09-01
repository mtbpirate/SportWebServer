package org.pirate.sportwebserver.service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DbConnectionService
{
	private static final Logger log = LoggerFactory.getLogger(DbConnectionService.class);
	
	@Autowired
	private DataSource dataSource;
	

	/**
	 * Führt ein SQL SELECT Statement aus und gibt die Ergebnisse zurück
	 * 
	 * @param sql SQL Query (z.B. "SELECT * FROM bike WHERE gewicht > 2.5")
	 * @return List von Maps mit Spaltennamen als Keys und Spaltenwerte als Values
	 * @throws Exception Falls SQL-Fehler auftritt
	 */
	public List<Map<String, Object>> executeQuery(String sql) throws Exception 
	{
		log.info("ConnectionService - Executing query: {}", sql);
		List<Map<String, Object>> results = new ArrayList<>();
		
		try (Connection connection = dataSource.getConnection();
			 Statement statement = connection.createStatement();
			 ResultSet resultSet = statement.executeQuery(sql)) 
		{
			ResultSetMetaData metaData = resultSet.getMetaData();
			int columnCount = metaData.getColumnCount();
			
			while (resultSet.next()) 
			{
				Map<String, Object> row = new HashMap<>();
				for (int i = 1; i <= columnCount; i++) 
				{
					String columnName = metaData.getColumnName(i);
					Object value = resultSet.getObject(i);
					row.put(columnName, value);
				}
				results.add(row);
			}
			
			log.info("ConnectionService - Query returned {} rows", results.size());
		} 
		catch (Exception e) 
		{
			log.error("ConnectionService - Error executing query", e);
			throw new RuntimeException("Failed to execute query: " + e.getMessage(), e);
		}
		
		return results;
	}
	
	/**
	 * Führt ein SQL INSERT/UPDATE/DELETE Statement aus
	 * 
	 * @param sql SQL Statement (z.B. "UPDATE bike SET gewicht = 2.5 WHERE idbike = 1")
	 * @return Anzahl der betroffenen Zeilen
	 * @throws Exception Falls SQL-Fehler auftritt
	 */
	public int executeUpdate(String sql) throws Exception 
	{
		log.info("ConnectionService - Executing update: {}", sql);
		
		try (Connection connection = dataSource.getConnection();
			 Statement statement = connection.createStatement()) 
		{
			int affectedRows = statement.executeUpdate(sql);
			log.info("ConnectionService - Update affected {} rows", affectedRows);
			return affectedRows;
		} 
		catch (Exception e) 
		{
			log.error("ConnectionService - Error executing update", e);
			throw new RuntimeException("Failed to execute update: " + e.getMessage(), e);
		}
	}
	
	/**
	 * Testet die Datenbankverbindung
	 * 
	 * @return true wenn Verbindung erfolgreich, false sonst
	 */
	public boolean testConnection() 
	{
		log.info("ConnectionService - Testing database connection");
		
		try (Connection connection = dataSource.getConnection()) 
		{
			if (connection != null && !connection.isClosed()) 
			{
				log.info("ConnectionService - Database connection successful");
				log.info("ConnectionService - Database: {} v{}",
					connection.getMetaData().getDatabaseProductName(),
					connection.getMetaData().getDatabaseProductVersion());
				return true;
			}
		} 
		catch (Exception e) 
		{
			log.error("ConnectionService - Database connection failed", e);
		}
		
		return false;
	}
	
	/**
	 * Führt ein SQL Statement mit Parametern aus (verhindert SQL-Injection)
	 * 
	 * @param sql SQL Query mit ? als Platzhalter (z.B. "SELECT * FROM bike WHERE idbike = ?")
	 * @param parameters Parameter für die Platzhalter
	 * @return List von Maps mit Ergebnissen
	 * @throws Exception Falls SQL-Fehler auftritt
	 */
	public List<Map<String, Object>> executeQueryWithParams(String sql, Object... parameters) throws Exception 
	{
		log.info("ConnectionService - Executing parameterized query: {}", sql);
		List<Map<String, Object>> results = new ArrayList<>();
		
		try (Connection connection = dataSource.getConnection();
			 var preparedStatement = connection.prepareStatement(sql)) 
		{
			// Parameter setzen
			for (int i = 0; i < parameters.length; i++) 
			{
				preparedStatement.setObject(i + 1, parameters[i]);
			}
			
			try (ResultSet resultSet = preparedStatement.executeQuery()) 
			{
				ResultSetMetaData metaData = resultSet.getMetaData();
				int columnCount = metaData.getColumnCount();
				
				while (resultSet.next()) 
				{
					Map<String, Object> row = new HashMap<>();
					for (int i = 1; i <= columnCount; i++) 
					{
						String columnName = metaData.getColumnName(i);
						Object value = resultSet.getObject(i);
						row.put(columnName, value);
					}
					results.add(row);
				}
			}
			
			log.info("ConnectionService - Query returned {} rows", results.size());
		} 
		catch (Exception e) 
		{
			log.error("ConnectionService - Error executing parameterized query", e);
			throw new RuntimeException("Failed to execute parameterized query: " + e.getMessage(), e);
		}
		
		return results;
	}
	
	/**
	 * Führt ein SQL INSERT/UPDATE/DELETE Statement mit Parametern aus (verhindert SQL-Injection)
	 * 
	 * @param sql SQL Statement mit ? als Platzhalter (z.B. "INSERT INTO bike (text, gewicht) VALUES (?, ?)")
	 * @param parameters Parameter für die Platzhalter
	 * @return Anzahl der betroffenen Zeilen
	 * @throws Exception Falls SQL-Fehler auftritt
	 */
	public int executeUpdateWithParams(String sql, Object... parameters) throws Exception 
	{
		log.info("ConnectionService - Executing parameterized update: {}", sql);
		
		try (Connection connection = dataSource.getConnection();
			 var preparedStatement = connection.prepareStatement(sql)) 
		{
			// Parameter setzen
			for (int i = 0; i < parameters.length; i++) 
			{
				preparedStatement.setObject(i + 1, parameters[i]);
			}
			
			int affectedRows = preparedStatement.executeUpdate();
			log.info("ConnectionService - Update affected {} rows", affectedRows);
			return affectedRows;
		} 
		catch (Exception e) 
		{
			log.error("ConnectionService - Error executing parameterized update", e);
			throw new RuntimeException("Failed to execute parameterized update: " + e.getMessage(), e);
		}
	}
	
	
	
}
