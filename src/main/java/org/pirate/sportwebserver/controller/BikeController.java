package org.pirate.sportwebserver.controller;

import java.util.List;

import org.pirate.sportwebserver.dto.Bike;
import org.pirate.sportwebserver.service.BikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
	public Bike getBikeById(@PathVariable int id) 
	{
		return bikeService.getBikeById(id);
	}
	
	/**
	 * Neues Bike erstellen
	 */
	@PostMapping("/bikes")
	public Bike createBike(@RequestBody Bike bike) 
	{
		return bikeService.saveBike(bike);
	}
	
	/**
	 * Bike löschen
	 */
	@DeleteMapping("/bikes/{id}")
	public void deleteBike(@PathVariable int id) 
	{
		bikeService.deleteBike(id);
	}
}

