package org.pirate.sportwebserver.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
public class SchedulerService
{
	
	private static final Logger log = LoggerFactory.getLogger(SchedulerService.class);

	@Autowired
	private DbConnectionService dbConnection;
	
	@Value("${testvar:default-testvar}")
	private int testvar;
	
	@PostConstruct
	private void init()
	{
		log.info("SchedulerService - init, testvar={}", testvar);
		if(testvar != 3283 )
		{
			log.warn("SchedulerService - Falsch konfiguriert, testvar={}", testvar);
			log.warn("---- Programm wird beendet ----");
			System.exit(1);
			
		}
		
		
		if(!dbConnection.testConnection())
		{
			log.warn("SchedulerService - DB connection test failed");
			log.warn("---- Programm wird beendet ----");
			System.exit(1);
		}
		
		
		log.info("SchedulerService - init completed");
	}
	
	
	@Scheduled(cron = "0 * * * * *")
    public void everyMinute() 
    {
    	log.info("TestService - Running every minute, testvar={}", testvar);
    	testDBConnection();
    }
	
	private void testDBConnection() 
	{
		log.info("Test DB connection every Minute");
		dbConnection.testConnection();
		log.info("TestService - DB connection test completed");
	}
	
}
