package org.pirate.sportwebserver.service;

import org.pirate.sportwebserver.dto.Bike;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

		try
		{
			List<Map<String, Object>> results = connectionService.executeQuery("SELECT * FROM STRAVA_BIKE order by gear_id");

			for (Map<String, Object> row : results)
			{
				bikes.add(readBike(row));
			}

			log.info("BikeService - Found {} bikes", bikes.size());
			return bikes;
		}
		catch (Exception e)
		{
			log.error("BikeService - Error fetching bikes from database", e);
			throw new RuntimeException("Failed to fetch bikes from database", e);
		}
	}

	private Bike readBike(Map<String, Object> row)
	{
		Bike bike = new Bike();
		bike.gear_id = getString(row, "GEAR_ID");
		bike.gear_name = getString(row, "GEAR_NAME");
		bike.cda = getFloat(row, "CDA");
		bike.crr = getFloat(row, "CRR");
		bike.type = getString(row, "TYP");
		bike.gewicht = getFloat(row, "GEWICHT");
		return bike;
	}

	private String getString(Map<String, Object> row, String column)
	{
		Object val = row.get(column);
		return val != null ? val.toString() : null;
	}

	private float getFloat(Map<String, Object> row, String column)
	{
		Object val = row.get(column);
		return (val instanceof Number n) ? n.floatValue() : 0.0f;
	}

	/**
	 * Bike nach ID abrufen
	 */
	public Bike getBikeById(String id)
	{
		log.info("BikeService - Fetching bike with id: {}", id);

		try
		{

			List<Map<String, Object>> results = connectionService.executeQueryWithParams(
				"SELECT * FROM STRAVA_BIKE WHERE GEAR_ID = ?", id);

			if (results.isEmpty())
			{
				throw new RuntimeException("Bike not found with id: " + id);
			}

			Map<String, Object> row = results.get(0);
			Bike bike = readBike(row);

			log.info("BikeService - Bike found with id: {}", id);
			return bike;
		}
		catch (Exception e)
		{
			log.error("BikeService - Error fetching bike with id: {}", id, e);
			throw new RuntimeException("Failed to fetch bike with id: " + id, e);
		}
	}

}
