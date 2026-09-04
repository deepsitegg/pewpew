package gg.deepsite.pewpew.modules.weapons.shooting;

import gg.deepsite.pewpew.api.objects.PewpewGunItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface ShotExecutor {

	void execute(@NotNull Player shooter, @NotNull PewpewGunItem gun, @NotNull ItemStack weapon);
}
