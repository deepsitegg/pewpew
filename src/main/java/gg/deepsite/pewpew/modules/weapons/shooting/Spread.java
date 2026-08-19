package gg.deepsite.pewpew.modules.weapons.shooting;

import gg.deepsite.pewpew.api.objects.PewpewGunItem;
import gg.deepsite.pewpew.api.objects.SpreadModifiers;
import gg.deepsite.pewpew.modules.weapons.attachment.AttachmentUtil;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@UtilityClass
public class Spread {

	private static final double MOVING_SPEED_SQUARED = 0.0009;

	private static final Map<UUID, double[]> BLOOM = new ConcurrentHashMap<>();

	public static double effective(@NotNull Player shooter, @NotNull PewpewGunItem gun,
	                               @NotNull ItemStack weapon, boolean scoped) {
		double spread = gun.getSpread() * AttachmentUtil.recoilMultiplier(weapon);
		if (scoped) spread *= AttachmentUtil.aimSpreadMultiplier(weapon);
		spread *= stateMultiplier(gun.getSpreadModifiers(), shooter);
		return spread + bloom(shooter, gun);
	}

	public static double stateMultiplier(@Nullable SpreadModifiers modifiers, @NotNull Player shooter) {
		return stateMultiplier(modifiers, shooter.isSprinting(), isMoving(shooter), shooter.isSneaking(),
				!shooter.isOnGround(), shooter.isInWater());
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

	public static double bloom(@NotNull Player shooter, @NotNull PewpewGunItem gun) {
		double[] state = BLOOM.get(shooter.getUniqueId());
		if (state == null) return 0.0;
		return decay(state[0], Bukkit.getCurrentTick() - state[1], gun.getBloomDecay());
	}

	public static void addShot(@NotNull Player shooter, @NotNull PewpewGunItem gun) {
		if (gun.getBloomPerShot() <= 0) return;

		double tick = Bukkit.getCurrentTick();
		double grown = bloom(shooter, gun) + gun.getBloomPerShot();
		double max = gun.getBloomMax();
		if (max > 0 && grown > max) grown = max;
		BLOOM.put(shooter.getUniqueId(), new double[]{grown, tick});
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

	private static boolean isMoving(@NotNull Player shooter) {
		var velocity = shooter.getVelocity();
		return velocity.getX() * velocity.getX() + velocity.getZ() * velocity.getZ() > MOVING_SPEED_SQUARED;
	}
}
