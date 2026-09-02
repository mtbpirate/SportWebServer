package org.pirate.sportwebserver.dto;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

/**
 * DTO representing all fields from a Strava Activity according to the Strava API.
 */
public class StravaActivity implements Serializable
{

	private static final long serialVersionUID = 1L;

	// Basic identification
	private Long id;
	private String externalId;
	private Long uploadId;
	private Long athleteId; // Extracted from athlete object

	// Activity naming and description
	private String name;
	private String description;

	// Distance and time metrics
	private Double distance; // meters
	private Integer movingTime; // seconds
	private Integer elapsedTime; // seconds
	private Double totalElevationGain; // meters
	private Double elevHigh; // meters
	private Double elevLow; // meters

	// Type and classification
	private String type; // Ride, Run, Swim, etc.
	private String sportType; // More detailed classification
	private Integer workoutType; // 1=race, 2=long run, etc.

	// Dates and timezone
	private Instant startDate; // UTC
	private Instant startDateLocal; // local time
	private String timezone;
	private Integer utcOffset; // seconds

	// Location information
	private String locationCity;
	private String locationState;
	private String locationCountry;
	private List<Double> startLatlng; // [latitude, longitude]
	private List<Double> endLatlng; // [latitude, longitude]

	// Speed metrics
	private Double averageSpeed; // m/s
	private Double maxSpeed; // m/s

	// Power metrics
	private Double averageWatts;
	private Double maxWatts;
	private Double weightedAverageWatts;

	// Heart rate metrics
	private Double averageHeartrate;
	private Double maxHeartrate;
	private Double averageTemp; // celsius

	// Cadence metrics
	private Double averageCadence;

	// Other metrics
	private Double calories;
	private Integer achievementCount;
	private Integer kudosCount;
	private Integer commentCount;
	private Integer athleteCount;
	private Integer photoCount;
	private String gearId;
	private String gearName; // Gear name extracted from gear object

	// Boolean flags
	private Boolean trainer;
	private Boolean commute;
	private Boolean manual;
	private Boolean private_;
	private Boolean flagged;

	// Visibility and status
	private String visibility; // private, followers_only, public
	private String deviceName;
	private String embedToken;
	private Integer resourceState;

	// Array fields (simplified - could be expanded with separate classes)
	private Integer splitCount;
	private Integer lapCount;
	private Integer segmentEffortCount;

	public StravaActivity()
	{
	}

	// Getters and Setters
	public Long getId()
	{
		return id;
	}

	public void setId(Long id)
	{
		this.id = id;
	}

	public String getExternalId()
	{
		return externalId;
	}

	public void setExternalId(String externalId)
	{
		this.externalId = externalId;
	}

	public Long getUploadId()
	{
		return uploadId;
	}

	public void setUploadId(Long uploadId)
	{
		this.uploadId = uploadId;
	}

	public Long getAthleteId()
	{
		return athleteId;
	}

	public void setAthleteId(Long athleteId)
	{
		this.athleteId = athleteId;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public Double getDistance()
	{
		return distance;
	}

	public void setDistance(Double distance)
	{
		this.distance = distance;
	}

	public Integer getMovingTime()
	{
		return movingTime;
	}

	public void setMovingTime(Integer movingTime)
	{
		this.movingTime = movingTime;
	}

	public Integer getElapsedTime()
	{
		return elapsedTime;
	}

	public void setElapsedTime(Integer elapsedTime)
	{
		this.elapsedTime = elapsedTime;
	}

	public Double getTotalElevationGain()
	{
		return totalElevationGain;
	}

	public void setTotalElevationGain(Double totalElevationGain)
	{
		this.totalElevationGain = totalElevationGain;
	}

	public Double getElevHigh()
	{
		return elevHigh;
	}

	public void setElevHigh(Double elevHigh)
	{
		this.elevHigh = elevHigh;
	}

	public Double getElevLow()
	{
		return elevLow;
	}

	public void setElevLow(Double elevLow)
	{
		this.elevLow = elevLow;
	}

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public String getSportType()
	{
		return sportType;
	}

	public void setSportType(String sportType)
	{
		this.sportType = sportType;
	}

	public Integer getWorkoutType()
	{
		return workoutType;
	}

	public void setWorkoutType(Integer workoutType)
	{
		this.workoutType = workoutType;
	}

	public Instant getStartDate()
	{
		return startDate;
	}

	public void setStartDate(Instant startDate)
	{
		this.startDate = startDate;
	}

	public Instant getStartDateLocal()
	{
		return startDateLocal;
	}

	public void setStartDateLocal(Instant startDateLocal)
	{
		this.startDateLocal = startDateLocal;
	}

	public String getTimezone()
	{
		return timezone;
	}

	public void setTimezone(String timezone)
	{
		this.timezone = timezone;
	}

	public Integer getUtcOffset()
	{
		return utcOffset;
	}

	public void setUtcOffset(Integer utcOffset)
	{
		this.utcOffset = utcOffset;
	}

	public String getLocationCity()
	{
		return locationCity;
	}

	public void setLocationCity(String locationCity)
	{
		this.locationCity = locationCity;
	}

	public String getLocationState()
	{
		return locationState;
	}

	public void setLocationState(String locationState)
	{
		this.locationState = locationState;
	}

	public String getLocationCountry()
	{
		return locationCountry;
	}

	public void setLocationCountry(String locationCountry)
	{
		this.locationCountry = locationCountry;
	}

	public List<Double> getStartLatlng()
	{
		return startLatlng;
	}

	public void setStartLatlng(List<Double> startLatlng)
	{
		this.startLatlng = startLatlng;
	}

	public List<Double> getEndLatlng()
	{
		return endLatlng;
	}

	public void setEndLatlng(List<Double> endLatlng)
	{
		this.endLatlng = endLatlng;
	}

	public Double getAverageSpeed()
	{
		return averageSpeed;
	}

	public void setAverageSpeed(Double averageSpeed)
	{
		this.averageSpeed = averageSpeed;
	}

	public Double getMaxSpeed()
	{
		return maxSpeed;
	}

	public void setMaxSpeed(Double maxSpeed)
	{
		this.maxSpeed = maxSpeed;
	}

	public Double getAverageWatts()
	{
		return averageWatts;
	}

	public void setAverageWatts(Double averageWatts)
	{
		this.averageWatts = averageWatts;
	}

	public Double getMaxWatts()
	{
		return maxWatts;
	}

	public void setMaxWatts(Double maxWatts)
	{
		this.maxWatts = maxWatts;
	}

	public Double getWeightedAverageWatts()
	{
		return weightedAverageWatts;
	}

	public void setWeightedAverageWatts(Double weightedAverageWatts)
	{
		this.weightedAverageWatts = weightedAverageWatts;
	}

	public Double getAverageHeartrate()
	{
		return averageHeartrate;
	}

	public void setAverageHeartrate(Double averageHeartrate)
	{
		this.averageHeartrate = averageHeartrate;
	}

	public Double getMaxHeartrate()
	{
		return maxHeartrate;
	}

	public void setMaxHeartrate(Double maxHeartrate)
	{
		this.maxHeartrate = maxHeartrate;
	}

	public Double getAverageTemp()
	{
		return averageTemp;
	}

	public void setAverageTemp(Double averageTemp)
	{
		this.averageTemp = averageTemp;
	}

	public Double getAverageCadence()
	{
		return averageCadence;
	}

	public void setAverageCadence(Double averageCadence)
	{
		this.averageCadence = averageCadence;
	}

	public Double getCalories()
	{
		return calories;
	}

	public void setCalories(Double calories)
	{
		this.calories = calories;
	}

	public Integer getAchievementCount()
	{
		return achievementCount;
	}

	public void setAchievementCount(Integer achievementCount)
	{
		this.achievementCount = achievementCount;
	}

	public Integer getKudosCount()
	{
		return kudosCount;
	}

	public void setKudosCount(Integer kudosCount)
	{
		this.kudosCount = kudosCount;
	}

	public Integer getCommentCount()
	{
		return commentCount;
	}

	public void setCommentCount(Integer commentCount)
	{
		this.commentCount = commentCount;
	}

	public Integer getAthleteCount()
	{
		return athleteCount;
	}

	public void setAthleteCount(Integer athleteCount)
	{
		this.athleteCount = athleteCount;
	}

	public Integer getPhotoCount()
	{
		return photoCount;
	}

	public void setPhotoCount(Integer photoCount)
	{
		this.photoCount = photoCount;
	}

	public String getGearId()
	{
		return gearId;
	}

	public void setGearId(String gearId)
	{
		this.gearId = gearId;
	}

	public String getGearName()
	{
		return gearName;
	}

	public void setGearName(String gearName)
	{
		this.gearName = gearName;
	}

	public Boolean getTrainer()
	{
		return trainer;
	}

	public void setTrainer(Boolean trainer)
	{
		this.trainer = trainer;
	}

	public Boolean getCommute()
	{
		return commute;
	}

	public void setCommute(Boolean commute)
	{
		this.commute = commute;
	}

	public Boolean getManual()
	{
		return manual;
	}

	public void setManual(Boolean manual)
	{
		this.manual = manual;
	}

	public Boolean getPrivate()
	{
		return private_;
	}

	public void setPrivate(Boolean private_)
	{
		this.private_ = private_;
	}

	public Boolean getFlagged()
	{
		return flagged;
	}

	public void setFlagged(Boolean flagged)
	{
		this.flagged = flagged;
	}

	public String getVisibility()
	{
		return visibility;
	}

	public void setVisibility(String visibility)
	{
		this.visibility = visibility;
	}

	public String getDeviceName()
	{
		return deviceName;
	}

	public void setDeviceName(String deviceName)
	{
		this.deviceName = deviceName;
	}

	public String getEmbedToken()
	{
		return embedToken;
	}

	public void setEmbedToken(String embedToken)
	{
		this.embedToken = embedToken;
	}

	public Integer getResourceState()
	{
		return resourceState;
	}

	public void setResourceState(Integer resourceState)
	{
		this.resourceState = resourceState;
	}

	public Integer getSplitCount()
	{
		return splitCount;
	}

	public void setSplitCount(Integer splitCount)
	{
		this.splitCount = splitCount;
	}

	public Integer getLapCount()
	{
		return lapCount;
	}

	public void setLapCount(Integer lapCount)
	{
		this.lapCount = lapCount;
	}

	public Integer getSegmentEffortCount()
	{
		return segmentEffortCount;
	}

	public void setSegmentEffortCount(Integer segmentEffortCount)
	{
		this.segmentEffortCount = segmentEffortCount;
	}
}
