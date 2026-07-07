package gg.deepsite.pewpew.modules.weapons;

import com.jazzkuh.inventorylib.objects.Menu;
import com.jazzkuh.modulemanager.spigot.SpigotModule;
import com.jazzkuh.modulemanager.spigot.SpigotModuleManager;
import gg.deepsite.pewpew.PewpewPlugin;
import gg.deepsite.pewpew.integrations.CombatTagIntegration;
import gg.deepsite.pewpew.modules.weapons.listeners.AttachmentListener;
import gg.deepsite.pewpew.modules.weapons.listeners.ScopeListener;
import gg.deepsite.pewpew.modules.weapons.listeners.ShootingListener;
import gg.deepsite.pewpew.modules.weapons.listeners.ThrowingListener;
import gg.deepsite.pewpew.modules.weapons.shooting.ShootingHandler;
import gg.deepsite.pewpew.modules.weapons.throwing.ThrowableHandler;
import lombok.Getter;

@SuppressWarnings("unused")
public class WeaponsModule extends SpigotModule<PewpewPlugin> {

	@Getter
	private ShootingHandler shootingHandler;

	@Getter
	private ThrowableHandler throwableHandler;

	public WeaponsModule(SpigotModuleManager<PewpewPlugin> moduleManager) {
		super(moduleManager);
	}

	@Override
	public void onEnable() {
		Menu.init(getPlugin());
		CombatTagIntegration.init();
		shootingHandler = new ShootingHandler(getPlugin());
		throwableHandler = new ThrowableHandler(getPlugin());
		registerComponent(new ShootingListener(shootingHandler));
		registerComponent(new ThrowingListener(throwableHandler));
		registerComponent(new AttachmentListener());
		registerComponent(new ScopeListener());
	}

	@Override
	public void onDisable() {
		if (shootingHandler != null) {
			shootingHandler.clearCooldowns();
		}
		if (throwableHandler != null) {
			throwableHandler.clearCooldowns();
		}
	}
}
