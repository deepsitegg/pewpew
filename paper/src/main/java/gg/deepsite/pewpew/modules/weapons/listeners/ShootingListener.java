package gg.deepsite.pewpew.modules.weapons.listeners;

import gg.deepsite.pewpew.PewpewPlugin;
import gg.deepsite.pewpew.api.events.PewpewHitBlockEvent;
import gg.deepsite.pewpew.api.events.PewpewKillEvent;
import gg.deepsite.pewpew.api.objects.PewPewItem;
import gg.deepsite.pewpew.api.objects.PewpewGunItem;
import gg.deepsite.pewpew.api.objects.PewpewThrowableItem;
import gg.deepsite.pewpew.integrations.WeaponRestrictions;
import gg.deepsite.pewpew.modules.items.ItemsModule;
import gg.deepsite.pewpew.modules.weapons.WeaponsModule;
import gg.deepsite.pewpew.modules.weapons.shooting.GunHitTracker;
import gg.deepsite.pewpew.modules.weapons.shooting.ProjectileShotExecutor;
import gg.deepsite.pewpew.modules.weapons.shooting.ShootingHandler;
import gg.deepsite.pewpew.utils.ChatUtils;
import gg.deepsite.pewpew.utils.item.ItemFactory;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ChargedProjectiles;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

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
		if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

		ItemStack held = event.getItem();
		if (held == null) return;

		PewPewItem item = itemsModule().fromItemStack(held);
		if (event.getHand() != EquipmentSlot.HAND) {
			// A loaded crossbow-pose item in the off hand would fire its placeholder arrow.
			if (item != null && held.getType() == Material.CROSSBOW) event.setCancelled(true);
			return;
		}
		if (!(item instanceof PewpewGunItem gun)) return;

		event.setCancelled(true);
		if (WeaponRestrictions.denied(event.getPlayer(), gun, true)) return;
		shootingHandler.onTrigger(event.getPlayer(), gun, held);
	}

	@EventHandler
	public void onReload(PlayerSwapHandItemsEvent event) {
		ItemStack mainHand = event.getOffHandItem();
		if (mainHand == null) return;

		PewPewItem item = itemsModule().fromItemStack(mainHand);
		if (!(item instanceof PewpewGunItem gun)) return;

		event.setCancelled(true);
		if (event.getPlayer().isSneaking()) {
			shootingHandler.ejectMagazine(event.getPlayer(), gun, mainHand);
			return;
		}
		shootingHandler.startReload(event.getPlayer(), gun, mainHand);
	}

	@EventHandler
	public void onItemHeld(PlayerItemHeldEvent event) {
		shootingHandler.cancelReload(event.getPlayer().getUniqueId());
		ItemStack next = event.getPlayer().getInventory().getItem(event.getNewSlot());
		if (next != null && next.getType() == Material.CROSSBOW && itemsModule().fromItemStack(next) != null
				&& !isCharged(next)) {
			ItemFactory.charge(next); // items built before the arrow fix were never loaded
		}
	}

	private static boolean isCharged(ItemStack crossbow) {
		ChargedProjectiles charged = crossbow.getData(DataComponentTypes.CHARGED_PROJECTILES);
		return charged != null && !charged.projectiles().isEmpty();
	}

	@EventHandler
	public void onShootBow(EntityShootBowEvent event) {
		ItemStack bow = event.getBow();
		if (bow == null || itemsModule().fromItemStack(bow) == null) return;
		event.setCancelled(true);
		event.setConsumeItem(false);
		// Vanilla empties the crossbow before the event fires; load it again once it is done.
		Bukkit.getScheduler().runTask(PewpewPlugin.getInstance(), () -> ItemFactory.charge(bow));
	}

	@EventHandler
	public void onHitBlock(PewpewHitBlockEvent event) {
		WeaponsModule weapons = PewpewPlugin.getModuleManager().get(WeaponsModule.class);
		if (weapons == null) return;
		weapons.getBulletImpacts().spawn(event.getGun(), event.getLocation(), event.getBlock());
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
	public void onQuit(PlayerQuitEvent event) {
		shootingHandler.clearPlayer(event.getPlayer().getUniqueId());
	}

	@EventHandler
	public void onDeath(PlayerDeathEvent event) {
		Player victim = event.getEntity();
		GunHitTracker.Hit hit = GunHitTracker.consume(victim.getUniqueId());
		if (hit == null || !killedBy(victim, hit.killerId())) return;

		PewPewItem weapon = itemsModule().get(hit.weaponId());
		PewpewGunItem gun = weapon instanceof PewpewGunItem direct ? direct
				: hit.launcherId() != null && itemsModule().get(hit.launcherId()) instanceof PewpewGunItem launcher
				? launcher : null;

		String template;
		if (weapon instanceof PewpewThrowableItem throwable) {
			template = throwable.getDeathMessage() != null ? throwable.getDeathMessage()
					: gun != null && gun.getDeathMessage() != null ? gun.getDeathMessage()
					: PewpewPlugin.getMessagesConfig().throwableDeath();
		} else if (gun != null) {
			template = gun.getDeathMessage() != null ? gun.getDeathMessage()
					: PewpewPlugin.getMessagesConfig().gunDeath();
		} else {
			return;
		}

		OfflinePlayer killer = Bukkit.getOfflinePlayer(hit.killerId());
		String killerName = killer.getName() != null ? killer.getName() : "Unknown";
		String message = template
				.replace("%victim%", victim.getName())
				.replace("%killer%", killerName)
				.replace("%weapon%", (gun != null ? gun : weapon).getName());

		if (gun != null) {
			PewpewKillEvent killEvent = new PewpewKillEvent(victim, killer, gun, message);
			killEvent.callEvent();
			message = killEvent.getDeathMessage();
		}
		if (message != null) event.deathMessage(ChatUtils.format(message));
	}

	/**
	 * Whether the killing blow could have come from the tracked hit. Damage with a causing entity must come from
	 * the tracked killer; sourceless damage only counts when it is the kind pewpew leaves behind (burning, poison,
	 * gas). Anything else, like a fall, keeps the vanilla death message.
	 */
	private static boolean killedBy(Player victim, UUID killerId) {
		EntityDamageEvent last = victim.getLastDamageCause();
		if (last == null) return false;
		Entity causing = last.getDamageSource().getCausingEntity();
		if (causing != null) return causing.getUniqueId().equals(killerId);
		// ponytail: sourceless fire/poison is credited to the last pewpew hit in the tracker window, even if a lava pool lit them
		return switch (last.getCause()) {
			case FIRE, FIRE_TICK, POISON, MAGIC, WITHER -> true;
			default -> false;
		};
	}
}
