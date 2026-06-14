package gg.deepsite.pewpew;

import com.jazzkuh.commandlib.spigot.SpigotCommandLoader;
import com.jazzkuh.modulemanager.spigot.SpigotModuleManager;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import gg.deepsite.pewpew.configuration.DefaultConfiguration;
import gg.deepsite.pewpew.utils.ChatUtils;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

@Getter @Setter
public final class PewpewPlugin extends JavaPlugin {

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

		moduleManager.scanModules(this.getClass());
		moduleManager.load();

	}

	@Override
	public void onDisable() {

		moduleManager.disable();

	}
}
