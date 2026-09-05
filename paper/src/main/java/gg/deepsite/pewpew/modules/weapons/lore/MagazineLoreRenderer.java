package gg.deepsite.pewpew.modules.weapons.lore;

import gg.deepsite.pewpew.PewpewPlugin;
import gg.deepsite.pewpew.api.objects.PewPewItem;
import gg.deepsite.pewpew.api.objects.PewpewMagazineItem;
import gg.deepsite.pewpew.modules.items.ItemsModule;
import gg.deepsite.pewpew.modules.weapons.magazine.MagazineUtil;
import gg.deepsite.pewpew.utils.ChatUtils;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class MagazineLoreRenderer {

	public static void apply(ItemStack stack, PewpewMagazineItem magazine) {
		ItemMeta meta = stack.getItemMeta();
		if (meta == null) return;

		int rounds = MagazineUtil.rounds(stack);

		List<Component> lore = new ArrayList<>();
		if (magazine.getLore() != null) {
			for (String flavor : magazine.getLore()) lore.add(line(flavor));
		}
		lore.add(Component.empty());
		lore.add(ChatUtils.format("<gray>Rounds <dark_gray>┃ <color>%1<gray>/%2",
				ChatUtils.PRIMARY, rounds, magazine.getCapacity()).decoration(TextDecoration.ITALIC, false));
		lore.add(line("<gray>Ammo Type <dark_gray>┃ <gray>" + magazine.getAmmoType()));

		String loadedName = loadedName(stack);
		if (loadedName != null) lore.add(line("<gray>Loaded <dark_gray>┃ <reset>" + loadedName));

		meta.lore(lore);
		if (meta instanceof Damageable damageable) {
			damageable.setMaxDamage(Math.max(2, magazine.getCapacity() + 1));
			damageable.setDamage(magazine.getCapacity() - rounds);
		}
		stack.setItemMeta(meta);
	}

	private static String loadedName(ItemStack stack) {
		String ammoId = MagazineUtil.ammoId(stack);
		if (ammoId == null) return null;
		PewPewItem ammo = PewpewPlugin.getModuleManager().get(ItemsModule.class).get(ammoId);
		return ammo != null ? ammo.getName() : ammoId;
	}

	private static Component line(String miniMessage) {
		return ChatUtils.format(miniMessage).decoration(TextDecoration.ITALIC, false);
	}
}
