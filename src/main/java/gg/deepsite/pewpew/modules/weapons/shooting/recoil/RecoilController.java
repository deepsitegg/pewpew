package gg.deepsite.pewpew.modules.weapons.shooting.recoil;

import gg.deepsite.pewpew.api.objects.RecoilProfile;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class RecoilController {

	private static final float EPSILON = 0.01f;
	private static final int HISTORY = 20;

	private final Deque<float[]> commanded = new ArrayDeque<>();

	private final Player player;
	private final RecoilProfile profile;

	private float currentYaw;
	private float currentPitch;
	private float targetYaw;
	private float targetPitch;
	private float keptYaw;
	private float keptPitch;
	private float appliedYaw;
	private float appliedPitch;
	private int shot;
	private int idleTicks;

	public RecoilController(@NotNull Player player, @NotNull RecoilProfile profile) {
		this.player = player;
		this.profile = profile;
	}

	public boolean uses(@NotNull RecoilProfile other) {
		return profile == other;
	}

	public void kick(double degrees) {
		if (degrees <= 0) return;

		idleTicks = 0;
		float vertical;
		float horizontal;

		if (profile.hasPattern()) {
			List<float[]> pattern = profile.getPattern();
			float[] step = pattern.get(patternIndex(shot, pattern.size(), profile.isPatternLoop()));
			horizontal = (float) degrees * step[0];
			vertical = (float) degrees * step[1];
			shot++;
		} else {
			vertical = (float) degrees * spread(profile.getVerticalMean(), profile.getVerticalVariance());
			horizontal = (float) degrees * spread(profile.getHorizontalMean(), profile.getHorizontalVariance());
		}

		targetPitch -= vertical;
		targetYaw += horizontal;
		if (profile.getRecovery() > 0f) {
			keptPitch -= vertical * profile.getRecoveryPenalty();
			keptYaw += horizontal * profile.getRecoveryPenalty();
		}

		float length = (float) Math.sqrt(targetYaw * targetYaw + targetPitch * targetPitch);
		float max = profile.getMaxAccumulation();
		if (max > 0 && length > max) {
			float scale = max / length;
			targetYaw *= scale;
			targetPitch *= scale;
		}
	}

	static int patternIndex(int shot, int size, boolean loop) {
		if (size <= 0) return 0;
		if (loop) return Math.floorMod(shot, size);
		return Math.min(shot, size - 1);
	}

	public boolean tick() {
		if (!player.isOnline() || player.isDead()) return true;

		if (++idleTicks > profile.getPatternReset()) shot = 0;

		targetYaw *= (1f - profile.getDamping());
		targetPitch *= (1f - profile.getDamping());

		currentYaw = lerp(currentYaw, targetYaw, profile.getSmoothing());
		currentPitch = lerp(currentPitch, targetPitch, profile.getSmoothing());

		if (profile.getRecovery() > 0f) {
			currentYaw = moveTowards(currentYaw, 0f, profile.getRecovery());
			currentPitch = moveTowards(currentPitch, 0f, profile.getRecovery());
		}

		float deltaYaw = (currentYaw + keptYaw - appliedYaw) * profile.getSpeed();
		float deltaPitch = (currentPitch + keptPitch - appliedPitch) * profile.getSpeed();

		if (Math.abs(deltaYaw) > EPSILON || Math.abs(deltaPitch) > EPSILON) {
			if (!RelativeRotation.apply(player, deltaYaw, deltaPitch)) {
				applyAbsolute(deltaYaw, deltaPitch);
			}
			appliedYaw += deltaYaw;
			appliedPitch += deltaPitch;
		}

		return settled() && (!profile.hasPattern() || shot == 0);
	}

	private void applyAbsolute(float deltaYaw, float deltaPitch) {
		Location loc = player.getLocation();
		float[] base = rebase(loc.getYaw(), loc.getPitch());
		float yaw = Location.normalizeYaw(base[0] + deltaYaw);
		float pitch = Math.max(-90f, Math.min(90f, base[1] + deltaPitch));
		player.setRotation(yaw, pitch);
		commanded.addLast(new float[]{yaw, pitch});
		if (commanded.size() > HISTORY) commanded.removeFirst();
	}

	float[] rebase(float observedYaw, float observedPitch) {
		if (commanded.isEmpty()) {
			float[] start = {observedYaw, observedPitch};
			commanded.addLast(start);
			return start;
		}

		float bestYaw = 0f;
		float bestPitch = 0f;
		float best = Float.MAX_VALUE;
		for (float[] previous : commanded) {
			float diffYaw = Location.normalizeYaw(observedYaw - previous[0]);
			float diffPitch = observedPitch - previous[1];
			float distance = Math.abs(diffYaw) + Math.abs(diffPitch);
			if (distance < best) {
				best = distance;
				bestYaw = diffYaw;
				bestPitch = diffPitch;
			}
		}

		float[] last = commanded.peekLast();
		return new float[]{last[0] + bestYaw, last[1] + bestPitch};
	}

	private boolean settled() {
		return Math.abs(currentYaw + keptYaw - appliedYaw) < EPSILON
				&& Math.abs(currentPitch + keptPitch - appliedPitch) < EPSILON
				&& Math.abs(targetYaw) < EPSILON && Math.abs(targetPitch) < EPSILON
				&& Math.abs(currentYaw) < EPSILON && Math.abs(currentPitch) < EPSILON;
	}

	private static float spread(float mean, float variance) {
		if (variance <= 0f) return mean;
		return mean + (float) (ThreadLocalRandom.current().nextDouble() * 2 - 1) * variance;
	}

	private static float lerp(float from, float to, float t) {
		return from + (to - from) * t;
	}

	private static float moveTowards(float current, float target, float maxDelta) {
		float diff = target - current;
		if (Math.abs(diff) <= maxDelta) return target;
		return current + Math.signum(diff) * maxDelta;
	}
}
