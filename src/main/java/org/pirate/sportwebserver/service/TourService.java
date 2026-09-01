package org.pirate.sportwebserver.service;

import java.util.ArrayList;
import java.util.List;

import org.pirate.sportwebserver.dto.Tour;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TourService {
private static final Logger log = LoggerFactory.getLogger(TourService.class);
	
	@Autowired
	private DbConnectionService connectionService;

	public List<Tour> getAllTouren() {

		List<Tour> touren = new ArrayList<>();
		Tour t = new Tour();
		t.tourdaten.idtour=1;
		t.tourdaten.beschreibung="Tour 1";
		
		touren.add(t);
		Tour x = new Tour();
		x.tourdaten.idtour=2;
		x.tourdaten.beschreibung="Tour 2";
		
		touren.add(x);
		
		return touren;
		
	}

	public Tour getTourById(int tourId) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
}
