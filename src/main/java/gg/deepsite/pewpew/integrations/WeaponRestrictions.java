package gg.deepsite.pewpew.integrations;

import gg.deepsite.pewpew.PewpewPlugin;
import gg.deepsite.pewpew.utils.ChatUtils;
import org.bukkit.entity.Player;

public final class WeaponRestrictions {

	private WeaponRestrictions() {
	}

	public static boolean denied(Player player, boolean sendMessage) {
		String message = null;
		if (!WorldGuardIntegration.allows(player)) {
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
