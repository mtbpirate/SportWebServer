package org.pirate.sportwebserver.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/greetings")
public class GreetingController
{

	@GetMapping("/{name}")
	public Map<String, String> greet(@PathVariable String name)
	{
		return Map.of("message", "Hallo " + name + ", willkommen auf unserem neuen Webserver!");
	}

}