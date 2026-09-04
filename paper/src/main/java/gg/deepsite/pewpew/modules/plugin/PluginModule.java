package gg.deepsite.pewpew.modules.plugin;

import com.jazzkuh.modulemanager.spigot.SpigotModule;
import com.jazzkuh.modulemanager.spigot.SpigotModuleManager;
import gg.deepsite.pewpew.PewpewPlugin;
import gg.deepsite.pewpew.modules.plugin.commands.PewpewCommand;
import gg.deepsite.pewpew.modules.plugin.listeners.UpdateNotifyListener;
import gg.deepsite.pewpew.utils.UpdateChecker;

@SuppressWarnings("unused")
public class PluginModule extends SpigotModule<PewpewPlugin> {

	public PluginModule(SpigotModuleManager<PewpewPlugin> moduleManager) {
		super(moduleManager);
	}

	@Override
	public void onEnable() {

		registerComponent(new PewpewCommand());
		registerComponent(new UpdateNotifyListener());

		UpdateChecker.check(getPlugin());

	}

	@Override
	public void onDisable() {

	}
}
