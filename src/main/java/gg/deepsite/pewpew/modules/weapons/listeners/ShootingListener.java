package gg.deepsite.pewpew.modules.weapons.listeners;

import gg.deepsite.pewpew.PewpewPlugin;
import gg.deepsite.pewpew.api.objects.PewPewItem;
import gg.deepsite.pewpew.api.objects.PewpewGunItem;
import gg.deepsite.pewpew.integrations.WorldGuardIntegration;
import gg.deepsite.pewpew.modules.items.ItemsModule;
import gg.deepsite.pewpew.modules.weapons.shooting.GunHitTracker;
import gg.deepsite.pewpew.modules.weapons.shooting.ProjectileShotExecutor;
import gg.deepsite.pewpew.modules.weapons.shooting.ShootingHandler;
import gg.deepsite.pewpew.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class ShootingListener implements Listener {

	private final ShootingHandler shootingHandler;

	public ShootingListener(ShootingHandler shootingHandler) {
		this.shootingHandler = shootingHandler;
	}

	private static ItemsModule itemsModule() {
		return PewpewPlugin.getModuleManager().get(ItemsModule.class);
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent event) {
		if (event.getHand() != EquipmentSlot.HAND) return;
		if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

		ItemStack held = event.getItem();
		if (held == null) return;

		PewPewItem item = itemsModule().fromItemStack(held);
		if (!(item instanceof PewpewGunItem gun)) return;

		event.setCancelled(true);
		if (!WorldGuardIntegration.allows(event.getPlayer())) {
			event.getPlayer().sendActionBar(ChatUtils.format(PewpewPlugin.getMessagesConfig().worldGuardDeny()));
			return;
		}
		shootingHandler.onTrigger(event.getPlayer(), gun, held);
	}

	@EventHandler
	public void onReload(PlayerSwapHandItemsEvent event) {
		ItemStack mainHand = event.getOffHandItem();
		if (mainHand == null) return;

		PewPewItem item = itemsModule().fromItemStack(mainHand);
		if (!(item instanceof PewpewGunItem gun)) return;

		event.setCancelled(true);
		shootingHandler.startReload(event.getPlayer(), gun, mainHand);
	}

	@EventHandler
	public void onProjectileHit(ProjectileHitEvent event) {
		if (!(event.getEntity() instanceof Snowball projectile)) return;

		String weaponId = ProjectileShotExecutor.getWeaponId(projectile);
		if (weaponId == null) return;

		if (!(itemsModule().get(weaponId) instanceof PewpewGunItem gun)) return;

		LivingEntity target = event.getHitEntity() instanceof LivingEntity living ? living : null;
		shootingHandler.getProjectileExecutor().handleHit(projectile, gun, target);
	}

	@EventHandler
	public void onDeath(PlayerDeathEvent event) {
		Player victim = event.getEntity();
		GunHitTracker.Hit hit = GunHitTracker.consume(victim.getUniqueId());
		if (hit == null) return;
		if (!(itemsModule().get(hit.gunId()) instanceof PewpewGunItem gun) || gun.getDeathMessage() == null) return;

		OfflinePlayer killer = Bukkit.getOfflinePlayer(hit.killerId());
		String killerName = killer.getName() != null ? killer.getName() : "Unknown";
		String message = gun.getDeathMessage()
				.replace("%victim%", victim.getName())
				.replace("%killer%", killerName)
				.replace("%weapon%", gun.getName());
		event.deathMessage(ChatUtils.format(message));
	}
}
