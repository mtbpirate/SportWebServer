package org.pirate.sportwebserver.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.pirate.sportwebserver.dto.Bike;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BikeService 
{
	private static final Logger log = LoggerFactory.getLogger(BikeService.class);
	
	@Autowired
	private DbConnectionService connectionService;
	
	/**
	 * Alle Bikes aus der Datenbank abrufen
	 */
	public List<Bike> getAllBikes() 
	{
		log.info("BikeService - Fetching all bikes from database");
		List<Bike> bikes = new ArrayList<>();
		
		try {
			List<Map<String, Object>> results = 
				connectionService.executeQuery("SELECT * FROM BIKE");
			
			for (Map<String, Object> row : results) 
			{
							
				bikes.add(readBike(row));
			}
			
			log.info("BikeService - Found {} bikes", bikes.size());
			return bikes;
		} catch (Exception e) {
			log.error("BikeService - Error fetching bikes from database", e);
			throw new RuntimeException("Failed to fetch bikes from database", e);
		}
	}

	private Bike readBike(Map<String, Object> row) {
		Bike bike = new Bike();
		bike.setIdbike(((Number) row.get("IDBIKE")).intValue());
		bike.setIdsportart(((Number) row.get("IDSPORTART")).intValue());
		bike.setText((String) row.get("TEXT"));
		bike.setGueltig_von( row.get("GUELTIG_VON").toString());
		bike.setGueltig_bis( row.get("GUELTIG_BIS").toString());
		bike.setGewicht(((Number) row.get("GEWICHT")).floatValue());
		bike.setCr(((Number) row.get("CR")).floatValue());
		bike.setCwa(((Number) row.get("CWA")).floatValue());
		return bike;
	}
	
	/**
	 * Bike nach ID abrufen
	 */
	public Bike getBikeById(int id) 
	{
		log.info("BikeService - Fetching bike with id: {}", id);
		
		try {
			List<Map<String, Object>> results = 
				connectionService.executeQueryWithParams(
					"SELECT * FROM BIKE WHERE idbike = ?", id);
			
			if (results.isEmpty()) {
				throw new RuntimeException("Bike not found with id: " + id);
			}
			
			Map<String, Object> row = results.get(0);
			Bike bike = readBike(row);
			
			log.info("BikeService - Bike found with id: {}", id);
			return bike;
		} catch (Exception e) {
			log.error("BikeService - Error fetching bike with id: {}", id, e);
			throw new RuntimeException("Failed to fetch bike with id: " + id, e);
		}
	}
	
	/**
	 * Neues Bike speichern
	 */
	public Bike saveBike(Bike bike) 
	{
		log.info("BikeService - Saving new bike");
		
		try {
			String sql = "INSERT INTO BIKE (idsportart, text, gueltig_von, gueltig_bis, gewicht, cr, cwa) " +
						 "VALUES (?, ?, ?, ?, ?, ?, ?)";
			
			int affectedRows = connectionService.executeUpdateWithParams(sql,
				bike.getIdsportart(),
				bike.getText(),
				bike.getGueltig_von(),
				bike.getGueltig_bis(),
				bike.getGewicht(),
				bike.getCr(),
				bike.getCwa()
			);
			
			if (affectedRows > 0) {
				log.info("BikeService - Bike saved successfully");
				// Hole das gespeicherte Bike zurück (mit der neu generierten ID)
				return bike;
			} else {
				throw new RuntimeException("Failed to save bike");
			}
		} catch (Exception e) {
			log.error("BikeService - Error saving bike", e);
			throw new RuntimeException("Failed to save bike", e);
		}
	}
	
	/**
	 * Bike löschen
	 */
	public void deleteBike(int id) 
	{
		log.info("BikeService - Deleting bike with id: {}", id);
		
		try {
			int affectedRows = connectionService.executeUpdateWithParams(
				"DELETE FROM BIKE WHERE idbike = ?", id);
			
			if (affectedRows > 0) {
				log.info("BikeService - Bike deleted successfully");
			} else {
				throw new RuntimeException("Bike not found with id: " + id);
			}
		} catch (Exception e) {
			log.error("BikeService - Error deleting bike", e);
			throw new RuntimeException("Failed to delete bike", e);
		}
	}
}

