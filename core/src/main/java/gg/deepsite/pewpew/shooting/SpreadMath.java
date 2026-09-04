package gg.deepsite.pewpew.shooting;

import gg.deepsite.pewpew.api.objects.SpreadModifiers;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@UtilityClass
public class SpreadMath {

	private static final Map<UUID, double[]> BLOOM = new ConcurrentHashMap<>();

	@NotNull
	public static Vec3 applySpread(@NotNull Vec3 direction, double spreadDegrees, boolean legacy) {
		if (spreadDegrees <= 0) return direction;
		ThreadLocalRandom random = ThreadLocalRandom.current();
		if (legacy) {
			double yaw = Math.toRadians((random.nextDouble() * 2 - 1) * spreadDegrees);
			double pitch = Math.toRadians((random.nextDouble() * 2 - 1) * spreadDegrees);
			return direction.rotateAroundY(yaw).rotateAroundX(pitch).normalize();
		}
		double angle = Math.toRadians(spreadDegrees) * Math.sqrt(random.nextDouble());
		double azimuth = random.nextDouble() * 2 * Math.PI;
		return cone(direction, angle, azimuth);
	}

	@NotNull
	public static Vec3 cone(@NotNull Vec3 direction, double angle, double azimuth) {
		Vec3 forward = direction.normalize();
		Vec3 reference = Math.abs(forward.y()) > 0.999 ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
		Vec3 right = forward.cross(reference).normalize();
		Vec3 up = right.cross(forward).normalize();
		double radial = Math.sin(angle);
		return forward.multiply(Math.cos(angle))
				.add(right.multiply(Math.cos(azimuth) * radial))
				.add(up.multiply(Math.sin(azimuth) * radial))
				.normalize();
	}

	public static double stateMultiplier(@Nullable SpreadModifiers modifiers, boolean sprinting, boolean moving,
	                                     boolean sneaking, boolean midair, boolean inWater) {
		SpreadModifiers m = modifiers != null ? modifiers : SpreadModifiers.DEFAULT;

		double multiplier;
		if (sprinting) multiplier = m.getSprinting();
		else if (sneaking) multiplier = m.getSneaking();
		else if (moving) multiplier = m.getWalking();
		else multiplier = m.getStanding();

		if (midair) multiplier *= m.getMidair();
		if (inWater) multiplier *= m.getInWater();
		return multiplier;
	}

	public static double bloom(@NotNull UUID shooter, double decayPerTick, long currentTick) {
		double[] state = BLOOM.get(shooter);
		if (state == null) return 0.0;
		return decay(state[0], currentTick - state[1], decayPerTick);
	}

	public static void addShot(@NotNull UUID shooter, double perShot, double max, double decayPerTick, long currentTick) {
		if (perShot <= 0) return;
		double grown = bloom(shooter, decayPerTick, currentTick) + perShot;
		if (max > 0 && grown > max) grown = max;
		BLOOM.put(shooter, new double[]{grown, currentTick});
	}

	public static double decay(double value, double ticksElapsed, double perTick) {
		if (value <= 0) return 0.0;
		if (perTick <= 0) return value;
		return Math.max(0.0, value - perTick * Math.max(0, ticksElapsed));
	}

	public static void clear(@NotNull UUID id) {
		BLOOM.remove(id);
	}

	public static void clearAll() {
		BLOOM.clear();
	}
}
