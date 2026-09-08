package gg.deepsite.pewpew.modules.skript;

import gg.deepsite.pewpew.PewpewPlugin;
import gg.deepsite.pewpew.api.objects.PewPewItem;
import gg.deepsite.pewpew.api.objects.PewpewGunItem;
import gg.deepsite.pewpew.modules.items.ItemsModule;
import lombok.experimental.UtilityClass;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@UtilityClass
public class SkriptGuns {

	@Nullable
	public static PewpewGunItem gunOf(@Nullable ItemStack stack) {
		if (stack == null) return null;
		ItemsModule items = PewpewPlugin.getModuleManager().get(ItemsModule.class);
		if (items == null) return null;
		PewPewItem item = items.fromItemStack(stack);
		return item instanceof PewpewGunItem gun ? gun : null;
	}

	@Nullable
	public static PewpewGunItem heldGun(@NotNull Player player) {
		return gunOf(player.getInventory().getItemInMainHand());
	}
}
