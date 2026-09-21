package org.pirate.sportwebserver.service;

import org.pirate.sportwebserver.dto.Tour;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class TourService
{
	private static final Logger log = LoggerFactory.getLogger(TourService.class);

	@Autowired
	private DbConnectionService connectionService;

	public Tour getTourById(long tourId)
	{
		log.info("TourService - Fetching Tour {} from database", tourId);
		Tour tour = new Tour();

		try
		{
			List<Map<String, Object>> results = connectionService.executeQuery("SELECT * FROM TOURDATEN where IDTOUR = " + tourId);

		}
		catch (Exception e)
		{
			log.error("TourService - Error fetching Tour from database", e);
			throw new RuntimeException("Failed to fetch Tour from database", e);
		}
		return null;
	}

	public List<Tour> getTouren(LocalDate vonDatum, LocalDate bisDatum, String sportart, String titel)
	{
		return null;
	}
}
