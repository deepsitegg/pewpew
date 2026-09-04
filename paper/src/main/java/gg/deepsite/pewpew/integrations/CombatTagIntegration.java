package gg.deepsite.pewpew.integrations;

import gg.deepsite.pewpew.PewpewPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;

public final class CombatTagIntegration {

	private static boolean available = false;
	private static Object tagManager;
	private static Method tagMethod;

	private CombatTagIntegration() {
	}

	public static void init() {
		Plugin plugin = Bukkit.getPluginManager().getPlugin("CombatTagPlus");
		if (plugin == null) return;
		try {
			Method getTagManager = plugin.getClass().getMethod("getTagManager");
			tagManager = getTagManager.invoke(plugin);
			tagMethod = tagManager.getClass().getMethod("tag", Player.class, Player.class);
			available = true;
			PewpewPlugin.getInstance().getLogger().info("Hooked into CombatTagPlus.");
		} catch (Throwable t) {
			PewpewPlugin.getInstance().getLogger().warning("Failed to hook into CombatTagPlus: " + t.getMessage());
		}
	}

	public static void tag(Player victim, Player attacker) {
		if (!available || victim == null || attacker == null) return;
		if (!PewpewPlugin.getDefaultConfiguration().isCombatTagEnabled()) return;
		try {
			tagMethod.invoke(tagManager, victim, attacker);
		} catch (Throwable ignored) {
		}
	}
}
