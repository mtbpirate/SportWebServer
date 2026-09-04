package org.pirate.sportwebserver.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.pirate.sportwebserver.dto.strava.StravaActivity;
import org.pirate.sportwebserver.dto.strava.StravaToken;
import org.pirate.sportwebserver.service.StravaService;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

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

	private long lastStravaImportTime = 0;
	
	@PostConstruct
	private void init()
	{
		log.info("SchedulerService - init, testvar={}", testvar);
		if (testvar != 3283)
		{
			log.warn("SchedulerService - Falsch konfiguriert, testvar={}", testvar);
			log.warn("---- Programm wird beendet ----");
			System.exit(1);

		}

		if (!dbConnection.testConnection())
		{
			log.warn("SchedulerService - DB connection test failed");
			log.warn("---- Programm wird beendet ----");
			System.exit(1);
		}

		refreshStravaTokenIfNeeded();

		log.info("SchedulerService - init completed");

	}

	@Scheduled(cron = "0 * * * * *")
	public void everyMinute()
	{
		log.info("TestService - Running every minute");
		importStravaActivities();
		
		
	}
	
	

	@Scheduled(cron = "0 */5 * * * *")
	public void every5Minute()
	{
		log.info("TestService - Running every 5 minutes");
		testDBConnection();
		refreshStravaTokenIfNeeded();

	}

	private void refreshStravaTokenIfNeeded()
	{
		log.info("SchedulerService - Checking Strava token expiration");
		if (stravaService != null)
		{
			try
			{
				StravaToken t = stravaService.getCurrentToken();
				if (t != null && t.getExpiresAt() != null)
				{
					int offsetSeconds = ZoneId.systemDefault().getRules().getOffset(Instant.now()).getTotalSeconds();
					ZonedDateTime deathTime = ZonedDateTime.now().plusSeconds(offsetSeconds).plusMinutes(10);
					Instant deathTimeInstant = deathTime.toInstant();

					log.info("SchedulerService - Current Strava token expires at: {}", t.getExpiresAt());
					log.info("SchedulerService - Current time+10: {}", deathTimeInstant);

					if (t.getExpiresAt().isBefore(deathTimeInstant))
					{
						log.info("SchedulerService - Strava token expires soon ({}), refreshing...", t.getExpiresAt());
						try
						{
							stravaService.refreshAccessToken();
							log.info("SchedulerService - Strava token refreshed successfully");
						}
						catch (Exception e)
						{
							log.error("SchedulerService - Failed to refresh Strava token", e);
						}
					}
				}
			}
			catch (Exception e)
			{
				log.warn("SchedulerService - Error while checking/refreshing Strava token", e);
			}
		}
	}

	private void testDBConnection()
	{
		log.info("Test DB connection every Minute");
		dbConnection.testConnection();
		log.info("TestService - DB connection test completed");
	}
	
	
	private void importStravaActivities()
	{
		if (lastStravaImportTime == 0) lastStravaImportTime =  System.currentTimeMillis() / 1000L;
	
		long timefrom = lastStravaImportTime-3600*24*30; // 1 month back
		List<StravaActivity> activities = stravaService.getActivities(timefrom, lastStravaImportTime);
		
		for (StravaActivity activity : activities)
		{
			log.info("Importing Strava activity: {}", activity);
			if (stravaService.existsStravaActivityinDB(activity.getId()))
			{
				log.info("Strava activity {} already exists in DB, skipping", activity.getId());
				lastStravaImportTime = Math.min(lastStravaImportTime, activity.getStartDate().getEpochSecond());
			}
			else
			{
				stravaService.saveActivityToDb(activity);
				log.info("Strava activity {} inserted into DB", activity.getId());
				lastStravaImportTime = Math.min(lastStravaImportTime, activity.getStartDate().getEpochSecond());
			}	
		}
		
		
		
		
	}
	
	

}
