package org.pirate.sportwebserver.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/strava/webhook")
public class StravaWebhookController
{

	private static final Logger log = LoggerFactory.getLogger(StravaWebhookController.class);

	@Value("${strava.webhooktoken}")
	private String webhooktoken;

	@GetMapping
	public ResponseEntity<Map<String, Object>> verify(
		@RequestParam("hub.mode") String mode,
		@RequestParam("hub.challenge") String challenge,
		@RequestParam("hub.verify_token") String verifyToken)
	{
		log.info("receive Verify: {}, {}, {}", mode, challenge, verifyToken);

		if (!webhooktoken.equals(verifyToken))
		{
			log.error("Invalid webhook token");
			return ResponseEntity
				.status(HttpStatus.UNAUTHORIZED)
				.body(Map.of("error", "Not authorized"));
		}

		return ResponseEntity.ok(Map.of(
			"hub.challenge", challenge
		));
	}

	@PostMapping
	public ResponseEntity<Void> receiveEvent(
		@RequestBody String payload)
	{

		log.info("receive Event: {}", payload);

		return ResponseEntity.ok().build();
	}
}
