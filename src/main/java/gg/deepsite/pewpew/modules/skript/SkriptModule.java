package gg.deepsite.pewpew.modules.skript;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;
import com.jazzkuh.modulemanager.spigot.SpigotModule;
import com.jazzkuh.modulemanager.spigot.SpigotModuleManager;
import gg.deepsite.pewpew.PewpewPlugin;
import lombok.Getter;
import org.bukkit.Bukkit;

@Getter
@SuppressWarnings({"unused", "deprecation", "removal"})
public class SkriptModule extends SpigotModule<PewpewPlugin> {

	private SkriptAddon addon;

	public SkriptModule(SpigotModuleManager<PewpewPlugin> moduleManager) {
		super(moduleManager);
	}

	@Override
	public void onEnable() {
		if (!Bukkit.getServer().getPluginManager().isPluginEnabled("Skript")) return;

		addon = Skript.registerAddon(PewpewPlugin.getInstance());
		try {

			addon.loadClasses("gg.deepsite.pewpew.modules.skript", "expressions");
			addon.loadClasses("gg.deepsite.pewpew.modules.skript", "effects");
			addon.loadClasses("gg.deepsite.pewpew.modules.skript", "events");

		} catch (Throwable t) {
			PewpewPlugin.getInstance().getLogger().severe("Failed to register Pewpew Skript syntax: " + t);
		}
	}
}
