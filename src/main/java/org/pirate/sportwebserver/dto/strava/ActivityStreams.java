package org.pirate.sportwebserver.dto.strava;

import java.util.List;

public class ActivityStreams {

	private StreamData<List<Double>> latlng;

	private StreamData<Double> distance;

	private StreamData<Integer> time;

	private StreamData<Double> altitude;

	private StreamData<Integer> heartrate;

	private StreamData<Integer> watts;

	private StreamData<Double> velocity_smooth;

	public StreamData<List<Double>> getLatlng() {
		return latlng;
	}

	public void setLatlng(StreamData<List<Double>> latlng) {
		this.latlng = latlng;
	}

	public StreamData<Double> getDistance() {
		return distance;
	}

	public void setDistance(StreamData<Double> distance) {
		this.distance = distance;
	}

	public StreamData<Integer> getTime() {
		return time;
	}

	public void setTime(StreamData<Integer> time) {
		this.time = time;
	}

	public StreamData<Double> getAltitude() {
		return altitude;
	}

	public void setAltitude(StreamData<Double> altitude) {
		this.altitude = altitude;
	}

	public StreamData<Integer> getHeartrate() {
		return heartrate;
	}

	public void setHeartrate(StreamData<Integer> heartrate) {
		this.heartrate = heartrate;
	}

	public StreamData<Integer> getWatts() {
		return watts;
	}

	public void setWatts(StreamData<Integer> watts) {
		this.watts = watts;
	}

	public StreamData<Double> getVelocity_smooth() {
		return velocity_smooth;
	}

	public void setVelocity_smooth(StreamData<Double> velocity_smooth) {
		this.velocity_smooth = velocity_smooth;
	}
}