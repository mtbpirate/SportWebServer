package org.pirate.sportwebserver.service;

import org.pirate.sportwebserver.dto.strava.StravaActivity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ActivityService
{
	private static final Logger log = LoggerFactory.getLogger(ActivityService.class);

	@Autowired
	private DbConnectionService connectionService;

	public StravaActivity getActivityById(long id)
	{
		log.info("AtivityService - Fetching Ativity {} from database", id);
		StravaActivity activity = null;

		try
		{
			List<Map<String, Object>> results = connectionService.executeQuery("SELECT * FROM STRAVA_ACTIVITY where id = " + id);
			if (!results.isEmpty())
			{
				activity = mapToActivity(results.get(0));
			}
		}
		catch (Exception e)
		{
			log.error("TourService - Error fetching Tour from database", e);
			throw new RuntimeException("Failed to fetch Tour from database", e);
		}
		return activity;
	}

	public List<StravaActivity> getActivities(LocalDate vonDatum, LocalDate bisDatum, String sportart, String textfilter)
	{
		List<StravaActivity> activities = new ArrayList<>();

		try
		{

			String sql = "SELECT * FROM STRAVA_ACTIVITY where  1=1 ";

			if (vonDatum != null)
				sql += " and START_DATE_LOCAL >= '" + vonDatum + "' ";
			if (bisDatum != null)
				sql += " and START_DATE_LOCAL <= '" + bisDatum + "' ";
			if (textfilter != null)
				sql += " and lower(name) like '%" + textfilter.toLowerCase() + "%' ";

			sql += " order by ID";

			List<Map<String, Object>> results = connectionService.executeQuery(sql);
			for (Map<String, Object> result : results)
			{
				activities.add(mapToActivity(result));
			}
		}
		catch (Exception e)
		{
			log.error("TourService - Error fetching Tour from database", e);
			throw new RuntimeException("Failed to fetch Tour from database", e);
		}

		return activities;
	}

	private StravaActivity mapToActivity(Map<String, Object> m)
	{
		StravaActivity a = new StravaActivity();

		a.setId(longValue(m.get("ID")));
		a.setExternalId(stringValue(m.get("EXTERNAL_ID")));
		a.setUploadId(longValue(m.get("UPLOAD_ID")));
		a.setAthleteId(longValue(m.get("ATHLETE_ID")));
		a.setName(stringValue(m.get("NAME")));
		a.setDescription(stringValue(m.get("DESCRIPTION")));

		a.setDistance(doubleValue(m.get("DISTANCE")));
		a.setMovingTime(integerValue(m.get("MOVING_TIME")));
		a.setElapsedTime(integerValue(m.get("ELAPSED_TIME")));
		a.setRiderweight(doubleValue(m.get("RIDER_WEIGHT")));
		a.setTotalElevationGain(doubleValue(m.get("TOTAL_ELEVATION_GAIN")));
		a.setElevHigh(doubleValue(m.get("ELEV_HIGH")));
		a.setElevLow(doubleValue(m.get("ELEV_LOW")));

		a.setType(stringValue(m.get("TYPE")));
		a.setSportType(stringValue(m.get("SPORT_TYPE")));
		a.setWorkoutType(integerValue(m.get("WORKOUT_TYPE")));

		a.setStartDate(instantValue(m.get("START_DATE")));
		a.setStartDateLocal(instantValue(m.get("START_DATE_LOCAL")));
		a.setTimezone(stringValue(m.get("TIMEZONE")));
		a.setUtcOffset(integerValue(m.get("UTC_OFFSET")));

		a.setLocationCity(stringValue(m.get("LOCATION_CITY")));
		a.setLocationState(stringValue(m.get("LOCATION_STATE")));
		a.setLocationCountry(stringValue(m.get("LOCATION_COUNTRY")));
		a.setStartLatlng(coordinates(m.get("START_LATITUDE"), m.get("START_LONGITUDE")));
		a.setEndLatlng(coordinates(m.get("END_LATITUDE"), m.get("END_LONGITUDE")));

		a.setAverageSpeed(doubleValue(m.get("AVERAGE_SPEED")));
		a.setMaxSpeed(doubleValue(m.get("MAX_SPEED")));
		a.setAverageWatts(doubleValue(m.get("AVERAGE_WATTS")));
		a.setMaxWatts(doubleValue(m.get("MAX_WATTS")));
		a.setWeightedAverageWatts(doubleValue(m.get("WEIGHTED_AVERAGE_WATTS")));
		a.setDeviceWatts(booleanValue(m.get("DEVICEWATTS")));

		a.setAverageHeartrate(doubleValue(m.get("AVERAGE_HEARTRATE")));
		a.setMaxHeartrate(doubleValue(m.get("MAX_HEARTRATE")));
		a.setAverageTemp(doubleValue(m.get("AVERAGE_TEMP")));
		a.setAverageCadence(doubleValue(m.get("AVERAGE_CADENCE")));

		a.setCalories(doubleValue(m.get("CALORIES")));
		a.setSufferScore(integerValue(m.get("SUFFER_SCORE")));
		a.setAchievementCount(integerValue(m.get("ACHIEVEMENT_COUNT")));
		a.setKudosCount(integerValue(m.get("KUDOS_COUNT")));
		a.setCommentCount(integerValue(m.get("COMMENT_COUNT")));
		a.setAthleteCount(integerValue(m.get("ATHLETE_COUNT")));
		a.setPhotoCount(integerValue(m.get("PHOTO_COUNT")));

		a.setGearId(stringValue(m.get("GEAR_ID")));
		a.setGearName(stringValue(m.get("GEAR_NAME")));
		a.setTrainer(booleanValue(m.get("TRAINER")));
		a.setCommute(booleanValue(m.get("COMMUTE")));
		a.setManual(booleanValue(m.get("MANUAL")));
		a.setPrivate(booleanValue(m.get("PRIVATE_FLAG")));
		a.setFlagged(booleanValue(m.get("FLAGGED")));

		a.setVisibility(stringValue(m.get("VISIBILITY")));
		a.setDeviceName(stringValue(m.get("DEVICE_NAME")));
		a.setEmbedToken(stringValue(m.get("EMBED_TOKEN")));
		a.setResourceState(integerValue(m.get("RESOURCE_STATE")));
		a.setSplitCount(integerValue(m.get("SPLIT_COUNT")));
		a.setLapCount(integerValue(m.get("LAP_COUNT")));
		a.setSegmentEffortCount(integerValue(m.get("SEGMENT_EFFORT_COUNT")));

		return a;
	}

	private String stringValue(Object value)
	{
		return value == null ? null : value.toString();
	}

	private Long longValue(Object value)
	{
		return value instanceof Number ? ((Number) value).longValue() : null;
	}

	private Integer integerValue(Object value)
	{
		return value instanceof Number ? ((Number) value).intValue() : null;
	}

	private Double doubleValue(Object value)
	{
		return value instanceof Number ? ((Number) value).doubleValue() : null;
	}

	private Boolean booleanValue(Object value)
	{
		if (value instanceof Boolean)
			return (Boolean) value;
		if (value instanceof Number)
			return ((Number) value).intValue() != 0;
		return value == null ? null : Boolean.valueOf(value.toString());
	}

	private Instant instantValue(Object value)
	{
		if (value instanceof java.sql.Timestamp)
			return ((java.sql.Timestamp) value).toInstant();
		if (value instanceof java.util.Date)
			return ((java.util.Date) value).toInstant();
		if (value instanceof Instant)
			return (Instant) value;
		if (value instanceof LocalDateTime)
			return ((LocalDateTime) value).toInstant(ZoneOffset.UTC);
		return value == null ? null : Instant.parse(value.toString());
	}

	private List<Double> coordinates(Object latitude, Object longitude)
	{
		Double lat = doubleValue(latitude);
		Double lng = doubleValue(longitude);
		if (lat == null || lng == null)
			return null;

		List<Double> result = new ArrayList<>(2);
		result.add(lat);
		result.add(lng);
		return result;
	}

}
