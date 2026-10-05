package gg.deepsite.pewpew.integrations;

import gg.deepsite.pewpew.PewpewPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Display;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;

public final class AxiomIntegration {

	private static Object api;
	private static Method hideDisplayGizmoMethod;

	private AxiomIntegration() {
	}

	public static void init() {
		api = null;
		hideDisplayGizmoMethod = null;
		Plugin plugin = Bukkit.getPluginManager().getPlugin("AxiomPaper");
		if (plugin == null || !plugin.isEnabled()) return;
		try {
			Class<?> apiClass = Class.forName("com.moulberry.axiom.paperapi.AxiomEntityAPI", true,
					plugin.getClass().getClassLoader());
			api = apiClass.getMethod("getAPI").invoke(null);
			hideDisplayGizmoMethod = apiClass.getMethod("hideDisplayGizmo", Display.class);
			PewpewPlugin.getInstance().getLogger().info("Hooked into AxiomPaper.");
		} catch (ReflectiveOperationException | LinkageError exception) {
			api = null;
			PewpewPlugin.getInstance().getLogger().warning("Failed to hook into AxiomPaper: " + exception);
		}
	}

	public static void hideDisplayGizmo(@NotNull Display display) {
		if (hideDisplayGizmoMethod == null) return;
		if (!PewpewPlugin.getDefaultConfiguration().isAxiomEnabled()) return;
		try {
			hideDisplayGizmoMethod.invoke(api, display);
		} catch (ReflectiveOperationException | LinkageError exception) {
			hideDisplayGizmoMethod = null;
			api = null;
			PewpewPlugin.getInstance().getLogger()
					.warning("Disabled AxiomPaper hook after failing to hide a display gizmo: " + exception);
		}
	}
}
