package gg.deepsite.pewpew.modules.weapons.throwing;

import gg.deepsite.pewpew.api.enums.ThrowableEffect;
import gg.deepsite.pewpew.api.events.PewpewThrowEvent;
import gg.deepsite.pewpew.api.events.PewpewThrowableDetonateEvent;
import gg.deepsite.pewpew.api.objects.PewpewThrowableItem;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ThrowableHandler {

	private static final long THROW_COOLDOWN_MILLIS = 500L;

	private static final int SMOKE_DURATION_TICKS = 200;
	private static final int SMOKE_BLIND_AMPLIFIER = 0;
	private static final int POISON_DURATION_TICKS = 140;
	private static final int POISON_AMPLIFIER = 1;
	private static final int FLASH_BLIND_TICKS = 100;
	private static final double FLASH_NAUSEA_RATIO = 1.4;

	private final Plugin plugin;
	private final Map<UUID, Long> nextThrowAt = new ConcurrentHashMap<>();

	public ThrowableHandler(@NotNull Plugin plugin) {
		this.plugin = plugin;
	}

	public void tryThrow(@NotNull Player player, @NotNull PewpewThrowableItem throwable, @NotNull ItemStack held) {
		UUID id = player.getUniqueId();
		long now = System.currentTimeMillis();
		if (now < nextThrowAt.getOrDefault(id, 0L)) return;

		if (!new PewpewThrowEvent(player, throwable, held).callEvent()) return;
		nextThrowAt.put(id, now + THROW_COOLDOWN_MILLIS);

		consumeOne(player, held);

		Location eye = player.getEyeLocation();
		ItemStack display = held.clone();
		display.setAmount(1);

		Item thrown = player.getWorld().dropItem(eye, display);
		thrown.setVelocity(eye.getDirection().multiply(Math.max(0.1, throwable.getThrowForce())));
		thrown.setPickupDelay(Integer.MAX_VALUE);
		thrown.setWillAge(false);
		thrown.setThrower(id);

		player.getWorld().playSound(eye, Sound.ENTITY_SNOWBALL_THROW, 0.8f, 0.8f);

		int fuse = Math.max(1, throwable.getFuseTime());
		plugin.getServer().getScheduler().runTaskLater(plugin, () -> detonate(thrown, throwable), fuse);
	}

	public void clearCooldowns() {
		nextThrowAt.clear();
	}

	private void detonate(Item thrown, PewpewThrowableItem throwable) {
		if (thrown.isDead() || !thrown.isValid()) return;
		Location loc = thrown.getLocation();
		World world = loc.getWorld();

		boolean detonate = new PewpewThrowableDetonateEvent(thrown, throwable, loc).callEvent();
		thrown.remove();
		if (!detonate || world == null) return;

		switch (throwable.getEffect()) {
			case EXPLOSION -> explosion(world, loc, throwable);
			case SMOKE -> smoke(world, loc, throwable);
			case FLASH -> flash(world, loc, throwable);
			case POISON -> poison(world, loc, throwable);
			case FIRE -> fire(world, loc, throwable);
		}
	}

	private static int or(int value, int fallback) {
		return value >= 0 ? value : fallback;
	}

	private void explosion(World world, Location loc, PewpewThrowableItem throwable) {
		double radius = throwable.getBlastRadius();
		world.spawnParticle(Particle.EXPLOSION_EMITTER, loc, 1);
		world.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 1.0f);
		for (LivingEntity living : world.getNearbyLivingEntities(loc, radius)) {
			double dist = living.getLocation().distance(loc);
			double factor = Math.max(0.0, 1.0 - dist / radius);
			if (factor <= 0.0) continue;
			living.damage(throwable.getExplosionDamage() * factor);
			Vector away = living.getLocation().toVector().subtract(loc.toVector());
			if (away.lengthSquared() > 0) {
				living.setVelocity(living.getVelocity().add(
						away.normalize().multiply(throwable.getExplosionKnockback() * factor).setY(0.4 * factor)));
			}
		}
	}

	private void smoke(World world, Location loc, PewpewThrowableItem throwable) {
		world.playSound(loc, Sound.ENTITY_TNT_PRIMED, 1.0f, 1.4f);
		AreaEffectCloud cloud = world.spawn(loc, AreaEffectCloud.class);
		cloud.setRadius((float) Math.max(1.0, throwable.getBlastRadius()));
		cloud.setDuration(or(throwable.getEffectDuration(), SMOKE_DURATION_TICKS));
		cloud.setParticle(Particle.LARGE_SMOKE);
		cloud.addCustomEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60,
				or(throwable.getEffectAmplifier(), SMOKE_BLIND_AMPLIFIER)), true);
	}

	private void flash(World world, Location loc, PewpewThrowableItem throwable) {
		int blind = or(throwable.getEffectDuration(), FLASH_BLIND_TICKS);
		int nausea = (int) (blind * FLASH_NAUSEA_RATIO);
		int amplifier = or(throwable.getEffectAmplifier(), 0);
		world.spawnParticle(Particle.FLASH, loc, 4);
		world.playSound(loc, Sound.ITEM_FIRECHARGE_USE, 1.5f, 0.6f);
		for (LivingEntity living : world.getNearbyLivingEntities(loc, throwable.getBlastRadius())) {
			if (!(living instanceof Player target)) continue;
			target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, blind, amplifier));
			target.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, nausea, amplifier));
		}
	}

	private void poison(World world, Location loc, PewpewThrowableItem throwable) {
		world.playSound(loc, Sound.ENTITY_GENERIC_SPLASH, 1.0f, 1.0f);
		AreaEffectCloud cloud = world.spawn(loc, AreaEffectCloud.class);
		cloud.setRadius((float) Math.max(1.0, throwable.getBlastRadius()));
		cloud.setDuration(or(throwable.getEffectDuration(), POISON_DURATION_TICKS));
		cloud.setParticle(Particle.ITEM_SLIME);
		cloud.addCustomEffect(new PotionEffect(PotionEffectType.POISON, 100,
				or(throwable.getEffectAmplifier(), POISON_AMPLIFIER)), true);
	}

	private void fire(World world, Location loc, PewpewThrowableItem throwable) {
		double radius = throwable.getBlastRadius();
		world.spawnParticle(Particle.FLAME, loc, 40, radius / 2, 0.5, radius / 2, 0.02);
		world.playSound(loc, Sound.ITEM_FIRECHARGE_USE, 1.2f, 1.0f);
		for (LivingEntity living : world.getNearbyLivingEntities(loc, radius)) {
			living.setFireTicks(throwable.getFireTicks());
		}
	}

	private void consumeOne(Player player, ItemStack held) {
		int amount = held.getAmount();
		if (amount <= 1) {
			player.getInventory().setItemInMainHand(null);
		} else {
			held.setAmount(amount - 1);
			player.getInventory().setItemInMainHand(held);
		}
	}
}
