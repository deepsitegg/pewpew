package gg.deepsite.pewpew.modules.weapons.throwing;

import gg.deepsite.pewpew.PewpewPlugin;
import gg.deepsite.pewpew.api.enums.SoundEvent;
import gg.deepsite.pewpew.modules.items.ItemsModule;
import gg.deepsite.pewpew.modules.weapons.shooting.GunHitTracker;
import gg.deepsite.pewpew.utils.BukkitRegistry;
import gg.deepsite.pewpew.utils.PersistentDataUtil;
import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.key.Key;
import gg.deepsite.pewpew.utils.Sounds;
import gg.deepsite.pewpew.api.events.PewpewThrowEvent;
import gg.deepsite.pewpew.api.events.PewpewThrowableDetonateEvent;
import gg.deepsite.pewpew.api.objects.PewpewThrowableItem;
import org.bukkit.Location;
import org.bukkit.Particle;
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
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class ThrowableHandler {

	private static final int SMOKE_DURATION_TICKS = 200;
	private static final int SMOKE_BLIND_AMPLIFIER = 0;
	private static final int POISON_DURATION_TICKS = 140;
	private static final int POISON_AMPLIFIER = 1;
	private static final int FLASH_BLIND_TICKS = 100;
	private static final double FLASH_NAUSEA_RATIO = 1.4;
	private static final double GAS_RADIUS = 5.0;
	private static final int GAS_DURATION_TICKS = 200;
	private static final int GAS_PULSE_TICKS = 10;
	private static final List<PotionEffect> GAS_EFFECTS = List.of(
			new PotionEffect(PotionEffectType.NAUSEA, 100, 0),
			new PotionEffect(PotionEffectType.BLINDNESS, 60, 0),
			new PotionEffect(PotionEffectType.SLOWNESS, 60, 1));

	private final Plugin plugin;
	private final Map<String, Long> nextThrowAt = new ConcurrentHashMap<>();

	public ThrowableHandler(@NotNull Plugin plugin) {
		this.plugin = plugin;
	}

	public void tryThrow(@NotNull Player player, @NotNull PewpewThrowableItem throwable, @NotNull ItemStack held) {
		UUID id = player.getUniqueId();
		long now = System.currentTimeMillis();
		String key = id + ":" + throwable.getId();
		if (now < nextThrowAt.getOrDefault(key, 0L)) return;

		if (!new PewpewThrowEvent(player, throwable, held).callEvent()) return;
		nextThrowAt.put(key, now + throwable.getCooldown() * 50L);
		if (throwable.getCooldown() > 0) player.setCooldown(held, throwable.getCooldown());

		consumeOne(player, held);

		Location eye = player.getEyeLocation();
		ItemStack display = held.clone();
		display.setAmount(1);

		Item thrown = player.getWorld().dropItem(eye, display);
		thrown.setVelocity(eye.getDirection().multiply(Math.max(0.1, throwable.getThrowForce())));
		thrown.setPickupDelay(Integer.MAX_VALUE);
		thrown.setWillAge(false);
		thrown.setThrower(id);

		Sounds.at(player.getWorld(), eye, SoundEvent.THROWABLE_THROW);

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

		applyEffect(world, loc, throwable, thrown.getThrower(), null);
	}

	public void applyEffect(@NotNull World world, @NotNull Location loc, @NotNull PewpewThrowableItem throwable) {
		applyEffect(world, loc, throwable, null, null);
	}

	/**
	 * @param thrower    credited for kills in death messages, if known
	 * @param launcherId the gun that fired this throwable as its round, if any
	 */
	public void applyEffect(@NotNull World world, @NotNull Location loc, @NotNull PewpewThrowableItem throwable,
	                        @Nullable UUID thrower, @Nullable String launcherId) {
		Consumer<LivingEntity> credit = thrower == null ? victim -> {
		} : victim -> GunHitTracker.record(victim, thrower, throwable.getId(), launcherId);
		switch (throwable.getEffect()) {
			case EXPLOSION -> explosion(world, loc, throwable, thrower, credit);
			case SMOKE -> smoke(world, loc, throwable);
			case FLASH -> flash(world, loc, throwable);
			// ponytail: poison clouds are not credited, vanilla poison never kills
			case POISON -> poison(world, loc, throwable);
			case FIRE -> fire(world, loc, throwable, credit);
			case TEAR_GAS -> tearGas(world, loc, throwable, credit);
		}
	}

	private static int or(int value, int fallback) {
		return value >= 0 ? value : fallback;
	}

	private void explosion(World world, Location loc, PewpewThrowableItem throwable, @Nullable UUID thrower,
	                       Consumer<LivingEntity> credit) {
		double radius = throwable.getBlastRadius();
		Player source = thrower == null ? null : plugin.getServer().getPlayer(thrower);
		world.spawnParticle(Particle.EXPLOSION_EMITTER, loc, 1);
		Sounds.at(world, loc, SoundEvent.THROWABLE_EXPLODE);
		for (LivingEntity living : world.getNearbyLivingEntities(loc, radius)) {
			double dist = living.getLocation().distance(loc);
			double factor = Math.max(0.0, 1.0 - dist / radius);
			if (factor <= 0.0) continue;
			credit.accept(living);
			if (source != null) living.damage(throwable.getExplosionDamage() * factor, source);
			else living.damage(throwable.getExplosionDamage() * factor);
			Vector away = living.getLocation().toVector().subtract(loc.toVector());
			if (away.lengthSquared() > 0) {
				living.setVelocity(living.getVelocity().add(
						away.normalize().multiply(throwable.getExplosionKnockback() * factor).setY(0.4 * factor)));
			}
		}
	}

	private void smoke(World world, Location loc, PewpewThrowableItem throwable) {
		Sounds.at(world, loc, SoundEvent.THROWABLE_SMOKE);
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
		Sounds.at(world, loc, SoundEvent.THROWABLE_FLASH);
		for (LivingEntity living : world.getNearbyLivingEntities(loc, throwable.getBlastRadius())) {
			if (!(living instanceof Player target)) continue;
			target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, blind, amplifier));
			target.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, nausea, amplifier));
		}
	}

	private void poison(World world, Location loc, PewpewThrowableItem throwable) {
		Sounds.at(world, loc, SoundEvent.THROWABLE_POISON);
		AreaEffectCloud cloud = world.spawn(loc, AreaEffectCloud.class);
		cloud.setRadius((float) Math.max(1.0, throwable.getBlastRadius()));
		cloud.setDuration(or(throwable.getEffectDuration(), POISON_DURATION_TICKS));
		cloud.setParticle(Particle.ITEM_SLIME);
		cloud.addCustomEffect(new PotionEffect(PotionEffectType.POISON, 100,
				or(throwable.getEffectAmplifier(), POISON_AMPLIFIER)), true);
	}

	private void fire(World world, Location loc, PewpewThrowableItem throwable, Consumer<LivingEntity> credit) {
		double radius = throwable.getBlastRadius();
		world.spawnParticle(Particle.FLAME, loc, 40, radius / 2, 0.5, radius / 2, 0.02);
		Sounds.at(world, loc, SoundEvent.THROWABLE_FIRE);
		for (LivingEntity living : world.getNearbyLivingEntities(loc, radius)) {
			credit.accept(living);
			living.setFireTicks(throwable.getFireTicks());
		}
	}

	/** A lingering cloud that re-applies its effects every pulse to players inside it who wear no gas mask. */
	private void tearGas(World world, Location loc, PewpewThrowableItem throwable, Consumer<LivingEntity> credit) {
		double radius = throwable.getBlastRadius() > 0 ? throwable.getBlastRadius() : GAS_RADIUS;
		int duration = throwable.getEffectDuration() > 0 ? throwable.getEffectDuration() : GAS_DURATION_TICKS;
		List<PotionEffect> configured = BukkitRegistry.effects(throwable.getGasEffects());
		List<PotionEffect> effects = configured.isEmpty() ? GAS_EFFECTS : configured;
		Sounds.at(world, loc, SoundEvent.THROWABLE_TEAR_GAS);

		int[] elapsed = {0};
		plugin.getServer().getScheduler().runTaskTimer(plugin, task -> {
			if (elapsed[0] >= duration || !world.isChunkLoaded(loc.getBlockX() >> 4, loc.getBlockZ() >> 4)) {
				task.cancel();
				return;
			}
			elapsed[0] += GAS_PULSE_TICKS;
			world.spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, loc, (int) Math.ceil(radius * 3),
					radius / 2, 0.5, radius / 2, 0.01);
			List<String> masks = gasMasks();
			for (Player player : world.getNearbyPlayers(loc, radius)) {
				if (wearsGasMask(player, masks)) {
					effects.forEach(effect -> player.removePotionEffect(effect.getType()));
					continue;
				}
				credit.accept(player);
				effects.forEach(player::addPotionEffect);
			}
		}, 0L, GAS_PULSE_TICKS);
	}

	private static List<String> gasMasks() {
		try {
			return PewpewPlugin.getDefaultConfiguration().getRootNode().node("tear-gas", "gas-masks")
					.getList(String.class, List.of("vibe:gas_mask"));
		} catch (SerializationException e) {
			return List.of("vibe:gas_mask");
		}
	}

	/** A gas mask is a helmet whose item model, or pewpew item id, is listed in tear-gas.gas-masks. */
	private static boolean wearsGasMask(Player player, List<String> masks) {
		ItemStack helmet = player.getInventory().getHelmet();
		if (helmet == null || helmet.isEmpty()) return false;
		Key model = helmet.getData(DataComponentTypes.ITEM_MODEL);
		if (model != null && masks.contains(model.asString())) return true;
		String id = PersistentDataUtil.getPewpew(helmet, ItemsModule.PDC_KEY);
		return id != null && (masks.contains(id) || masks.contains("pewpew:" + id));
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
