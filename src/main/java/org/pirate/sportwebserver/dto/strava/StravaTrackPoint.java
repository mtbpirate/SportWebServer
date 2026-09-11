package org.pirate.sportwebserver.dto.strava;

public class StravaTrackPoint
{

	private Integer time;
	private Double distance;
	private Double latitude;
	private Double longitude;
	private Double altitude;
	private Integer heartrate;
	private Double watts;
	private Double velocity;
	private Integer temperature;
	private Integer cadence;
	private Double grade;

	public Integer getTime()
	{
		return time;
	}

	public void setTime(Integer time)
	{
		this.time = time;
	}

	public Double getDistance()
	{
		return distance;
	}

	public void setDistance(Double distance)
	{
		this.distance = distance;
	}

	public Double getLatitude()
	{
		return latitude;
	}

	public void setLatitude(Double latitude)
	{
		this.latitude = latitude;
	}

	public Double getLongitude()
	{
		return longitude;
	}

	public void setLongitude(Double longitude)
	{
		this.longitude = longitude;
	}

	public Double getAltitude()
	{
		return altitude;
	}

	public void setAltitude(Double altitude)
	{
		this.altitude = altitude;
	}

	public Integer getHeartrate()
	{
		return heartrate;
	}

	public void setHeartrate(Integer heartrate)
	{
		this.heartrate = heartrate;
	}

	public Double getWatts()
	{
		return watts;
	}

	public void setWatts(Double watts)
	{
		this.watts = watts;
	}

	public Double getVelocity()
	{
		return velocity;
	}

	public void setVelocity(Double velocity)
	{
		this.velocity = velocity;
	}

	public Integer getTemperature()
	{
		return temperature;
	}

	public void setTemperature(Integer temperature)
	{
		this.temperature = temperature;
	}

	public Integer getCadence()
	{
		return cadence;
	}

	public void setCadence(Integer cadence)
	{
		this.cadence = cadence;
	}

	public Double getGrade()
	{
		return grade;
	}

	public void setGrade(Double grade)
	{
		this.grade = grade;
	}
}
