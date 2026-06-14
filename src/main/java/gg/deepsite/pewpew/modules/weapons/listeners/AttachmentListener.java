package gg.deepsite.pewpew.modules.weapons.listeners;

import gg.deepsite.pewpew.PewpewPlugin;
import gg.deepsite.pewpew.api.objects.PewPewItem;
import gg.deepsite.pewpew.api.objects.PewpewGunItem;
import gg.deepsite.pewpew.modules.items.ItemsModule;
import gg.deepsite.pewpew.modules.weapons.attachment.menu.AttachmentMenu;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class AttachmentListener implements Listener {

	private static ItemsModule itemsModule() {
		return PewpewPlugin.getModuleManager().get(ItemsModule.class);
	}

	@EventHandler
	public void onSwapInInventory(InventoryClickEvent event) {
		if (event.getClick() != ClickType.SWAP_OFFHAND) return;
		if (!(event.getWhoClicked() instanceof Player player)) return;

		Inventory clicked = event.getClickedInventory();
		if (!(clicked instanceof PlayerInventory) || !clicked.equals(player.getInventory())) return;

		ItemStack hovered = event.getCurrentItem();
		if (hovered == null) return;

		PewPewItem item = itemsModule().fromItemStack(hovered);
		if (!(item instanceof PewpewGunItem)) return;

		event.setCancelled(true);
		int gunSlot = event.getSlot();
		PewpewPlugin.getInstance().getServer().getScheduler().runTask(
				PewpewPlugin.getInstance(), () -> new AttachmentMenu(player, gunSlot).open(player));
	}
}
