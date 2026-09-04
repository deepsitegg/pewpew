package gg.deepsite.pewpew.shooting;

import lombok.experimental.UtilityClass;

import java.util.concurrent.ThreadLocalRandom;

@UtilityClass
public class DamageMath {

	public static final double HEADSHOT_BAND = 0.3;

	public static boolean isHeadshot(double hitY, double eyeY) {
		return hitY >= eyeY - HEADSHOT_BAND;
	}

	public static double falloffMultiplier(double distance, double start, double end, double minMultiplier) {
		if (end <= start || distance <= start) return 1.0;
		if (distance >= end) return minMultiplier;
		double t = (distance - start) / (end - start);
		return 1.0 + t * (minMultiplier - 1.0);
	}

	public static boolean rollCrit(double chance) {
		return chance > 0 && ThreadLocalRandom.current().nextDouble() < chance;
	}

	public static double blastFactor(double distance, double radius) {
		if (radius <= 0) return 0.0;
		return Math.max(0.0, 1.0 - distance / radius);
	}
}
