package org.pirate.sportwebserver.dto;

import java.io.Serializable;
import java.time.Instant;

/**
 * DTO representing a Strava Athlete with fields from the Strava API.
 * Includes profile information, preferences, and social data.
 */
public class StravaAthlete implements Serializable
{
	private static final long serialVersionUID = 1L;

	private Long id;
	private String username;
	private String firstname;
	private String lastname;
	private String city;
	private String state;
	private String country;
	private String sex; // 'M' or 'F'
	private Boolean summit;
	private Instant createdAt;
	private Instant updatedAt;
	private Integer badgeTypeId;
	private String profileMedium; // URL to medium profile picture
	private String profile; // URL to large profile picture
	private Boolean friend;
	private Boolean follower;
	private String measurementPreference; // 'feet' or 'meters'
	private Double weight; // kg
	private Integer resourceState;
	private Integer ftp; // Functional Threshold Power
	private Boolean premium;

	public StravaAthlete()
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

	public String getUsername()
	{
		return username;
	}

	public void setUsername(String username)
	{
		this.username = username;
	}

	public String getFirstname()
	{
		return firstname;
	}

	public void setFirstname(String firstname)
	{
		this.firstname = firstname;
	}

	public String getLastname()
	{
		return lastname;
	}

	public void setLastname(String lastname)
	{
		this.lastname = lastname;
	}

	public String getCity()
	{
		return city;
	}

	public void setCity(String city)
	{
		this.city = city;
	}

	public String getState()
	{
		return state;
	}

	public void setState(String state)
	{
		this.state = state;
	}

	public String getCountry()
	{
		return country;
	}

	public void setCountry(String country)
	{
		this.country = country;
	}

	public String getSex()
	{
		return sex;
	}

	public void setSex(String sex)
	{
		this.sex = sex;
	}

	public Boolean getSummit()
	{
		return summit;
	}

	public void setSummit(Boolean summit)
	{
		this.summit = summit;
	}

	public Instant getCreatedAt()
	{
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt)
	{
		this.createdAt = createdAt;
	}

	public Instant getUpdatedAt()
	{
		return updatedAt;
	}

	public void setUpdatedAt(Instant updatedAt)
	{
		this.updatedAt = updatedAt;
	}

	public Integer getBadgeTypeId()
	{
		return badgeTypeId;
	}

	public void setBadgeTypeId(Integer badgeTypeId)
	{
		this.badgeTypeId = badgeTypeId;
	}

	public String getProfileMedium()
	{
		return profileMedium;
	}

	public void setProfileMedium(String profileMedium)
	{
		this.profileMedium = profileMedium;
	}

	public String getProfile()
	{
		return profile;
	}

	public void setProfile(String profile)
	{
		this.profile = profile;
	}

	public Boolean getFriend()
	{
		return friend;
	}

	public void setFriend(Boolean friend)
	{
		this.friend = friend;
	}

	public Boolean getFollower()
	{
		return follower;
	}

	public void setFollower(Boolean follower)
	{
		this.follower = follower;
	}

	public String getMeasurementPreference()
	{
		return measurementPreference;
	}

	public void setMeasurementPreference(String measurementPreference)
	{
		this.measurementPreference = measurementPreference;
	}

	public Double getWeight()
	{
		return weight;
	}

	public void setWeight(Double weight)
	{
		this.weight = weight;
	}

	public Integer getResourceState()
	{
		return resourceState;
	}

	public void setResourceState(Integer resourceState)
	{
		this.resourceState = resourceState;
	}

	public Integer getFtp()
	{
		return ftp;
	}

	public void setFtp(Integer ftp)
	{
		this.ftp = ftp;
	}

	public Boolean getPremium()
	{
		return premium;
	}

	public void setPremium(Boolean premium)
	{
		this.premium = premium;
	}
}
