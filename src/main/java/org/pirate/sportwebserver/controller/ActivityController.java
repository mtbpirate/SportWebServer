package org.pirate.sportwebserver.controller;

import org.pirate.sportwebserver.dto.strava.StravaActivity;
import org.pirate.sportwebserver.service.ActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ActivityController
{
	@Autowired
	private ActivityService activityService;

	/**
	 *
	 */
	@GetMapping("/activitys")
	public ResponseEntity<List<StravaActivity>> getActivities(
		@RequestParam(required = false) LocalDate vonDatum,
		@RequestParam(required = false) LocalDate bisDatum,
		@RequestParam(required = false) String sportart,
		@RequestParam(required = false) String textfilter)
	{
		List<StravaActivity> activities = activityService.getActivities(vonDatum, bisDatum, sportart, textfilter);
		if (activities.isEmpty())
		{
			return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		}

		return ResponseEntity.ok(activities);
	}

	/**
	 * Tour nach ID abrufen
	 */
	@GetMapping("/activity/{tourId}")
	public ResponseEntity<StravaActivity> getTourById(@PathVariable long tourId)
	{
		StravaActivity activity = activityService.getActivityById(tourId);
		if (activity == null)
		{
			return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		}

		return ResponseEntity.ok(activity);
	}

}
