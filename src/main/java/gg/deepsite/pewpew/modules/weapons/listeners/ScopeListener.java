package gg.deepsite.pewpew.modules.weapons.listeners;

import gg.deepsite.pewpew.PewpewPlugin;
import gg.deepsite.pewpew.api.enums.AttachmentType;
import gg.deepsite.pewpew.api.objects.PewPewItem;
import gg.deepsite.pewpew.api.objects.PewpewGunItem;
import gg.deepsite.pewpew.api.objects.attachment.PewpewScopeAttachment;
import gg.deepsite.pewpew.modules.items.ItemsModule;
import gg.deepsite.pewpew.modules.weapons.attachment.AttachmentUtil;
import gg.deepsite.pewpew.modules.weapons.shooting.ScopeState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class ScopeListener implements Listener {

	private static final double ZOOM_TO_AMPLIFIER = 4.0;
	private static final int MAX_AMPLIFIER = 9;

	private static ItemsModule itemsModule() {
		return PewpewPlugin.getModuleManager().get(ItemsModule.class);
	}

	@EventHandler
	public void onToggleSneak(PlayerToggleSneakEvent event) {
		Player player = event.getPlayer();
		if (event.isSneaking()) {
			PewpewScopeAttachment scope = scopeOf(player.getInventory().getItemInMainHand());
			if (scope != null) scopeIn(player, scope);
		} else {
			scopeOut(player);
		}
	}

	@EventHandler
	public void onItemHeld(PlayerItemHeldEvent event) {
		scopeOut(event.getPlayer());
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		ScopeState.setScoped(event.getPlayer(), false);
	}

	private void scopeIn(Player player, PewpewScopeAttachment scope) {
		int amplifier = Math.min(MAX_AMPLIFIER, (int) Math.round((scope.getZoom() - 1.0) * ZOOM_TO_AMPLIFIER));
		if (amplifier < 0) return;
		player.addPotionEffect(new PotionEffect(
				PotionEffectType.SLOWNESS, PotionEffect.INFINITE_DURATION, amplifier, false, false, false));
		ScopeState.setScoped(player, true);
	}

	private void scopeOut(Player player) {
		if (ScopeState.isScoped(player)) {
			ScopeState.setScoped(player, false);
			player.removePotionEffect(PotionEffectType.SLOWNESS);
		}
	}

	private PewpewScopeAttachment scopeOf(ItemStack held) {
		if (held == null) return null;
		PewPewItem item = itemsModule().fromItemStack(held);
		if (!(item instanceof PewpewGunItem)) return null;
		return AttachmentUtil.get(held, AttachmentType.SCOPE) instanceof PewpewScopeAttachment scope ? scope : null;
	}
}
