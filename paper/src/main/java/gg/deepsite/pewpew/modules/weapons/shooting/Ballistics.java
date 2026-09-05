package gg.deepsite.pewpew.modules.weapons.shooting;

import gg.deepsite.pewpew.api.enums.SoundEvent;
import gg.deepsite.pewpew.utils.Sounds;
import gg.deepsite.pewpew.PewpewPlugin;
import gg.deepsite.pewpew.api.objects.PewpewEffect;
import gg.deepsite.pewpew.api.objects.PewpewGunItem;
import gg.deepsite.pewpew.shooting.DamageMath;
import gg.deepsite.pewpew.shooting.SpreadMath;
import gg.deepsite.pewpew.shooting.Vec3;
import gg.deepsite.pewpew.utils.BukkitRegistry;
import gg.deepsite.pewpew.utils.ChatUtils;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.key.Key;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@UtilityClass
public class Ballistics {

	@NotNull
	public static Vector applySpread(@NotNull Vector direction, double spreadDegrees) {
		return bukkit(SpreadMath.applySpread(vec(direction), spreadDegrees, legacySpread()));
	}

	@NotNull
	public static Vec3 vec(@NotNull Vector vector) {
		return new Vec3(vector.getX(), vector.getY(), vector.getZ());
	}

	@NotNull
	public static Vector bukkit(@NotNull Vec3 vec) {
		return new Vector(vec.x(), vec.y(), vec.z());
	}

	private static boolean legacySpread() {
		return PewpewPlugin.getDefaultConfiguration() != null
				&& PewpewPlugin.getDefaultConfiguration().isLegacySpread();
	}

	@NotNull
	public static DamageType resolveDamageType(@Nullable Key configured) {
		DamageType resolved = BukkitRegistry.damageType(configured);
		return resolved != null ? resolved : DamageType.ARROW;
	}

	public static void dealProjectileDamage(@NotNull LivingEntity target, double amount,
	                                        @NotNull Entity causing, @NotNull Entity direct,
	                                        @Nullable Key damageType) {
		DamageSource source = DamageSource.builder(resolveDamageType(damageType))
				.withCausingEntity(causing)
				.withDirectEntity(direct)
				.build();
		target.setNoDamageTicks(0);
		target.damage(amount, source);
	}

	public static void applyKnockback(@NotNull LivingEntity target, @NotNull Player shooter, double knockback) {
		if (knockback <= 0) return;
		Vector away = target.getLocation().toVector().subtract(shooter.getLocation().toVector());
		if (away.lengthSquared() > 0) {
			target.setVelocity(target.getVelocity().add(
					away.normalize().multiply(knockback).setY(0.35 * knockback)));
		}
	}

	public static void applySelfKnockback(@NotNull Player shooter, double selfKnockback) {
		if (selfKnockback <= 0) return;
		Vector back = shooter.getLocation().getDirection().multiply(-selfKnockback);
		shooter.setVelocity(shooter.getVelocity().add(back));
	}

	public static boolean isHeadshot(@NotNull LivingEntity target, double hitY) {
		return DamageMath.isHeadshot(hitY, target.getEyeLocation().getY());
	}

	public static void hitFeedback(@NotNull Player shooter, @NotNull PewpewGunItem gun,
	                               @NotNull LivingEntity victim, double damage, boolean headshot) {
		if (gun.getHitSound() != null && !gun.getHitSound().isEmpty()) {
			gun.getHitSound().forEach(sound -> shooter.playSound(sound.adventure()));
		} else {
			Sounds.to(shooter, headshot ? SoundEvent.HIT_MARKER_HEADSHOT : SoundEvent.HIT_MARKER);
		}
		if (gun.getHitMessage() != null) {
			shooter.sendActionBar(ChatUtils.format(gun.getHitMessage()
					.replace("%victim%", victim.getName())
					.replace("%damage%", String.format("%.1f", damage))));
		}
	}

	public static double falloffMultiplier(double distance, double start, double end, double minMultiplier) {
		return DamageMath.falloffMultiplier(distance, start, end, minMultiplier);
	}

	public static void impact(@Nullable Key particle, @NotNull Location at) {
		Particle resolved = BukkitRegistry.particle(particle);
		if (resolved == null || at.getWorld() == null) return;
		at.getWorld().spawnParticle(resolved, at, 8, 0.1, 0.1, 0.1, 0.02);
	}

	public static void applyEffects(@NotNull LivingEntity entity, @Nullable List<PewpewEffect> effects) {
		for (PotionEffect effect : BukkitRegistry.effects(effects)) entity.addPotionEffect(effect);
	}

	public static void disableShield(@NotNull LivingEntity target, int ticks) {
		if (ticks <= 0 || !(target instanceof Player player) || !player.isBlocking()) return;
		player.setCooldown(Material.SHIELD, ticks);
		Sounds.at(player, SoundEvent.HIT_SHIELD_BREAK);
	}

	public static boolean rollCrit(double chance) {
		return DamageMath.rollCrit(chance);
	}

	public static void critEffect(@NotNull Player shooter, @NotNull LivingEntity target) {
		target.getWorld().spawnParticle(org.bukkit.Particle.CRIT,
				target.getLocation().add(0, target.getHeight() * 0.6, 0), 14, 0.3, 0.4, 0.3, 0.15);
		Sounds.to(shooter, SoundEvent.HIT_CRIT);
	}

}
