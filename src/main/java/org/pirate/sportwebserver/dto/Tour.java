package org.pirate.sportwebserver.dto;

import java.util.ArrayList;
import java.util.List;

public class Tour 
{
	public Tourheader tourdaten = new Tourheader();
	public List<Waypoint> waypoints = new ArrayList<>();
	
	public Tour()
	{
		
	}
}
