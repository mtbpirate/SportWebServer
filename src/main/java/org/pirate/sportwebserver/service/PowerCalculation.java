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

			calibratePower(activity.getAverageWatts(), trackPoints);

		}
		else
		{
			log.info("PowerCalculation - Skipping power calculation for activity {} as device watts are available and forceRecalculation is false",
				activity.getId());
		}

	}

	private static void calibratePower(Double averageWatts, List<StravaTrackPoint> trackPoints)
	{
		if (averageWatts == null || trackPoints == null || trackPoints.isEmpty())
		{
			return;
		}

		// compute current mean of existing watt values (ignore nulls)
		double sum = 0.0;
		int count = 0;
		for (StravaTrackPoint p : trackPoints)
		{
			Double w = p.getWatts();
			if (w != null)
			{
				sum += w;
				count++;
			}
		}

		if (count == 0)
		{
			return;
		}

		double currentMean = sum / count;

		// If current mean is zero, set all non-null watts to the target average (rounded)
		if (currentMean == 0.0)
		{
			for (StravaTrackPoint p : trackPoints)
			{
				if (p.getWatts() != null)
				{
					p.setWatts(averageWatts);
				}
			}
			log.info("calibratePower - current mean was 0, set {} points to {}", count, averageWatts);
			return;
		}

		double factor = averageWatts / currentMean;
		if (Double.isNaN(factor) || Double.isInfinite(factor))
		{
			return;
		}

		// scale each watt value by the factor and ensure non-negative integers
		for (StravaTrackPoint p : trackPoints)
		{
			Double w = p.getWatts();
			if (w != null)
			{
				Double newW = Math.max(0, w * factor);
				p.setWatts(newW);
			}
		}

		// optional verification log: compute new mean
		double newSum = 0.0;
		for (StravaTrackPoint p : trackPoints)
		{
			Double w = p.getWatts();
			if (w != null)
				newSum += w;
		}
		double newMean = newSum / count;
		log.info("calibratePower - scaled watts: oldMean={} target={} factor={} newMean={}", currentMean, averageWatts, factor, newMean);
	}

	private static Double calculatePower(StravaTrackPoint point, float gewicht, float cda, float crr)
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

			return power;
		}
		catch (Exception e)
		{
			return null;
		}
	}

}
