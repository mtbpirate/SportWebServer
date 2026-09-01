package org.pirate.sportwebserver.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.pirate.sportwebserver.dto.Bike;
import org.pirate.sportwebserver.dto.Tour;
import org.pirate.sportwebserver.dto.Waypoint;
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
			
			results = connectionService.executeQuery("SELECT * FROM WAYPOINTS where IDTOUR = " + tourId + " order by ZEIT");
			for (Map<String, Object> row : results) 
			{
				Waypoint waypoint = new Waypoint();
				//waypoint.idtour = (long) ((Number) row.get("IDTOUR"));
				waypoint.zeit = (int) ((Number) row.get("ZEIT"));
				if(row.get("DISTANZ") != null) waypoint.distanz = (float) ((Number) row.get("DISTANZ"));
				if(row.get("HOEHE") != null) waypoint.hoehe = (int) ((Number) row.get("HOEHE"));
				if(row.get("PULS") != null) waypoint.puls = (int) ((Number) row.get("PULS"));
				if(row.get("LEISTUNG") != null) waypoint.leistung = (int) ((Number) row.get("LEISTUNG"));
				if(row.get("DREHZAHL") != null) waypoint.drehzahl = (int) ((Number) row.get("DREHZAHL"));
				if(row.get("TEMPERATUR") != null) waypoint.temperatur = (int) ((Number) row.get("TEMPERATUR"));
				if(row.get("LAT") != null) waypoint.lat = (double) ((Number) row.get("LAT"));
				if(row.get("LON") != null) waypoint.lon = (double) ((Number) row.get("LON"));
				//if(row.get("OLDID") != null) waypoint.oldid = (int) ((Number) row.get("OLDID"));
				
				tour.waypoints.add(waypoint);
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
		log.info("TourService - Reading Tour with ID: {}", tour.tourdaten.idtour);
		tour.tourdaten.datum =  ((LocalDateTime) row.get("DATUM")).toString();
		if(row.get("TITEL") != null) 		tour.tourdaten.titel = (String) row.get("TITEL");
		if(row.get("BESCHREIBUNG") != null)	tour.tourdaten.beschreibung = (String) row.get("BESCHREIBUNG");
		if(row.get("DAUER") != null) 		tour.tourdaten.dauer = (int) ((Number) row.get("DAUER"));
		if(row.get("FAHRZEIT") != null)		tour.tourdaten.fahrzeit = (int) ((Number) row.get("FAHRZEIT"));
		if(row.get("DISTANZ") != null)		tour.tourdaten.distanz = (int) ((Number) row.get("DISTANZ"));
		if(row.get("HM") != null) 			tour.tourdaten.hm = (int) ((Number) row.get("HM"));
		if(row.get("GEWICHT") != null)		tour.tourdaten.gewicht = (int) ((Number) row.get("GEWICHT"));
		if(row.get("LEISTUNG") != null)		tour.tourdaten.leistung = (int) ((Number) row.get("LEISTUNG"));
		if(row.get("ENERGIE") != null)		tour.tourdaten.energie = (int) ((Number) row.get("ENERGIE"));
		if(row.get("PULS") != null)			tour.tourdaten.puls = (int) ((Number) row.get("PULS"));
		if(row.get("IDBIKE") != null) 		tour.tourdaten.idbike = (int) ((Number) row.get("IDBIKE"));
		if(row.get("GPS") != null)			tour.tourdaten.gps = (int) ((Number) row.get("GPS"));
		if(row.get("POSL") != null)			tour.tourdaten.posl = (float) ((Number) row.get("POSL"));
		if(row.get("POSB") != null)			tour.tourdaten.posb = (float) ((Number) row.get("POSB"));
		if(row.get("SAVEDATUM") != null) 	tour.tourdaten.savedatum = (int) ((Number) row.get("SAVEDATUM"));
		if(row.get("DELETED") != null) 		tour.tourdaten.deleted = (int) ((Number) row.get("DELETED"));
		if(row.get("OLDID") != null) 		tour.tourdaten.oldid = (long) ((Number) row.get("OLDID"));
		if(row.get("STRAVAID") != null)		tour.tourdaten.stravaid = (long) ((Number) row.get("STRAVAID"));
		return tour;
	}
	
	
	
}
