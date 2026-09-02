package org.pirate.sportwebserver.dto;

import java.io.Serializable;
import java.time.Instant;

/**
 * DTO representing a subset of fields from a Strava Activity.
 * Only commonly used fields are included; add more as needed.
 */
public class StravaActivity implements Serializable
{

	private static final long serialVersionUID = 1L;

	private Long id;
	private String name;
	private String type;
	private Double distance; // meters
	private Integer movingTime; // seconds
	private Integer elapsedTime; // seconds
	private Double totalElevationGain; // meters
	private Instant startDate; // UTC
	private Instant startDateLocal; // local time
	private Double averageSpeed; // m/s
	private Double maxSpeed; // m/s
	private Double averageHeartrate;
	private Double maxHeartrate;
	private Double averageWatts;
	private String deviceName;

	public StravaActivity()
	{
	}

	public Long getId()
	{
		return id;
	}

	public void setId(Long id)
	{
		this.id = id;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
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

	public Double getAverageWatts()
	{
		return averageWatts;
	}

	public void setAverageWatts(Double averageWatts)
	{
		this.averageWatts = averageWatts;
	}

	public String getDeviceName()
	{
		return deviceName;
	}

	public void setDeviceName(String deviceName)
	{
		this.deviceName = deviceName;
	}
}
