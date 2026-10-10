package org.pirate.sportwebserver.controller;

import org.pirate.sportwebserver.service.DbConnectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ha")
public class HomeAssistantController
{
	private static final Logger log = LoggerFactory.getLogger(HomeAssistantController.class);

	@Autowired
	private DbConnectionService connectionService;

	@GetMapping("/km")
	public ResponseEntity<Map<String, Object>> getDistance()
	{
		log.info("GET: /ha/km");
		float distance = 0.0f;
		try
		{
			List<Map<String, Object>> results = connectionService.executeQuery("SELECT round(SUM(DISTANCE)/1000.0,1) AS DISTANCE FROM STRAVA_ACTIVITY ");
			if (!results.isEmpty())
			{
				Map<String, Object> row = results.get(0);
				distance = ((Number) row.get("DISTANCE")).floatValue();
			}
			else
			{
				log.warn("No results returned from query");
				throw new RuntimeException();
			}

		}
		catch (Exception e)
		{
			log.error("Error executing query", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
		}

		return ResponseEntity.ok(Map.of("distance", distance));
	}

	@GetMapping("/hm")
	public ResponseEntity<Map<String, Object>> getElevation()
	{
		log.info("GET: /ha/hm");
		long elevation = 0L;
		try
		{
			List<Map<String, Object>> results = connectionService.executeQuery("SELECT round(SUM(TOTAL_ELEVATION_GAIN)) AS ELEVATION FROM STRAVA_ACTIVITY ");
			if (!results.isEmpty())
			{
				Map<String, Object> row = results.get(0);
				elevation = ((Number) row.get("ELEVATION")).longValue();
			}
			else
			{
				log.warn("No results returned from query");
				throw new RuntimeException();
			}

		}
		catch (Exception e)
		{
			log.error("Error executing query", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
		}

		return ResponseEntity.ok(Map.of("elevation", elevation));
	}

}
