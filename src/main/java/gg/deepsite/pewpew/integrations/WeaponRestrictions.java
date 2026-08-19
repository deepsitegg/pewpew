package gg.deepsite.pewpew.integrations;

import gg.deepsite.pewpew.PewpewPlugin;
import gg.deepsite.pewpew.api.objects.PewPewItem;
import gg.deepsite.pewpew.modules.items.ItemsModule;
import gg.deepsite.pewpew.utils.ChatUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public final class WeaponRestrictions {

	private WeaponRestrictions() {
	}

	public static boolean denied(Player player, boolean sendMessage) {
		return denied(player, null, sendMessage);
	}

	public static boolean denied(Player player, @Nullable PewPewItem item, boolean sendMessage) {
		String message = null;
		if (item != null && !player.hasPermission(ItemsModule.USE_PERMISSION_PREFIX + item.getId())) {
			message = PewpewPlugin.getMessagesConfig().noPermission();
		} else if (!WorldGuardIntegration.allows(player)) {
			message = PewpewPlugin.getMessagesConfig().worldGuardDeny();
		} else if (OpenMinetopiaIntegration.isHandcuffed(player)) {
			message = PewpewPlugin.getMessagesConfig().openMinetopiaHandcuffedDeny();
		} else if (OpenMinetopiaIntegration.isPlaceBanned(player)) {
			message = PewpewPlugin.getMessagesConfig().openMinetopiaPlaceDeny();
		}

		if (message == null) return false;
		if (sendMessage) player.sendActionBar(ChatUtils.format(message));
		return true;
	}
}
