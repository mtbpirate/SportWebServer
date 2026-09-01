package org.pirate.sportwebserver.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.pirate.sportwebserver.dto.Bike;
import org.pirate.sportwebserver.dto.Tour;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TourService {
private static final Logger log = LoggerFactory.getLogger(TourService.class);
	
	@Autowired
	private DbConnectionService connectionService;

	public List<Tour> getAllTouren() 
	{

		log.info("TourService - Fetching all Tours from database");
		List<Tour> touren = new ArrayList<>();
		
		try {
			List<Map<String, Object>> results = 
				connectionService.executeQuery("SELECT * FROM TOURDATEN order by IDTOUR desc");
			int i=0;
			for (Map<String, Object> row : results) 
			{
				i++;		
				touren.add(readTourHeader(row));
				if(i>20) break;  //TODO: remove this limit, just for testing
			}
			
			log.info("TourService - Found {} Touren", touren.size());
			return touren;
		} catch (Exception e) {
			log.error("TourService - Error fetching Touren from database", e);
			throw new RuntimeException("Failed to fetch Touren from database", e);
		}
		
	}

	public Tour getTourById(long tourId) {
		log.info("TourService - Fetching Tour {} from database", tourId);
		Tour tour = new Tour();
		
		try {
			List<Map<String, Object>> results = 
				connectionService.executeQuery("SELECT * FROM TOURDATEN where IDTOUR = " + tourId);
			
			for (Map<String, Object> row : results) 
			{
				tour = readTourHeader(row);
				break; // Assuming IDTOUR is unique, we can break after the first match
			}
			
			log.info("TourService - Found Tour");
			return tour;
		} catch (Exception e) {
			log.error("TourService - Error fetching Tour from database", e);
			throw new RuntimeException("Failed to fetch Tour from database", e);
		}
		
		
		
	}
	
	private Tour readTourHeader(Map<String, Object> row) {
		Tour tour = new Tour();
		tour.tourdaten.idtour = (long) ((Number) row.get("IDTOUR"));
		tour.tourdaten.titel = (String) row.get("TITEL");
		
		return tour;
	}
	
	
	
}
