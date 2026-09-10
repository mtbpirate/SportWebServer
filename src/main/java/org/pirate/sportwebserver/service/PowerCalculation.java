package org.pirate.sportwebserver.service;

import org.pirate.sportwebserver.dto.strava.StravaActivity;
import org.pirate.sportwebserver.dto.strava.StravaTrackPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class PowerCalculation
{
	private static final Logger log = LoggerFactory.getLogger(PowerCalculation.class);

	/**
	 * Berechnet die Leistung der übergebenen Trackpoints
	 * @param activity           Strava Activity
	 * @param trackPoints        Liste Trackpoints
	 * @param gewicht            Gewicht Fahrrad + Bike
	 * @param crr                Rollweiderstand
	 * @param cda                Luftweiderstand
	 * @param forceRecalculation true=immer berechnen
	 */
	public static void calulatePower(StravaActivity activity, List<StravaTrackPoint> trackPoints, float gewicht, float crr, float cda, boolean forceRecalculation)
	{

		if (forceRecalculation || !activity.getDeviceWatts())
		{
			for (StravaTrackPoint point : trackPoints)
			{
				point.setWatts(calculatePower(point, gewicht, cda, crr));
			}

		}
		else
		{
			log.info("PowerCalculation - Skipping power calculation for activity {} as device watts are available and forceRecalculation is false",
				activity.getId());
		}

	}

	private static Integer calculatePower(StravaTrackPoint point, float gewicht, float cda, float crr)
	{
		try
		{
			float g = 9.81f; // Acceleration due to gravity in m/s^2
			float rho = 1.226f; // Air density in kg/m^3
			float v = point.getVelocity().floatValue();
			float m = gewicht;
			float grade = point.getGrade().floatValue() / 100.0f; // Convert grade percentage to decimal

			double pHill = m * g * v * grade;
			double pRoll = m * g * crr * v;
			double pAir = 0.5 * rho * cda * Math.pow(v, 3);

			double power = pHill + pRoll + pAir;

			//keine negative Leistung
			if (power < 0)
			{
				power = 0;
			}

			return (int) power;
		}
		catch (Exception e)
		{
			return null;
		}
	}

}
