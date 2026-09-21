package org.pirate.sportwebserver.controller;

import org.pirate.sportwebserver.dto.Bike;
import org.pirate.sportwebserver.service.BikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class BikeController
{
	@Autowired
	private BikeService bikeService;

	/**
	 * Alle Bikes abrufen
	 */
	@GetMapping("/bikes")
	public List<Bike> getBikes()
	{
		return bikeService.getAllBikes();
	}

	/**
	 * Bike nach ID abrufen
	 */
	@GetMapping("/bikes/{id}")
	public Bike getBikeById(@PathVariable String id)
	{
		return bikeService.getBikeById(id);
	}

}
