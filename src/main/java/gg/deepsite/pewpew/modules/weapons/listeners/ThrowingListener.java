package gg.deepsite.pewpew.modules.weapons.listeners;

import gg.deepsite.pewpew.PewpewPlugin;
import gg.deepsite.pewpew.api.objects.PewPewItem;
import gg.deepsite.pewpew.api.objects.PewpewThrowableItem;
import gg.deepsite.pewpew.modules.items.ItemsModule;
import gg.deepsite.pewpew.modules.weapons.throwing.ThrowableHandler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class ThrowingListener implements Listener {

	private final ThrowableHandler throwableHandler;

	public ThrowingListener(ThrowableHandler throwableHandler) {
		this.throwableHandler = throwableHandler;
	}

	private static ItemsModule itemsModule() {
		return PewpewPlugin.getModuleManager().get(ItemsModule.class);
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent event) {
		if (event.getHand() != EquipmentSlot.HAND) return;
		if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

		ItemStack held = event.getItem();
		if (held == null) return;

		PewPewItem item = itemsModule().fromItemStack(held);
		if (!(item instanceof PewpewThrowableItem throwable)) return;

		event.setCancelled(true);
		throwableHandler.tryThrow(event.getPlayer(), throwable, held);
	}
}
