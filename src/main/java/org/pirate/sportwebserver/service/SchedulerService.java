package org.pirate.sportwebserver.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.pirate.sportwebserver.service.StravaService;
import org.pirate.sportwebserver.dto.StravaToken;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import jakarta.annotation.PostConstruct;

@Service
public class SchedulerService
{
	
	private static final Logger log = LoggerFactory.getLogger(SchedulerService.class);

	@Autowired
	private DbConnectionService dbConnection;

	@Autowired(required = false)
	private StravaService stravaService;
	
	@Value("${testvar:default-testvar}")
	private int testvar;
	
	@PostConstruct
	private void init()
	{
		log.info("SchedulerService - init, testvar={}", testvar);
		if(testvar != 3283 )
		{
			log.warn("SchedulerService - Falsch konfiguriert, testvar={}", testvar);
			log.warn("---- Programm wird beendet ----");
			System.exit(1);
			
		}
		
		
		if(!dbConnection.testConnection())
		{
			log.warn("SchedulerService - DB connection test failed");
			log.warn("---- Programm wird beendet ----");
			System.exit(1);
		}
		
		
		log.info("SchedulerService - init completed");
	}
	
	
	@Scheduled(cron = "0 * * * * *")
    public void everyMinute() 
    {
    	log.info("TestService - Running every minute, testvar={}", testvar);
    	testDBConnection();

		
    }
	
	
	private void testStrava()
	{
		// Wenn StravaService vorhanden ist, versuche die ersten 10 Activities abzurufen und zu loggen
				if (stravaService != null) {
											// automatic refresh if token expires within next 5 minutes
											try {
												StravaToken t = stravaService.getCurrentToken();
												if (t != null && t.getExpiresAt() != null) {
													if (t.getExpiresAt().isBefore(Instant.now().plusSeconds(300))) {
														log.info("SchedulerService - Strava token expires soon ({}), refreshing...", t.getExpiresAt());
														try {
															stravaService.refreshAccessToken();
															log.info("SchedulerService - Strava token refreshed successfully");
														} catch (Exception e) {
															log.error("SchedulerService - Failed to refresh Strava token", e);
														}
													}
												}
											} catch (Exception e) {
												log.warn("SchedulerService - Error while checking/refreshing Strava token", e);
											}
					try {
						List<Map<String, Object>> activities = stravaService.getActivities(1, 10);
						log.info("SchedulerService - Fetched {} Strava activities", activities.size());
						int i = 0;
						for (Map<String, Object> act : activities) {
							i++;
							log.info("Strava Activity #{}: {}", i, act.toString());
						}
					} catch (IllegalStateException ise) {
						// z. B. no token available
						log.warn("SchedulerService - Strava not authenticated: {}", ise.getMessage());
					} catch (Exception e) {
						log.error("SchedulerService - Error fetching Strava activities", e);
					}
				} else {
					log.debug("SchedulerService - StravaService not configured, skipping Strava fetch");
				}
	}
	
	
	private void testDBConnection() 
	{
		log.info("Test DB connection every Minute");
		dbConnection.testConnection();
		log.info("TestService - DB connection test completed");
	}
	
}
