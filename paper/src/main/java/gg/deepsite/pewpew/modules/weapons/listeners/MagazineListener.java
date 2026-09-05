package gg.deepsite.pewpew.modules.weapons.listeners;

import gg.deepsite.pewpew.PewpewPlugin;
import gg.deepsite.pewpew.api.objects.PewPewItem;
import gg.deepsite.pewpew.api.objects.PewpewAmmoItem;
import gg.deepsite.pewpew.api.objects.PewpewMagazineItem;
import gg.deepsite.pewpew.modules.items.ItemsModule;
import gg.deepsite.pewpew.modules.weapons.magazine.MagazineUtil;
import gg.deepsite.pewpew.utils.ChatUtils;
import gg.deepsite.pewpew.api.enums.SoundEvent;
import gg.deepsite.pewpew.utils.Sounds;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class MagazineListener implements Listener {

	@EventHandler
	public void onFillMagazine(InventoryClickEvent event) {
		if (!MagazineUtil.enabled()) return;
		if (!(event.getWhoClicked() instanceof Player player)) return;

		ItemStack cursor = event.getCursor();
		ItemStack clicked = event.getCurrentItem();
		if (cursor == null || clicked == null || clicked.getAmount() != 1) return;

		PewpewMagazineItem magazine = MagazineUtil.defOf(clicked);
		if (magazine == null) return;

		PewPewItem held = PewpewPlugin.getModuleManager().get(ItemsModule.class).fromItemStack(cursor);
		if (!(held instanceof PewpewAmmoItem ammo) || !magazine.getAmmoType().equals(ammo.getAmmoType())) return;

		event.setCancelled(true);
		int consumed = MagazineUtil.load(clicked, magazine, cursor, ammo);
		if (consumed == MagazineUtil.MISMATCH) {
			player.sendActionBar(ChatUtils.format(PewpewPlugin.getMessagesConfig().magazineAmmoMismatch()));
			return;
		}
		if (consumed <= 0) {
			player.sendActionBar(ChatUtils.format(PewpewPlugin.getMessagesConfig().magazineFull()));
			return;
		}

		player.setItemOnCursor(cursor.getAmount() <= 0 ? null : cursor);
		event.setCurrentItem(clicked);
		player.updateInventory();
		Sounds.at(player, SoundEvent.MAGAZINE_FILL);
	}
}
