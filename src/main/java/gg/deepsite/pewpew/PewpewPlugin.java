package gg.deepsite.pewpew;

import com.jazzkuh.commandlib.spigot.SpigotCommandLoader;
import com.jazzkuh.modulemanager.spigot.SpigotModuleManager;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import gg.deepsite.pewpew.configuration.DefaultConfiguration;
import gg.deepsite.pewpew.modules.dummy.DummyModule;
import gg.deepsite.pewpew.modules.items.ItemsModule;
import gg.deepsite.pewpew.modules.plugin.PluginModule;
import gg.deepsite.pewpew.modules.skript.SkriptModule;
import gg.deepsite.pewpew.modules.weapons.WeaponsModule;
import gg.deepsite.pewpew.utils.ChatUtils;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

@Getter @Setter
public final class
PewpewPlugin extends JavaPlugin {

	@Getter
	private static PewpewPlugin instance;

	@Getter @Setter(AccessLevel.PRIVATE)
	private static SpigotModuleManager<@NotNull PewpewPlugin> moduleManager;

	@Getter @Setter
	private static DefaultConfiguration defaultConfiguration;

	public PewpewPlugin() {
		instance = this;
		moduleManager = new SpigotModuleManager<>(this, getComponentLogger());
	}

	@Override
	public void onEnable() {
		moduleManager.setDebug(false);

		SpigotCommandLoader.loadResolvers();
		SpigotCommandLoader.setFormattingProvider((commandException, message) -> ChatUtils.prefix(message));

		moduleManager.enable();

	}

	@Override
	public void onLoad() {

		defaultConfiguration = new DefaultConfiguration(this.getDataFolder());
		defaultConfiguration.saveConfiguration();

		// Reflections-based scanModules() cannot enumerate the plugin jar under
		// Paper's isolated plugin classloader, so register modules explicitly.
		// ItemsModule must come first: the other modules resolve it at runtime.
		moduleManager.prepare(new ItemsModule(moduleManager));
		moduleManager.prepare(new WeaponsModule(moduleManager));
		moduleManager.prepare(new SkriptModule(moduleManager));
		moduleManager.prepare(new PluginModule(moduleManager));
		moduleManager.prepare(new DummyModule(moduleManager));
		moduleManager.load();

	}

	@Override
	public void onDisable() {

		moduleManager.disable();

	}
}
