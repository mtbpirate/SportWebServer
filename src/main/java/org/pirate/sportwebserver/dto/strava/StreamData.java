package org.pirate.sportwebserver.dto.strava;

import java.util.List;

public class StreamData<T> {

	private String type;

	private List<T> data;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public List<T> getData() {
		return data;
	}

	public void setData(List<T> data) {
		this.data = data;
	}
}