package org.pirate.sportwebserver.service;

import jakarta.annotation.PostConstruct;
import org.pirate.sportwebserver.dto.strava.StravaActivity;
import org.pirate.sportwebserver.dto.strava.StravaToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Service
public class SchedulerService
{

	private static final Logger log = LoggerFactory.getLogger(SchedulerService.class);

	@Autowired
	private DbConnectionService dbConnection;

	@Autowired(required = false)
	private StravaService stravaService;

	@Value("${startupkey:default-9999}")
	private int startUpKey;

	@PostConstruct
	private void init()
	{

		if (startUpKey != 3283)
		{
			log.warn("SchedulerService - Falsch konfiguriert, startUpKey={}", startUpKey);
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

		/* verschienene Strava-IDs, die zum Testen verwendet werden können
		long idRR = 20089402315L;
		long idErgo = 18230808221L;
		long idMTB = 19762286801L;
		long idAttersee = 18266975743L;
		*/

	}

	//	@Scheduled(cron = "0 * * * * *")
	//	public void everyMinute()
	//	{
	//		log.info("SchedulerService - Running every minute, aktuelle Uhrzeit: {}", ZonedDateTime.now());
	//
	//	}

	@Scheduled(cron = "0 0 * * * *")
	public void everyHour()
	{
		log.info("SchedulerService - Running every hour, aktuelle Uhrzeit: {}", ZonedDateTime.now());
		importNewStravaActivities();
	}

	/**
	 * Runs every 5 minutes to test the DB connection and refresh the Strava token if needed.
	 */
	@Scheduled(cron = "0 */5 * * * *")
	public void every5Minute()
	{
		log.info("SchedulerService - Running every 5 minutes, aktuelle Uhrzeit: {}", ZonedDateTime.now());
		testDBConnection();
		refreshStravaTokenIfNeeded();
	}

	/**
	 * aktualisiert den Strava-Token, wenn er in weniger als 10 Minuten abläuft.
	 */
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

	/**
	 * Testet die DB-Verbindung.
	 */
	private void testDBConnection()
	{
		log.info("Test DB connection");
		dbConnection.testConnection();
		log.info("DB connection test completed");
	}

	private void importNewStravaActivities()
	{
		log.info("---- Import New Strava Activities -----");
		long before = Instant.now().minus(1, ChronoUnit.HOURS).getEpochSecond();
		long after = Instant.now().minus(7, ChronoUnit.DAYS).getEpochSecond();

		List<StravaActivity> activities = stravaService.getActivities(after, before);
		if (!activities.isEmpty())
		{
			log.info("{} Activities gefunden", activities.size());
			for (StravaActivity activity : activities)
			{
				if (!activityExists(activity.getId()))
				{
					stravaService.importStravaActivityToDB(activity.getId());
				}
			}
		}
		else
		{
			log.info("keine Activities gefunden");
		}
		log.info("---- END  Import New Strava Activities -----");
	}

	private boolean activityExists(Long id)
	{
		if (id == null)
		{
			return false;
		}

		try
		{
			List<Map<String, Object>> results = dbConnection.executeQueryWithParams(
				"SELECT ID FROM STRAVA_ACTIVITY WHERE ID = ?", id);
			return !results.isEmpty();
		}
		catch (Exception e)
		{
			log.error("SchedulerService - Error checking activity {} in database", id, e);
			throw new RuntimeException("Failed to check activity in database: " + id, e);
		}
	}

	/**
	 * Importiert noch fehlende Strava-Aktivitäten in die Datenbank. immer nur eine Aktivität pro Aufruf, um die Last zu reduzieren.
	 */
	@Deprecated
	private void importStravaActivities()
	{
		try
		{
			String sql = "SELECT STRAVAID FROM TOURDATEN " +
				" WHERE STRAVAID > 0 AND STRAVAID NOT IN (SELECT ID FROM STRAVA_ACTIVITY) " +
				" ORDER BY 1 ";
			List<Map<String, Object>> list = dbConnection.executeQuery(sql);

			for (Map<String, Object> row : list)
			{
				Long stravaId = (Long) row.get("STRAVAID");
				log.info("StravaID not in STRAVA_ACTIVITY: {}", stravaId);

				stravaService.importStravaActivityToDB(stravaId);
				break;
			}
		}
		catch (Exception e)
		{
			log.error("SchedulerService - Error executing query", e);
		}
	}

}
