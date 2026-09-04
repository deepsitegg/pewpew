package gg.deepsite.pewpew.integrations;

import gg.deepsite.pewpew.PewpewPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class WorldGuardIntegration {

	private static boolean active = false;

	private WorldGuardIntegration() {
	}

	public static void register() {
		if (Bukkit.getPluginManager().getPlugin("WorldGuard") == null) return;
		try {
			WorldGuardFlags.registerFlag();
			active = true;
			PewpewPlugin.getInstance().getLogger().info("Hooked into WorldGuard (region flag 'pewpew-guns').");
		} catch (Throwable t) {
			PewpewPlugin.getInstance().getLogger().warning("Failed to hook into WorldGuard: " + t.getMessage());
		}
	}

	public static boolean allows(Player player) {
		if (!active) return true;
		if (!PewpewPlugin.getDefaultConfiguration().isWorldGuardEnabled()) return true;
		try {
			return WorldGuardFlags.allows(player);
		} catch (Throwable t) {
			return true;
		}
	}
}
