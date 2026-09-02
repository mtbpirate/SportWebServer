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
		log.info("TestService - Running every minute, testvar={}", testvar);
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
					ZonedDateTime deathTime = ZonedDateTime.now().plusSeconds(offsetSeconds).plusMinutes(5);
					Instant deathTimeInstant = deathTime.toInstant();

					log.info("SchedulerService - Current Strava token expires at: {}", t.getExpiresAt());
					log.info("SchedulerService - Current time+5: {}", deathTimeInstant);

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

}
