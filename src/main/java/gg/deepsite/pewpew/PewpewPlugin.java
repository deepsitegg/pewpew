package gg.deepsite.pewpew;

import ch.njol.skript.bstats.bukkit.Metrics;
import com.jazzkuh.commandlib.spigot.SpigotCommandLoader;
import com.jazzkuh.modulemanager.spigot.SpigotModuleManager;
import gg.deepsite.pewpew.configuration.DefaultConfiguration;
import gg.deepsite.pewpew.configuration.MessagesConfig;
import gg.deepsite.pewpew.integrations.WorldGuardIntegration;
import gg.deepsite.pewpew.utils.ChatUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
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

	@Getter @Setter
	private static MessagesConfig messagesConfig;

	public PewpewPlugin() {
		instance = this;
		moduleManager = new SpigotModuleManager<>(this, getComponentLogger());
	}

	@Override
	public void onEnable() {
		moduleManager.setDebug(false);

		int pluginId = 32453;
		Metrics metrics = new Metrics(this, pluginId);

		SpigotCommandLoader.loadResolvers();
		SpigotCommandLoader.setFormattingProvider((commandException, message) -> ChatUtils.prefix(message));

		moduleManager.enable();

	}

	@Override
	public void onLoad() {

		defaultConfiguration = new DefaultConfiguration(this.getDataFolder());
		defaultConfiguration.saveConfiguration();

		messagesConfig = new MessagesConfig(this.getDataFolder());
		messagesConfig.saveConfiguration();

		WorldGuardIntegration.register();

		moduleManager.scanModules(this.getClass());
		moduleManager.load();

	}

	@Override
	public void onDisable() {

		moduleManager.disable();

	}
}
