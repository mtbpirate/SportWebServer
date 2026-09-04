package org.pirate.sportwebserver.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import org.pirate.sportwebserver.dto.strava.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import java.sql.Timestamp;

import org.pirate.sportwebserver.service.DbConnectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class StravaService
{
	private static final Logger log = LoggerFactory.getLogger(StravaService.class);

	@Value("${strava.client.id}")
	private String clientId;

	@Value("${strava.client.secret}")
	private String clientSecret;

	@Value("${strava.redirect.uri}")
	private String redirectUri;

	private final RestTemplate rest = new RestTemplate();

	// In-memory token store (simple). Replace with DB persistence if needed.
	private volatile StravaToken currentToken;

	@Autowired
	private DbConnectionService dbConnection;

	public String getAuthorizationUrl()
	{
		String scopes = "activity:read_all,profile:read_all";
		String url = String.format(
			"https://www.strava.com/oauth/authorize?client_id=%s&response_type=code&redirect_uri=%s&approval_prompt=auto&scope=%s",
			clientId, redirectUri, scopes);
		return url;
	}

	@SuppressWarnings("unchecked")
	public StravaToken exchangeCodeForToken(String code)
	{
		log.info("Exchanging code for Strava token");

		String url = "https://www.strava.com/oauth/token";

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
		body.add("client_id", clientId);
		body.add("client_secret", clientSecret);
		body.add("code", code);
		body.add("grant_type", "authorization_code");

		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

		Map<String, Object> resp = rest.postForObject(url, request, Map.class);
		if (resp == null)
		{
			throw new RuntimeException("Empty response from Strava token endpoint");
		}

		StravaToken token = mapToToken(resp);
		this.currentToken = token;
		// persist token to DB
		try
		{
			saveTokenToDb(token);
		}
		catch (Exception e)
		{
			log.warn("Failed to persist Strava token to DB", e);
		}
		log.info("Obtained Strava token, expires at {}", token.getExpiresAt());
		return token;
	}

	@PostConstruct
	private void init()
	{
		// Load token from DB on startup
		try
		{
			StravaToken token = loadTokenFromDb();
			if (token != null)
			{
				this.currentToken = token;
				log.info("Loaded Strava token from DB, athleteId={}, expiresAt={}", token.getAthleteId(),
					token.getExpiresAt());
			}
			else
			{
				log.info("No Strava token found in DB on startup");
			}
		}
		catch (Exception e)
		{
			log.warn("Error loading Strava token from DB on startup", e);
		}
	}

	private StravaToken mapToToken(Map<String, Object> resp)
	{
		StravaToken token = new StravaToken();
		token.setAccessToken((String) resp.get("access_token"));
		token.setRefreshToken((String) resp.get("refresh_token"));
		Number expiresAt = (Number) resp.get("expires_at");
		if (expiresAt != null)
			token.setExpiresAt(Instant.ofEpochSecond(expiresAt.longValue()));
		Object athlete = resp.get("athlete");
		if (athlete == null)
		{
			log.info("No athlete info in Strava token response, cop old ID");
			token.setAthleteId(currentToken != null ? currentToken.getAthleteId() : null);
		}
		else if (athlete instanceof Map)
		{
			Map<String, Object> a = (Map<String, Object>) athlete;
			Object id = a.get("id");
			if (id instanceof Number)
				token.setAthleteId(((Number) id).longValue());
		}
		return token;
	}

	@SuppressWarnings("unchecked")
	public StravaToken refreshAccessToken()
	{
		if (currentToken == null || currentToken.getRefreshToken() == null)
		{
			throw new IllegalStateException("No refresh token available. Authenticate first.");
		}

		String url = "https://www.strava.com/oauth/token";

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
		body.add("client_id", clientId);
		body.add("client_secret", clientSecret);
		body.add("grant_type", "refresh_token");
		body.add("refresh_token", currentToken.getRefreshToken());

		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

		Map<String, Object> resp = rest.postForObject(url, request, Map.class);
		if (resp == null)
		{
			throw new RuntimeException("Empty response from Strava token refresh endpoint");
		}
		StravaToken token = mapToToken(resp);
		this.currentToken = token;
		try
		{
			saveTokenToDb(token);
		}
		catch (Exception e)
		{
			log.warn("Failed to persist refreshed Strava token to DB", e);
		}
		log.info("Refreshed Strava token, expires at {}", token.getExpiresAt());
		return token;
	}

	private void saveTokenToDb(StravaToken token) throws Exception
	{
		if (token == null)
			return;
		if (token.getAthleteId() == null)
		{
			log.warn("Cannot persist Strava token without athleteId");
			return;
		}

		// Convert expiresAt to SQL Timestamp if present
		Timestamp expiresAtTs = null;
		if (token.getExpiresAt() != null)
		{
			expiresAtTs = Timestamp.from(token.getExpiresAt());
		}

		// Use MySQL-style upsert (ON DUPLICATE KEY) assuming UNIQUE constraint on
		// athlete_id
		String sql = "INSERT INTO STRAVA_TOKENS (ATHLETE_ID,ACCESS_TOKEN, REFRESH_TOKEN,EXPIRES_AT, RAW_RESPONSE, REVOKED) VALUES (?, ?, ?, ?, ?, 0) " +
			"ON DUPLICATE KEY UPDATE ACCESS_TOKEN = VALUES(ACCESS_TOKEN), REFRESH_TOKEN = VALUES(REFRESH_TOKEN), EXPIRES_AT = VALUES(EXPIRES_AT), RAW_RESPONSE = VALUES(RAW_RESPONSE), REVOKED = 0, UPDATED_AT = CURRENT_TIMESTAMP(3)";

		// raw_response left null (could be enhanced to store full JSON)
		dbConnection.executeUpdateWithParams(sql, token.getAthleteId(), token.getAccessToken(), token.getRefreshToken(),
			expiresAtTs, null);
		log.info("Strava token persisted for athlete {}", token.getAthleteId());
	}

	private StravaToken loadTokenFromDb() throws Exception
	{
		// Load most recently updated non-revoked token
		String sql = "SELECT * FROM STRAVA_TOKENS WHERE REVOKED = 0 ORDER BY UPDATED_AT DESC LIMIT 1";
		List<Map<String, Object>> rows = dbConnection.executeQuery(sql);
		if (rows == null || rows.isEmpty())
			return null;
		Map<String, Object> row = rows.get(0);
		StravaToken token = new StravaToken();
		if (row.get("ATHLETE_ID") != null)
			token.setAthleteId(((Number) row.get("ATHLETE_ID")).longValue());
		if (row.get("ACCESS_TOKEN") != null)
			token.setAccessToken((String) row.get("ACCESS_TOKEN"));
		if (row.get("REFRESH_TOKEN") != null)
			token.setRefreshToken((String) row.get("REFRESH_TOKEN"));
		if (row.get("EXPIRES_AT") != null)
		{
			Object o = row.get("EXPIRES_AT");
			if (o instanceof java.sql.Timestamp)
			{
				token.setExpiresAt(((java.sql.Timestamp) o).toInstant());
			}
			else if (o instanceof Number)
			{
				token.setExpiresAt(Instant.ofEpochSecond(((Number) o).longValue()));
			}
			else if (o instanceof LocalDateTime)
			{
				// tour.tourdaten.datum = ((LocalDateTime) row.get("DATUM")).toString();
				token.setExpiresAt(Instant.ofEpochSecond(((LocalDateTime) o).toEpochSecond(java.time.ZoneOffset.UTC)));
			}
			else if (o instanceof String)
			{
				try
				{
					token.setExpiresAt(Instant.parse((String) o));
				}
				catch (Exception e)
				{
					log.warn("Could not parse EXPIRES_AT value: {}", o);
				}
			}
		}
		return token;
	}

	public List<StravaTrackPoint> getActivityStream(long activityId)
	{
		if (currentToken == null || currentToken.getAccessToken() == null)
		{
			throw new IllegalStateException("No access token available. Authenticate first.");
		}
		try
		{
			String url = String.format("https://www.strava.com/api/v3/activities/%d/streams?keys=latlng,distance,time,altitude,heartrate,watts,velocity_smooth&key_by_type=true", activityId);
			HttpHeaders headers = new HttpHeaders();
			headers.setBearerAuth(currentToken.getAccessToken());
			headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

			org.springframework.http.HttpEntity<Void> entity = new org.springframework.http.HttpEntity<>(headers);
			org.springframework.http.ResponseEntity<Map> response = rest.exchange(url,
				org.springframework.http.HttpMethod.GET, entity, Map.class);
			Map<String, Object> resp = response.getBody();
			if (resp == null)
				throw new RuntimeException("Empty response from Strava activity streams endpoint");

			log.info("Fetched activity streams for activity {} from Strava", activityId);

			List<List<Double>> latlng = getData(resp, "latlng");

			List<Double> distance =
				getData(resp, "distance");

			List<Integer> time =
				getData(resp, "time");

			List<Double> altitude =
				getData(resp, "altitude");

			List<Integer> heartrate =
				getData(resp, "heartrate");

			List<Integer> watts =
				getData(resp, "watts");

			List<Double> velocity =
				getData(resp, "velocity_smooth");

			List<Integer> temp =
				getData(resp, "temp");

			List<Integer> cadence =
				getData(resp, "cadence");

			int count = latlng.size();

			List<StravaTrackPoint> result =
				new ArrayList<>(count);

			for (int i = 0; i < count; i++)
			{
				StravaTrackPoint point =
					new StravaTrackPoint();

				point.setLatitude(
					latlng.get(i).get(0));

				point.setLongitude(
					latlng.get(i).get(1));

				if (time != null && i < time.size())
					point.setTime(time.get(i));

				if (distance != null && i < distance.size())
					point.setDistance(distance.get(i));

				if (altitude != null && i < altitude.size())
					point.setAltitude(altitude.get(i));

				if (heartrate != null && i < heartrate.size())
					point.setHeartrate(heartrate.get(i));

				if (watts != null && i < watts.size())
					point.setWatts(watts.get(i));

				if (velocity != null && i < velocity.size())
					point.setVelocity(velocity.get(i));

				if (temp != null && i < temp.size())
					point.setTemperature(temp.get(i));

				if (cadence != null && i < cadence.size())
					point.setCadence(cadence.get(i));

				result.add(point);
			}

			log.info(
				"Fetched {} trackpoints for activity {}",
				result.size(),
				activityId);

			return result;


		}
		catch (Exception e)
		{
			log.error("Failed to fetch activity streams for activity ID {}", activityId, e);
			throw new RuntimeException("Failed to fetch activity streams for activity ID " + activityId, e);
		}
	}

	@SuppressWarnings("unchecked")
	private <T> List<T> getData(
		Map<String, Object> root,
		String key)
	{
		Object stream = root.get(key);

		if (stream == null)
		{
			return Collections.emptyList();
		}

		Map<String, Object> map =
			(Map<String, Object>) stream;

		return (List<T>) map.get("data");
	}


	public List<StravaActivity> getActivities(long timeFrom, long timeTo)
	{
		if (currentToken == null || currentToken.getAccessToken() == null)
		{
			throw new IllegalStateException("No access token available. Authenticate first.");
		}
		try
		{
			String url = String.format("https://www.strava.com/api/v3/athlete/activities?after=%d&befor=%d",timeFrom, timeTo);
			HttpHeaders headers = new HttpHeaders();
			headers.setBearerAuth(currentToken.getAccessToken());
			headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

			org.springframework.http.HttpEntity<Void> entity = new org.springframework.http.HttpEntity<>(headers);
			org.springframework.http.ResponseEntity<List> response = rest.exchange(url,
				org.springframework.http.HttpMethod.GET, entity, List.class);
			List<Map<String, Object>> resp = response.getBody();
			if (resp == null)
				return Collections.emptyList();

			List<StravaActivity> activities = new ArrayList<>();
			for (Object o : resp)
			{
				if (!(o instanceof Map))
					continue;
				@SuppressWarnings("unchecked")
				Map<String, Object> m = (Map<String, Object>) o;
				StravaActivity a = mapToStravaActivity(m);
				activities.add(a);
			}
			return activities;
		}
		catch (Exception e)
		{
			log.error("Failed to fetch activities", e);
			throw new RuntimeException("Failed to fetch activities", e);
		}
	}

	
	
	
	public List<StravaActivity> getActivities(int page, int perPage)
	{
		if (currentToken == null || currentToken.getAccessToken() == null)
		{
			throw new IllegalStateException("No access token available. Authenticate first.");
		}
		try
		{
			String url = String.format("https://www.strava.com/api/v3/athlete/activities?page=%d&per_page=%d", page,
				perPage);
			HttpHeaders headers = new HttpHeaders();
			headers.setBearerAuth(currentToken.getAccessToken());
			headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

			org.springframework.http.HttpEntity<Void> entity = new org.springframework.http.HttpEntity<>(headers);
			org.springframework.http.ResponseEntity<List> response = rest.exchange(url,
				org.springframework.http.HttpMethod.GET, entity, List.class);
			List<Map<String, Object>> resp = response.getBody();
			if (resp == null)
				return Collections.emptyList();

			List<StravaActivity> activities = new ArrayList<>();
			for (Object o : resp)
			{
				if (!(o instanceof Map))
					continue;
				@SuppressWarnings("unchecked")
				Map<String, Object> m = (Map<String, Object>) o;
				StravaActivity a = mapToStravaActivity(m);
				activities.add(a);
			}
			return activities;
		}
		catch (Exception e)
		{
			log.error("Failed to fetch activities", e);
			throw new RuntimeException("Failed to fetch activities", e);
		}
	}

	public StravaToken getCurrentToken()
	{
		return currentToken;
	}

	public StravaActivity getActivityById(long activityId)
	{
		if (currentToken == null || currentToken.getAccessToken() == null)
		{
			throw new IllegalStateException("No access token available. Authenticate first.");
		}
		try
		{
			String url = String.format("https://www.strava.com/api/v3/activities/%d", activityId);
			HttpHeaders headers = new HttpHeaders();
			headers.setBearerAuth(currentToken.getAccessToken());
			headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

			org.springframework.http.HttpEntity<Void> entity = new org.springframework.http.HttpEntity<>(headers);
			org.springframework.http.ResponseEntity<Map> response = rest.exchange(url,
				org.springframework.http.HttpMethod.GET, entity, Map.class);
			Map<String, Object> resp = response.getBody();
			if (resp == null)
				throw new RuntimeException("Empty response from Strava activity endpoint");

			StravaActivity activity = mapToStravaActivity(resp);
			log.info("Fetched activity {} from Strava", activityId);
			saveActivityToDb(activity);
			return activity;
		}
		catch (Exception e)
		{
			log.error("Failed to fetch activity with ID {}", activityId, e);
			throw new RuntimeException("Failed to fetch activity with ID " + activityId, e);
		}
	}

	private StravaActivity mapToStravaActivity(Map<String, Object> m)
	{
		StravaActivity a = new StravaActivity();

		// Basic identification
		Object id = m.get("id");
		if (id instanceof Number)
			a.setId(((Number) id).longValue());
		else if (id instanceof String)
		{
			try
			{
				a.setId(Long.parseLong((String) id));
			}
			catch (Exception ex)
			{}
		}

		if (m.get("external_id") != null)
			a.setExternalId((String) m.get("external_id"));

		Object uploadId = m.get("upload_id");
		if (uploadId instanceof Number)
			a.setUploadId(((Number) uploadId).longValue());

		// Extract athleteId from athlete object if present
		Object athlete = m.get("athlete");
		if (athlete instanceof Map)
		{
			Map<String, Object> athleteMap = (Map<String, Object>) athlete;
			Object athleteId = athleteMap.get("id");
			if (athleteId instanceof Number)
				a.setAthleteId(((Number) athleteId).longValue());
		}

		// Activity naming and description
		if (m.get("name") != null)
			a.setName((String) m.get("name"));
		if (m.get("description") != null)
			a.setDescription((String) m.get("description"));

		// Distance and time metrics
		Object dist = m.get("distance");
		if (dist instanceof Number)
			a.setDistance(((Number) dist).doubleValue());

		Object mv = m.get("moving_time");
		if (mv instanceof Number)
			a.setMovingTime(((Number) mv).intValue());

		Object el = m.get("elapsed_time");
		if (el instanceof Number)
			a.setElapsedTime(((Number) el).intValue());

		Object teg = m.get("total_elevation_gain");
		if (teg instanceof Number)
			a.setTotalElevationGain(((Number) teg).doubleValue());

		Object elevHigh = m.get("elev_high");
		if (elevHigh instanceof Number)
			a.setElevHigh(((Number) elevHigh).doubleValue());

		Object elevLow = m.get("elev_low");
		if (elevLow instanceof Number)
			a.setElevLow(((Number) elevLow).doubleValue());

		// Type and classification
		if (m.get("type") != null)
			a.setType((String) m.get("type"));
		if (m.get("sport_type") != null)
			a.setSportType((String) m.get("sport_type"));

		Object workoutType = m.get("workout_type");
		if (workoutType instanceof Number)
			a.setWorkoutType(((Number) workoutType).intValue());

		// Dates and timezone
		Object sd = m.get("start_date");
		if (sd instanceof String)
		{
			try
			{
				a.setStartDate(Instant.parse((String) sd));
			}
			catch (Exception ex)
			{
				/* ignore */ }
		}

		Object sdl = m.get("start_date_local");
		if (sdl instanceof String)
		{
			try
			{
				a.setStartDateLocal(Instant.parse((String) sdl));
			}
			catch (Exception ex)
			{
				/* ignore */ }
		}

		if (m.get("timezone") != null)
			a.setTimezone((String) m.get("timezone"));

		Object utcOffset = m.get("utc_offset");
		if (utcOffset instanceof Number)
			a.setUtcOffset(((Number) utcOffset).intValue());

		// Location information
		if (m.get("location_city") != null)
			a.setLocationCity((String) m.get("location_city"));
		if (m.get("location_state") != null)
			a.setLocationState((String) m.get("location_state"));
		if (m.get("location_country") != null)
			a.setLocationCountry((String) m.get("location_country"));

		Object startLatlng = m.get("start_latlng");
		if (startLatlng instanceof java.util.List)
			a.setStartLatlng((java.util.List<Double>) startLatlng);

		Object endLatlng = m.get("end_latlng");
		if (endLatlng instanceof java.util.List)
			a.setEndLatlng((java.util.List<Double>) endLatlng);

		// Speed metrics
		Object avgSpeed = m.get("average_speed");
		if (avgSpeed instanceof Number)
			a.setAverageSpeed(((Number) avgSpeed).doubleValue());

		Object maxSpeed = m.get("max_speed");
		if (maxSpeed instanceof Number)
			a.setMaxSpeed(((Number) maxSpeed).doubleValue());

		// Power metrics
		Object avgWatts = m.get("average_watts");
		if (avgWatts instanceof Number)
			a.setAverageWatts(((Number) avgWatts).doubleValue());

		Object maxWatts = m.get("max_watts");
		if (maxWatts instanceof Number)
			a.setMaxWatts(((Number) maxWatts).doubleValue());

		Object weightedAvgWatts = m.get("weighted_average_watts");
		if (weightedAvgWatts instanceof Number)
			a.setWeightedAverageWatts(((Number) weightedAvgWatts).doubleValue());

		// Heart rate metrics
		Object avgHr = m.get("average_heartrate");
		if (avgHr instanceof Number)
			a.setAverageHeartrate(((Number) avgHr).doubleValue());

		Object maxHr = m.get("max_heartrate");
		if (maxHr instanceof Number)
			a.setMaxHeartrate(((Number) maxHr).doubleValue());

		Object avgTemp = m.get("average_temp");
		if (avgTemp instanceof Number)
			a.setAverageTemp(((Number) avgTemp).doubleValue());

		// Cadence metrics
		Object avgCadence = m.get("average_cadence");
		if (avgCadence instanceof Number)
			a.setAverageCadence(((Number) avgCadence).doubleValue());

		// Other metrics
		Object calories = m.get("calories");
		if (calories instanceof Number)
			a.setCalories(((Number) calories).doubleValue());

		Object achievementCount = m.get("achievement_count");
		if (achievementCount instanceof Number)
			a.setAchievementCount(((Number) achievementCount).intValue());

		Object kudosCount = m.get("kudos_count");
		if (kudosCount instanceof Number)
			a.setKudosCount(((Number) kudosCount).intValue());

		Object commentCount = m.get("comment_count");
		if (commentCount instanceof Number)
			a.setCommentCount(((Number) commentCount).intValue());

		Object athleteCount = m.get("athlete_count");
		if (athleteCount instanceof Number)
			a.setAthleteCount(((Number) athleteCount).intValue());

		Object photoCount = m.get("photo_count");
		if (photoCount instanceof Number)
			a.setPhotoCount(((Number) photoCount).intValue());

		// Gear information
		Object gear = m.get("gear");
		if (gear instanceof Map)
		{
			Map<String, Object> gearMap = (Map<String, Object>) gear;
			Object gearId = gearMap.get("id");
			if (gearId instanceof String)
				a.setGearId((String) gearId);
			if (gearMap.get("name") != null)
				a.setGearName((String) gearMap.get("name"));
		}

		// Boolean flags
		Object trainer = m.get("trainer");
		if (trainer instanceof Boolean)
			a.setTrainer((Boolean) trainer);

		Object commute = m.get("commute");
		if (commute instanceof Boolean)
			a.setCommute((Boolean) commute);

		Object manual = m.get("manual");
		if (manual instanceof Boolean)
			a.setManual((Boolean) manual);

		Object private_ = m.get("private");
		if (private_ instanceof Boolean)
			a.setPrivate((Boolean) private_);

		Object flagged = m.get("flagged");
		if (flagged instanceof Boolean)
			a.setFlagged((Boolean) flagged);

		// Visibility and status
		if (m.get("visibility") != null)
			a.setVisibility((String) m.get("visibility"));

		if (m.get("device_name") != null)
			a.setDeviceName((String) m.get("device_name"));

		if (m.get("embed_token") != null)
			a.setEmbedToken((String) m.get("embed_token"));

		Object resourceState = m.get("resource_state");
		if (resourceState instanceof Number)
			a.setResourceState(((Number) resourceState).intValue());

		// Array field counts
		Object splitCount = m.get("splits_metric");
		if (splitCount instanceof java.util.List)
			a.setSplitCount(((java.util.List<?>) splitCount).size());

		Object lapCount = m.get("laps");
		if (lapCount instanceof java.util.List)
			a.setLapCount(((java.util.List<?>) lapCount).size());

		Object segmentEffortCount = m.get("segment_efforts");
		if (segmentEffortCount instanceof java.util.List)
			a.setSegmentEffortCount(((java.util.List<?>) segmentEffortCount).size());

		return a;
	}

	public StravaAthlete getAthlete(long athleteId)
	{
		if (currentToken == null || currentToken.getAccessToken() == null)
		{
			throw new IllegalStateException("No access token available. Authenticate first.");
		}
		try
		{
			String url = String.format("https://www.strava.com/api/v3/athletes/%d", athleteId);
			HttpHeaders headers = new HttpHeaders();
			headers.setBearerAuth(currentToken.getAccessToken());
			headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

			org.springframework.http.HttpEntity<Void> entity = new org.springframework.http.HttpEntity<>(headers);
			org.springframework.http.ResponseEntity<Map> response = rest.exchange(url,
				org.springframework.http.HttpMethod.GET, entity, Map.class);
			Map<String, Object> resp = response.getBody();
			if (resp == null)
				throw new RuntimeException("Empty response from Strava athlete endpoint");

			StravaAthlete athlete = mapToStravaAthlete(resp);
			log.info("Fetched athlete {} from Strava", athleteId);
			return athlete;
		}
		catch (Exception e)
		{
			log.error("Failed to fetch athlete with ID {}", athleteId, e);
			throw new RuntimeException("Failed to fetch athlete with ID " + athleteId, e);
		}
	}

	private StravaAthlete mapToStravaAthlete(Map<String, Object> m)
	{
		StravaAthlete athlete = new StravaAthlete();

		// id
		Object id = m.get("id");
		if (id instanceof Number)
			athlete.setId(((Number) id).longValue());
		else if (id instanceof String)
		{
			try
			{
				athlete.setId(Long.parseLong((String) id));
			}
			catch (Exception ex)
			{}
		}

		// basic info
		if (m.get("username") != null)
			athlete.setUsername((String) m.get("username"));
		if (m.get("firstname") != null)
			athlete.setFirstname((String) m.get("firstname"));
		if (m.get("lastname") != null)
			athlete.setLastname((String) m.get("lastname"));
		if (m.get("city") != null)
			athlete.setCity((String) m.get("city"));
		if (m.get("state") != null)
			athlete.setState((String) m.get("state"));
		if (m.get("country") != null)
			athlete.setCountry((String) m.get("country"));
		if (m.get("sex") != null)
			athlete.setSex((String) m.get("sex"));

		// boolean fields
		Object summit = m.get("summit");
		if (summit instanceof Boolean)
			athlete.setSummit((Boolean) summit);
		Object friend = m.get("friend");
		if (friend instanceof Boolean)
			athlete.setFriend((Boolean) friend);
		Object follower = m.get("follower");
		if (follower instanceof Boolean)
			athlete.setFollower((Boolean) follower);

		// dates
		Object createdAt = m.get("created_at");
		if (createdAt instanceof String)
		{
			try
			{
				athlete.setCreatedAt(Instant.parse((String) createdAt));
			}
			catch (Exception ex)
			{
				/* ignore */ }
		}
		Object updatedAt = m.get("updated_at");
		if (updatedAt instanceof String)
		{
			try
			{
				athlete.setUpdatedAt(Instant.parse((String) updatedAt));
			}
			catch (Exception ex)
			{
				/* ignore */ }
		}

		// profile pictures
		if (m.get("profile_medium") != null)
			athlete.setProfileMedium((String) m.get("profile_medium"));
		if (m.get("profile") != null)
			athlete.setProfile((String) m.get("profile"));

		// badge and resource state
		Object badgeTypeId = m.get("badge_type_id");
		if (badgeTypeId instanceof Number)
			athlete.setBadgeTypeId(((Number) badgeTypeId).intValue());
		Object resourceState = m.get("resource_state");
		if (resourceState instanceof Number)
			athlete.setResourceState(((Number) resourceState).intValue());

		// preferences
		if (m.get("measurement_preference") != null)
			athlete.setMeasurementPreference((String) m.get("measurement_preference"));
		Object weight = m.get("weight");
		if (weight instanceof Number)
			athlete.setWeight(((Number) weight).doubleValue());

		// additional fields
		if (m.get("ftp") != null)
			athlete.setFtp((Integer) m.get("ftp"));
		if (m.get("premium") != null)
			athlete.setPremium((Boolean) m.get("premium"));

		return athlete;
	}
	
	
    public void saveActivityToDb(StravaActivity a)
    {
        
    	String sql =
            """
            INSERT INTO STRAVA_ACTIVITY
            (
                ID, EXTERNAL_ID, UPLOAD_ID, ATHLETE_ID, NAME, DESCRIPTION, DISTANCE, MOVING_TIME, ELAPSED_TIME, TOTAL_ELEVATION_GAIN,
                ELEV_HIGH, ELEV_LOW, TYPE, SPORT_TYPE, WORKOUT_TYPE, START_DATE, START_DATE_LOCAL, TIMEZONE, UTC_OFFSET, LOCATION_CITY,
                LOCATION_STATE, LOCATION_COUNTRY, START_LATITUDE, START_LONGITUDE, END_LATITUDE, END_LONGITUDE, AVERAGE_SPEED, MAX_SPEED, AVERAGE_WATTS, MAX_WATTS,
                WEIGHTED_AVERAGE_WATTS, AVERAGE_HEARTRATE, MAX_HEARTRATE, AVERAGE_TEMP, AVERAGE_CADENCE, CALORIES, ACHIEVEMENT_COUNT, KUDOS_COUNT, COMMENT_COUNT, ATHLETE_COUNT,
                PHOTO_COUNT, GEAR_ID, GEAR_NAME, TRAINER, COMMUTE, MANUAL, PRIVATE_FLAG, FLAGGED, VISIBILITY, DEVICE_NAME,
                EMBED_TOKEN, RESOURCE_STATE, SPLIT_COUNT,  LAP_COUNT, SEGMENT_EFFORT_COUNT
            )
            VALUES
            (
                ?,?,?,?,?,?,?,?,?,?,
                ?,?,?,?,?,?,?,?,?,?,
                ?,?,?,?,?,?,?,?,?,?,
                ?,?,?,?,?,?,?,?,?,?,
                ?,?,?,?,?,?,?,?,?,?,
                ?,?,?,?,?
            )
            """;

    	
    	try
		{
			dbConnection.executeUpdateWithParams(
			    sql,
			    a.getId(),
			    a.getExternalId(),
			    a.getUploadId(),
			    a.getAthleteId(),
			    a.getName(),
			    a.getDescription(),
			    a.getDistance(),
			    a.getMovingTime(),
			    a.getElapsedTime(),
			    a.getTotalElevationGain(),
			    
			    a.getElevHigh(),
			    a.getElevLow(),
			    a.getType(),
			    a.getSportType(),
			    a.getWorkoutType(),
			    a.getStartDate() == null ? null : Timestamp.from(a.getStartDate()),
			    a.getStartDateLocal() == null ? null : Timestamp.from(a.getStartDateLocal()),
			    a.getTimezone(),
			    a.getUtcOffset(),
			    a.getLocationCity(),
			    
			    a.getLocationState(),
			    a.getLocationCountry(),
			    getLat(a.getStartLatlng()),
			    getLng(a.getStartLatlng()),
			    getLat(a.getEndLatlng()),
			    getLng(a.getEndLatlng()),
			    a.getAverageSpeed(),
			    a.getMaxSpeed(),
			    a.getAverageWatts(),
			    a.getMaxWatts(),
			    
			    a.getWeightedAverageWatts(),
			    a.getAverageHeartrate(),
			    a.getMaxHeartrate(),
			    a.getAverageTemp(),
			    a.getAverageCadence(),
			    a.getCalories(),
			    a.getAchievementCount(),
			    a.getKudosCount(),
			    a.getCommentCount(),
			    a.getAthleteCount(),
			    
			    a.getPhotoCount(),
			    a.getGearId(),
			    a.getGearName(),
			    a.getTrainer(),
			    a.getCommute(),
			    a.getManual(),
			    a.getPrivate(),
			    a.getFlagged(),
			    a.getVisibility(),
			    a.getDeviceName(),
			    
			    a.getEmbedToken(),
			    a.getResourceState(),
			    a.getSplitCount(),
			    a.getLapCount(),
			    a.getSegmentEffortCount()
			);
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    private Double getLat(List<Double> latlng)
    {
        return latlng != null && latlng.size() > 0
            ? latlng.get(0)
            : null;
    }

    private Double getLng(List<Double> latlng)
    {
        return latlng != null && latlng.size() > 1
            ? latlng.get(1)
            : null;
    }



	public List<StravaTrackPoint> createTrackPoints(
		ActivityStreams streams) {

		List<StravaTrackPoint> result = new ArrayList<>();

		int count = streams.getTime().getData().size();

		for (int i = 0; i < count; i++) {

			StravaTrackPoint p = new StravaTrackPoint();

			p.setTime(
				streams.getTime().getData().get(i));

			p.setDistance(
				streams.getDistance().getData().get(i));

			p.setAltitude(
				streams.getAltitude().getData().get(i));

			p.setHeartrate(
				streams.getHeartrate().getData().get(i));

			p.setWatts(
				streams.getWatts().getData().get(i));

			p.setVelocity(
				streams.getVelocity_smooth().getData().get(i));

			List<Double> latlng =
				streams.getLatlng().getData().get(i);

			p.setLatitude(latlng.get(0));
			p.setLongitude(latlng.get(1));

			result.add(p);
		}

		return result;
	}

	public boolean existsStravaActivityinDB(Long id)
	{
		String sql = "SELECT * FROM STRAVA_ACTIVITY WHERE ID = " + id;
		
		 try
		{
			List<Map<String, Object>>  x = this.dbConnection.executeQuery(sql);
			if(x != null && !x.isEmpty())
			{
				return true;
			}
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return false;
	}
	
}