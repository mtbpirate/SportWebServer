package org.pirate.sportwebserver.controller;

import java.util.List;

import org.pirate.sportwebserver.dto.Tour;
import org.pirate.sportwebserver.service.TourService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TourController 
{
	@Autowired
	private TourService tourService;

	/**
	 * Alle Touren abrufen
	 */
	@GetMapping("/touren")
	public List<Tour> getBikes() 
	{
		return tourService.getAllTouren();
	}
	
	/**
	 * Tour nach ID abrufen
	 */
	@GetMapping("/touren/{tourId}")
	public Tour getBikeById(@PathVariable long tourId) 
	{
		return tourService.getTourById(tourId);
	}
	
	
}
