package gg.deepsite.pewpew.modules.weapons;

import gg.deepsite.pewpew.api.objects.PewpewGunItem;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.UseCooldown;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.key.Key;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

@UtilityClass
public class CooldownUtil {

	@NotNull
	public static Key group(@NotNull PewpewGunItem gun) {
		return Key.key("pewpew", "gun_" + gun.getId().toLowerCase(Locale.ROOT));
	}

	public static void stamp(@NotNull ItemStack stack, @NotNull PewpewGunItem gun) {
		float seconds = Math.max(1, gun.getFireRate()) / 20.0f;
		stack.setData(DataComponentTypes.USE_COOLDOWN,
				UseCooldown.useCooldown(seconds).cooldownGroup(group(gun)));
	}
}
