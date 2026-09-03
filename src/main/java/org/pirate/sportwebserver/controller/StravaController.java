package org.pirate.sportwebserver.controller;

import java.util.List;
import java.util.Map;
import org.pirate.sportwebserver.dto.strava.StravaActivity;
import org.pirate.sportwebserver.dto.strava.StravaAthlete;
import org.pirate.sportwebserver.dto.strava.StravaToken;
import org.pirate.sportwebserver.dto.strava.StravaTrackPoint;
import org.pirate.sportwebserver.service.StravaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequestMapping("/strava")
public class StravaController
{
	private static final Logger log = LoggerFactory.getLogger(StravaController.class);

	@Autowired
	private StravaService stravaService;

	@GetMapping("/authorize")
	public RedirectView authorize()
	{
		String url = stravaService.getAuthorizationUrl();
		log.info("Redirecting to Strava authorize: {}", url);
		RedirectView x = new RedirectView(url);

		return x;
	}

	@GetMapping("/callback")
	public ResponseEntity<?> callback(@RequestParam(name = "code", required = false) String code, @RequestParam(name = "error", required = false) String error)
	{
		if (error != null)
		{
			log.warn("Strava returned error: {}", error);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", error));
		}
		if (code == null)
		{
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "missing code"));
		}
		try
		{
			StravaToken token = stravaService.exchangeCodeForToken(code);
			return ResponseEntity.ok(token);
		}
		catch (Exception e)
		{
			log.error("Failed to exchange code for token", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(Map.of("error", e.getMessage()));
		}
	}

	@GetMapping("/activities")
	public ResponseEntity<?> activities(@RequestParam(name = "page", required = false, defaultValue = "1") int page, @RequestParam(name = "per_page", required = false, defaultValue = "30") int perPage)
	{
		try
		{
			List<StravaActivity> activities = stravaService.getActivities(page, perPage);
			return ResponseEntity.ok(activities);
		}
		catch (Exception e)
		{
			log.error("Failed to get activities", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
		}
	}

	@GetMapping("/token/status")
	public ResponseEntity<?> tokenStatus()
	{
		StravaToken token = stravaService.getCurrentToken();
		if (token == null)
			return ResponseEntity.ok(Map.of("authenticated", false));
		return ResponseEntity.ok(token);
	}

	@GetMapping("/activity/{id}")
	public ResponseEntity<?> getActivity(@PathVariable(name = "id") long activityId)
	{
		try
		{
			StravaActivity activity = stravaService.getActivityById(activityId);
			return ResponseEntity.ok(activity);
		}
		catch (Exception e)
		{
			log.error("Failed to get activity with ID {}", activityId, e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
		}
	}

	@GetMapping("/trackpoints/{id}")
	public ResponseEntity<?> getTrackpoints(@PathVariable(name = "id") long activityId)
	{
		try
		{
			List<StravaTrackPoint> trackpoints = stravaService.getActivityStream(activityId);
			return ResponseEntity.ok(trackpoints);
		}
		catch (Exception e)
		{
			log.error("Failed to get trackpoints with ID {}", activityId, e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
		}
	}


	@GetMapping("/athlete/{id}")
	public ResponseEntity<?> getAthlete(@PathVariable(name = "id") long athleteId)
	{
		try
		{
			StravaAthlete athlete = stravaService.getAthlete(athleteId);
			return ResponseEntity.ok(athlete);
		}
		catch (Exception e)
		{
			log.error("Failed to get athlete with ID {}", athleteId, e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
		}
	}
}