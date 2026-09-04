package gg.deepsite.pewpew.modules.dummy;

import com.jazzkuh.modulemanager.spigot.SpigotModule;
import com.jazzkuh.modulemanager.spigot.SpigotModuleManager;
import gg.deepsite.pewpew.PewpewPlugin;
import gg.deepsite.pewpew.modules.dummy.commands.DummyCommand;
import gg.deepsite.pewpew.modules.dummy.listeners.DummyListener;

@SuppressWarnings("unused")
public class DummyModule extends SpigotModule<PewpewPlugin> {

	public DummyModule(SpigotModuleManager<PewpewPlugin> moduleManager) {
		super(moduleManager);
	}

	@Override
	public void onEnable() {
		registerComponent(new DummyCommand());
		registerComponent(new DummyListener());
	}

	@Override
	public void onDisable() {
		DummyListener.clearDummies();
	}
}
