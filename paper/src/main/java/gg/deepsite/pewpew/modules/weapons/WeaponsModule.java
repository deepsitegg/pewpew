package gg.deepsite.pewpew.modules.weapons;

import com.jazzkuh.inventorylib.objects.Menu;
import com.jazzkuh.modulemanager.spigot.SpigotModule;
import com.jazzkuh.modulemanager.spigot.SpigotModuleManager;
import gg.deepsite.pewpew.PewpewPlugin;
import gg.deepsite.pewpew.integrations.CombatTagIntegration;
import gg.deepsite.pewpew.integrations.OpenMinetopiaIntegration;
import gg.deepsite.pewpew.modules.weapons.animation.AnimationManager;
import gg.deepsite.pewpew.modules.weapons.animation.RigAnimator;
import gg.deepsite.pewpew.modules.weapons.listeners.AttachmentListener;
import gg.deepsite.pewpew.modules.weapons.listeners.MagazineListener;
import gg.deepsite.pewpew.modules.weapons.listeners.ScopeListener;
import gg.deepsite.pewpew.modules.weapons.listeners.ShootingListener;
import gg.deepsite.pewpew.modules.weapons.listeners.ThrowingListener;
import gg.deepsite.pewpew.modules.weapons.shooting.BulletImpacts;
import gg.deepsite.pewpew.modules.weapons.shooting.ShootingHandler;
import gg.deepsite.pewpew.modules.weapons.throwing.ThrowableHandler;
import lombok.Getter;

@SuppressWarnings("unused")
public class WeaponsModule extends SpigotModule<PewpewPlugin> {

	@Getter
	private ShootingHandler shootingHandler;

	@Getter
	private ThrowableHandler throwableHandler;

	@Getter
	private AnimationManager animationManager;

	@Getter
	private RigAnimator rigAnimator;

	@Getter
	private BulletImpacts bulletImpacts;

	public WeaponsModule(SpigotModuleManager<PewpewPlugin> moduleManager) {
		super(moduleManager);
	}

	@Override
	public void onEnable() {
		Menu.init(getPlugin());
		CombatTagIntegration.init();
		OpenMinetopiaIntegration.init();
		animationManager = new AnimationManager(getPlugin());
		animationManager.start();
		rigAnimator = new RigAnimator(getPlugin());
		rigAnimator.start();
		shootingHandler = new ShootingHandler(getPlugin());
		throwableHandler = new ThrowableHandler(getPlugin());
		bulletImpacts = new BulletImpacts(getPlugin());
		registerComponent(new ShootingListener(shootingHandler));
		registerComponent(new ThrowingListener(throwableHandler));
		registerComponent(new AttachmentListener());
		registerComponent(new ScopeListener());
		registerComponent(new MagazineListener());
	}

	@Override
	public void onDisable() {
		if (shootingHandler != null) {
			shootingHandler.clearCooldowns();
		}
		if (throwableHandler != null) {
			throwableHandler.clearCooldowns();
		}
		if (animationManager != null) {
			animationManager.stop();
		}
		if (rigAnimator != null) {
			rigAnimator.stop();
		}
		if (bulletImpacts != null) {
			bulletImpacts.clear();
		}
	}
}
