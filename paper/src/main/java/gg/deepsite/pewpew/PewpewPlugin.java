package gg.deepsite.pewpew;

import com.jazzkuh.commandlib.spigot.SpigotCommandLoader;
import com.jazzkuh.modulemanager.spigot.SpigotModuleManager;
import gg.deepsite.pewpew.configuration.DefaultConfiguration;
import gg.deepsite.pewpew.configuration.MessagesConfig;
import gg.deepsite.pewpew.configuration.SoundsConfig;
import gg.deepsite.pewpew.integrations.WorldGuardIntegration;
import gg.deepsite.pewpew.utils.ChatUtils;
import gg.deepsite.pewpew.utils.PewpewLog;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
public final class PewpewPlugin extends JavaPlugin {

	@Getter
	private static PewpewPlugin instance;

	@Getter
	@Setter(AccessLevel.PRIVATE)
	private static SpigotModuleManager<@NotNull PewpewPlugin> moduleManager;

	@Getter
	@Setter
	private static DefaultConfiguration defaultConfiguration;

	@Getter
	@Setter
	private static MessagesConfig messagesConfig;

	@Getter
	@Setter
	private static SoundsConfig soundsConfig;

	public PewpewPlugin() {
		instance = this;
		PewpewLog.set(getLogger());
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
		messagesConfig = new MessagesConfig(this.getDataFolder());
		soundsConfig = new SoundsConfig(this.getDataFolder());

		WorldGuardIntegration.register();

		moduleManager.scanModules(this.getClass());
		moduleManager.load();

	}

	@Override
	public void onDisable() {

		moduleManager.disable();

	}
}
