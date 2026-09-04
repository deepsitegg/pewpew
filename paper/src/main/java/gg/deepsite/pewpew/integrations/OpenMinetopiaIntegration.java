package gg.deepsite.pewpew.integrations;

import gg.deepsite.pewpew.PewpewPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.List;

public final class OpenMinetopiaIntegration {

	private static boolean available = false;
	private static boolean broken = false;
	private static Object handcuffManager;
	private static Method isHandcuffedMethod;
	private static Object placeManager;
	private static Method getPlaceMethod;
	private static Method getPlaceNameMethod;

	private OpenMinetopiaIntegration() {
	}

	public static void init() {
		if (Bukkit.getPluginManager().getPlugin("OpenMinetopia") == null) return;
		try {
			Class<?> handcuffClass = Class.forName("nl.openminetopia.modules.police.handcuff.HandcuffManager");
			handcuffManager = handcuffClass.getMethod("getInstance").invoke(null);
			isHandcuffedMethod = handcuffClass.getMethod("isHandcuffed", Player.class);

			Class<?> placeManagerClass = Class.forName("nl.openminetopia.api.places.MTPlaceManager");
			placeManager = placeManagerClass.getMethod("getInstance").invoke(null);
			getPlaceMethod = placeManagerClass.getMethod("getPlace", Location.class);
			getPlaceNameMethod = Class.forName("nl.openminetopia.api.places.objects.MTPlace").getMethod("getName");

			available = true;
			PewpewPlugin.getInstance().getLogger().info("Hooked into OpenMinetopia.");
		} catch (Throwable t) {
			PewpewPlugin.getInstance().getLogger().warning("Failed to hook into OpenMinetopia: " + t.getMessage());
		}
	}

	public static boolean isHandcuffed(Player player) {
		if (!enabled() || !PewpewPlugin.getDefaultConfiguration().isOpenMinetopiaBlockHandcuffed()) return false;
		try {
			return (boolean) isHandcuffedMethod.invoke(handcuffManager, player);
		} catch (Throwable t) {
			return fail(t);
		}
	}

	public static boolean isPlaceBanned(Player player) {
		if (!enabled()) return false;
		List<String> banned = PewpewPlugin.getDefaultConfiguration().getOpenMinetopiaBannedPlaces();
		if (banned.isEmpty()) return false;
		try {
			Object place = getPlaceMethod.invoke(placeManager, player.getLocation());
			if (place == null) return false;
			String name = (String) getPlaceNameMethod.invoke(place);
			return name != null && banned.stream().anyMatch(name::equalsIgnoreCase);
		} catch (Throwable t) {
			return fail(t);
		}
	}

	private static boolean fail(Throwable t) {
		if (!broken) {
			broken = true;
			PewpewPlugin.getInstance().getLogger().severe("The OpenMinetopia hook stopped working (" + t
					+ "). Blocking weapon use until this is fixed, or disable the hook in config.yml.");
		}
		return true;
	}

	private static boolean enabled() {
		return available && PewpewPlugin.getDefaultConfiguration().isOpenMinetopiaEnabled();
	}
}
