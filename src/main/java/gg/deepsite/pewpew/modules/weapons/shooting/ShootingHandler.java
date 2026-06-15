package gg.deepsite.pewpew.modules.weapons.shooting;

import gg.deepsite.pewpew.PewpewPlugin;
import gg.deepsite.pewpew.api.events.PewpewReloadEvent;
import gg.deepsite.pewpew.api.events.PewpewShootEvent;
import gg.deepsite.pewpew.api.enums.FiringMode;
import gg.deepsite.pewpew.api.enums.ReloadType;
import gg.deepsite.pewpew.api.objects.PewPewItem;
import gg.deepsite.pewpew.api.objects.PewpewAmmoItem;
import gg.deepsite.pewpew.api.objects.PewpewGunItem;
import gg.deepsite.pewpew.modules.items.ItemsModule;
import gg.deepsite.pewpew.modules.weapons.ammo.AmmoUtil;
import gg.deepsite.pewpew.modules.weapons.attachment.AttachmentUtil;
import gg.deepsite.pewpew.modules.weapons.lore.GunLoreRenderer;
import gg.deepsite.pewpew.utils.ChatUtils;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ShootingHandler {

	private static final long BURST_SHOT_DELAY_TICKS = 2L;
	private static final long SEMI_RELEASE_MS = 100L;
	private static final long AUTO_RELEASE_MS = 150L;

	private final Plugin plugin;
	private final Set<UUID> reloading = ConcurrentHashMap.newKeySet();
	private final Map<UUID, BukkitTask> reloadTasks = new ConcurrentHashMap<>();
	private final Map<UUID, BukkitTask> autoTasks = new ConcurrentHashMap<>();
	private final Map<UUID, Long> lastTrigger = new ConcurrentHashMap<>();
	private final Map<FiringMode, ShotExecutor> executors = new EnumMap<>(FiringMode.class);

	@Getter
	private final ProjectileShotExecutor projectileExecutor = new ProjectileShotExecutor();

	public ShootingHandler(@NotNull Plugin plugin) {
		this.plugin = plugin;
		executors.put(FiringMode.HITSCAN, new HitscanShotExecutor());
		executors.put(FiringMode.PROJECTILE, projectileExecutor);
	}

	public void onTrigger(@NotNull Player player, @NotNull PewpewGunItem gun, @NotNull ItemStack weapon) {
		UUID id = player.getUniqueId();
		long now = System.currentTimeMillis();
		long last = lastTrigger.getOrDefault(id, 0L);
		lastTrigger.put(id, now);

		if (gun.isAutomatic()) {
			if (!autoTasks.containsKey(id)) startAutoFire(player, gun);
		} else if (now - last > SEMI_RELEASE_MS) {
			tryShoot(player, gun, weapon);
		}
	}

	private void startAutoFire(Player player, PewpewGunItem gun) {
		UUID id = player.getUniqueId();
		BukkitTask task = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
			if (!player.isOnline() || player.isDead()) { stopAutoFire(id); return; }

			ItemStack held = player.getInventory().getItemInMainHand();
			if (!isSameGun(held, gun)) { stopAutoFire(id); return; }

			if (System.currentTimeMillis() - lastTrigger.getOrDefault(id, 0L) > AUTO_RELEASE_MS) {
				stopAutoFire(id);
				return;
			}

			if (AmmoUtil.usesAmmo(gun) && AmmoUtil.get(held) <= 0) {
				signalEmpty(player);
				stopAutoFire(id);
				return;
			}

			tryShoot(player, gun, held);
		}, 0L, 1L);
		autoTasks.put(id, task);
	}

	private void stopAutoFire(UUID id) {
		BukkitTask task = autoTasks.remove(id);
		if (task != null) task.cancel();
	}

	public void tryShoot(@NotNull Player player, @NotNull PewpewGunItem gun, @NotNull ItemStack weapon) {
		AmmoUtil.init(weapon, gun);
		boolean empty = AmmoUtil.usesAmmo(gun) && AmmoUtil.get(weapon) <= 0;

		if (isReloading(player)) {
			if (empty) return;
			endReload(player.getUniqueId());
		}

		if (empty) {
			signalEmpty(player);
			return;
		}

		if (player.hasCooldown(weapon)) return;

		int burstCount = Math.max(1, gun.getBurstCount());
		long burstSpan = (burstCount - 1) * BURST_SHOT_DELAY_TICKS;
		int actionTicks = gun.getActionOpenTime() + gun.getActionCloseTime();
		int cooldownTicks = (int) burstSpan + Math.max(1, gun.getFireRate()) + actionTicks;
		player.setCooldown(weapon, cooldownTicks);

		fireShot(player, gun);
		for (int shot = 1; shot < burstCount; shot++) {
			plugin.getServer().getScheduler().runTaskLater(plugin,
					() -> { if (player.isOnline() && !player.isDead()) fireShot(player, gun); },
					shot * BURST_SHOT_DELAY_TICKS);
		}

		if (actionTicks > 0) cycleFirearmAction(player, gun, burstSpan);
	}

	private void cycleFirearmAction(Player player, PewpewGunItem gun, long burstSpan) {
		long openAt = burstSpan + 1;
		plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
			if (player.isOnline()) player.getWorld().playSound(player.getLocation(), Sound.BLOCK_PISTON_CONTRACT, 0.7f, 0.8f);
		}, openAt);
		plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
			if (player.isOnline()) player.getWorld().playSound(player.getLocation(), Sound.BLOCK_PISTON_EXTEND, 0.7f, 0.8f);
		}, openAt + gun.getActionOpenTime());
	}

	public void startReload(@NotNull Player player, @NotNull PewpewGunItem gun, @NotNull ItemStack weapon) {
		if (!AmmoUtil.usesAmmo(gun)) return;
		UUID id = player.getUniqueId();
		if (reloading.contains(id)) return;

		if (AmmoUtil.get(weapon) >= AttachmentUtil.effectiveMaxAmmo(gun, weapon)) {
			player.sendActionBar(ChatUtils.format("<warning>Magazine already full."));
			return;
		}

		if (gun.isConsumesAmmo() && AmmoUtil.countInInventory(player.getInventory(), gun.getAmmoType()) <= 0) {
			player.sendActionBar(noAmmoMessage(gun));
			return;
		}

		if (!new PewpewReloadEvent(player, gun, weapon).callEvent()) return;

		int reloadTicks = AttachmentUtil.effectiveReloadTime(gun, weapon);
		reloading.add(id);
		player.sendActionBar(ChatUtils.format("<color>● <gray>Reloading...", ChatUtils.PRIMARY));

		if (gun.getReloadType() == ReloadType.SINGLE) {
			player.getWorld().playSound(player.getLocation(), Sound.BLOCK_PISTON_CONTRACT, 0.8f, 1.0f);
			BukkitTask task = plugin.getServer().getScheduler().runTaskTimer(plugin,
					() -> loadSingleRound(player, gun), reloadTicks, reloadTicks);
			reloadTasks.put(id, task);
		} else {
			player.getWorld().playSound(player.getLocation(), Sound.BLOCK_PISTON_CONTRACT, 0.8f, 1.2f);
			BukkitTask task = plugin.getServer().getScheduler().runTaskLater(plugin,
					() -> finishMagazineReload(player, gun), reloadTicks);
			reloadTasks.put(id, task);
		}
	}

	public boolean isReloading(@NotNull Player player) {
		return reloading.contains(player.getUniqueId());
	}

	public void clearCooldowns() {
		reloadTasks.values().forEach(BukkitTask::cancel);
		reloadTasks.clear();
		reloading.clear();
		autoTasks.values().forEach(BukkitTask::cancel);
		autoTasks.clear();
		lastTrigger.clear();
	}

	private void loadSingleRound(Player player, PewpewGunItem gun) {
		UUID id = player.getUniqueId();
		if (!player.isOnline()) { endReload(id); return; }

		ItemStack held = player.getInventory().getItemInMainHand();
		if (!isSameGun(held, gun)) { endReload(id); return; }

		int maxAmmo = AttachmentUtil.effectiveMaxAmmo(gun, held);
		int current = AmmoUtil.get(held);
		if (current >= maxAmmo) { endReload(id); return; }

		int newAmmo;
		if (gun.isConsumesAmmo()) {
			newAmmo = AmmoUtil.loadOneItem(player.getInventory(), gun.getAmmoType(), current, maxAmmo);
			if (newAmmo <= current) {
				player.sendActionBar(noAmmoMessage(gun));
				endReload(id);
				return;
			}
		} else {
			newAmmo = current + 1;
		}
		AmmoUtil.set(held, newAmmo);
		GunLoreRenderer.apply(held, gun);
		player.getInventory().setItemInMainHand(held);
		player.getWorld().playSound(player.getLocation(), Sound.BLOCK_PISTON_EXTEND, 0.7f, 1.4f);

		if (newAmmo >= maxAmmo) endReload(id);
	}

	private void finishMagazineReload(Player player, PewpewGunItem gun) {
		UUID id = player.getUniqueId();
		endReload(id);
		if (!player.isOnline()) return;

		ItemStack held = player.getInventory().getItemInMainHand();
		if (!isSameGun(held, gun)) return;

		int maxAmmo = AttachmentUtil.effectiveMaxAmmo(gun, held);
		int current = AmmoUtil.get(held);
		int newAmmo;
		if (gun.isConsumesAmmo()) {
			newAmmo = AmmoUtil.loadMagazine(player.getInventory(), gun.getAmmoType(), current, maxAmmo);
			if (newAmmo <= current) {
				player.sendActionBar(noAmmoMessage(gun));
				return;
			}
		} else {
			newAmmo = maxAmmo;
		}

		AmmoUtil.set(held, newAmmo);
		GunLoreRenderer.apply(held, gun);
		player.getInventory().setItemInMainHand(held);
		player.getWorld().playSound(player.getLocation(), Sound.BLOCK_PISTON_EXTEND, 0.8f, 1.4f);
	}

	private void endReload(UUID id) {
		reloading.remove(id);
		BukkitTask task = reloadTasks.remove(id);
		if (task != null) task.cancel();
	}

	private void fireShot(Player player, PewpewGunItem gun) {
		ItemStack held = player.getInventory().getItemInMainHand();
		if (!isSameGun(held, gun)) return;

		if (!new PewpewShootEvent(player, gun, held).callEvent()) return;

		if (AmmoUtil.usesAmmo(gun)) {
			int ammo = AmmoUtil.get(held);
			if (ammo <= 0) {
				signalEmpty(player);
				return;
			}
			AmmoUtil.set(held, ammo - 1);
			GunLoreRenderer.apply(held, gun);
			player.getInventory().setItemInMainHand(held);
		}

		if (gun.getFireSound() != null) {
			gun.getFireSound().playAt(player.getLocation());
		} else {
			player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.6f, 1.6f);
		}

		ShotExecutor executor = executors.getOrDefault(gun.getFiringMode(), executors.get(FiringMode.HITSCAN));
		executor.execute(player, gun, held);
	}

	private void signalEmpty(Player player) {
		player.getWorld().playSound(player.getLocation(), Sound.BLOCK_DISPENSER_FAIL, 0.8f, 1.2f);
		player.sendActionBar(ChatUtils.format("<error>Out of ammo <dark_gray>┃ <gray>press <color><key:key.swapOffhand><gray> to reload", ChatUtils.PRIMARY));
	}

	private Component noAmmoMessage(PewpewGunItem gun) {
		return ChatUtils.format("<error>Out of <reset>%1<error> in your inventory.",
				ChatUtils.PRIMARY, ammoDisplayName(gun));
	}

	private String ammoDisplayName(PewpewGunItem gun) {
		for (PewPewItem item : PewpewPlugin.getModuleManager().get(ItemsModule.class).getAll()) {
			if (item instanceof PewpewAmmoItem ammo && gun.getAmmoType().equals(ammo.getAmmoType())) {
				return ammo.getName();
			}
		}
		return gun.getAmmoType();
	}

	private boolean isSameGun(ItemStack held, PewpewGunItem gun) {
		if (held == null || held.getType().isAir()) return false;
		PewPewItem item = PewpewPlugin.getModuleManager().get(ItemsModule.class).fromItemStack(held);
		return item instanceof PewpewGunItem heldGun && heldGun.getId().equals(gun.getId());
	}
}
