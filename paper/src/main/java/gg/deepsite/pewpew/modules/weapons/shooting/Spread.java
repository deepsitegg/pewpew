package gg.deepsite.pewpew.modules.weapons.shooting;

import gg.deepsite.pewpew.api.objects.PewpewGunItem;
import gg.deepsite.pewpew.api.objects.SpreadModifiers;
import gg.deepsite.pewpew.modules.weapons.attachment.AttachmentUtil;
import gg.deepsite.pewpew.shooting.SpreadMath;
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
		return SpreadMath.stateMultiplier(modifiers, sprinting, moving, sneaking, midair, inWater);
	}

	public static double bloom(@NotNull Player shooter, @NotNull PewpewGunItem gun) {
		return SpreadMath.bloom(shooter.getUniqueId(), gun.getBloomDecay(), Bukkit.getCurrentTick());
	}

	public static void addShot(@NotNull Player shooter, @NotNull PewpewGunItem gun) {
		SpreadMath.addShot(shooter.getUniqueId(), gun.getBloomPerShot(), gun.getBloomMax(),
				gun.getBloomDecay(), Bukkit.getCurrentTick());
	}

	public static double decay(double value, double ticksElapsed, double perTick) {
		return SpreadMath.decay(value, ticksElapsed, perTick);
	}

	public static void clear(@NotNull UUID id) {
		SpreadMath.clear(id);
	}

	public static void clearAll() {
		SpreadMath.clearAll();
	}

	private static boolean isMoving(@NotNull Player shooter) {
		var velocity = shooter.getVelocity();
		return velocity.getX() * velocity.getX() + velocity.getZ() * velocity.getZ() > MOVING_SPEED_SQUARED;
	}
}
