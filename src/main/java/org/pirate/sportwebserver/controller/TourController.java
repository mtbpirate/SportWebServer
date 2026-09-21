package org.pirate.sportwebserver.controller;

import org.pirate.sportwebserver.dto.Tour;
import org.pirate.sportwebserver.service.TourService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
public class TourController
{
	@Autowired
	private TourService tourService;

	/**
	 *
	 */
	@GetMapping("/touren")
	public List<Tour> getTouren(
		@RequestParam(required = false) LocalDate vonDatum,
		@RequestParam(required = false) LocalDate bisDatum,
		@RequestParam(required = false) String sportart,
		@RequestParam(required = false) String titel)
	{
		return tourService.getTouren(vonDatum, bisDatum, sportart, titel);
	}

	/**
	 * Tour nach ID abrufen
	 */
	@GetMapping("/touren/{tourId}")
	public Tour getTourById(@PathVariable long tourId)
	{
		return tourService.getTourById(tourId);
	}

}
