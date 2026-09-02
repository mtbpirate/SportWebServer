package org.pirate.sportwebserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SportWebserverApplication
{
	public static void main(String[] args)
	{
		SpringApplication.run(SportWebserverApplication.class, args);
	}
}
