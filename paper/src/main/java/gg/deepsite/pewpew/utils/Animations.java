package gg.deepsite.pewpew.utils;

import gg.deepsite.pewpew.PewpewPlugin;
import gg.deepsite.pewpew.api.enums.AnimationEvent;
import gg.deepsite.pewpew.api.objects.PewpewGunItem;
import gg.deepsite.pewpew.api.objects.PewpewRig;
import gg.deepsite.pewpew.modules.weapons.WeaponsModule;
import lombok.experimental.UtilityClass;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@UtilityClass
public class Animations {

	@Nullable
	private static WeaponsModule module() {
		return PewpewPlugin.getModuleManager().get(WeaponsModule.class);
	}

	public static void play(@NotNull Player player, @NotNull PewpewGunItem gun, @NotNull AnimationEvent event) {
		play(player, gun, event, 0);
	}

	public static void play(@NotNull Player player, @NotNull PewpewGunItem gun, @NotNull AnimationEvent event,
	                        int stretchToTicks) {
		WeaponsModule module = module();
		if (module == null) return;

		PewpewRig rig = gun.getRig(event);
		if (rig != null) {
			int rigTicks = module.getRigAnimator().play(player, rig, stretchToTicks);
			if (rigTicks > 0) {
				module.getAnimationManager().cancel(player.getUniqueId());
				lockWeapon(player, gun, rigTicks);
				return;
			}
		}
		lockWeapon(player, gun, module.getAnimationManager().play(player, gun, event, stretchToTicks));
	}

	private static void lockWeapon(@NotNull Player player, @NotNull PewpewGunItem gun, int ticks) {
		if (ticks <= 0 || !gun.isAnimationCooldown()) return;
		ItemStack held = player.getInventory().getItemInMainHand();
		if (held.getType().isAir()) return;
		if (player.getCooldown(held) < ticks) player.setCooldown(held, ticks);
	}

	public static void cancel(@NotNull UUID id) {
		WeaponsModule module = module();
		if (module == null) return;
		module.getAnimationManager().cancel(id);
		module.getRigAnimator().cancel(id);
	}
}
