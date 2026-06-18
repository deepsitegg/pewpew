package gg.deepsite.pewpew.modules.weapons.shooting;

import gg.deepsite.pewpew.api.objects.PewpewGunItem;
import gg.deepsite.pewpew.utils.ChatUtils;
import lombok.experimental.UtilityClass;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
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

    private static final double HEADSHOT_BAND = 0.3;

    @NotNull
    public static Vector applySpread(@NotNull Vector direction, double spreadDegrees) {
        if (spreadDegrees <= 0) return direction;
        ThreadLocalRandom random = ThreadLocalRandom.current();
        double yaw = Math.toRadians((random.nextDouble() * 2 - 1) * spreadDegrees);
        double pitch = Math.toRadians((random.nextDouble() * 2 - 1) * spreadDegrees);
        return direction.clone().rotateAroundY(yaw).rotateAroundX(pitch).normalize();
    }

    public static void dealProjectileDamage(@NotNull LivingEntity target, double amount,
                                            @NotNull Entity causing, @NotNull Entity direct) {
        DamageSource source = DamageSource.builder(DamageType.ARROW)
                .withCausingEntity(causing)
                .withDirectEntity(direct)
                .build();
        target.setNoDamageTicks(0);
        target.damage(amount, source);
    }

    public static boolean isHeadshot(@NotNull LivingEntity target, double hitY) {
        return hitY >= target.getEyeLocation().getY() - HEADSHOT_BAND;
    }

    public static void hitFeedback(@NotNull Player shooter, @NotNull PewpewGunItem gun,
                                   @NotNull LivingEntity victim, double damage, boolean headshot) {
        if (gun.getHitSound() != null) {
            gun.getHitSound().play(shooter);
        } else {
            shooter.playSound(shooter, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, headshot ? 1.8f : 1.0f);
        }
        if (gun.getHitMessage() != null) {
            shooter.sendActionBar(ChatUtils.format(gun.getHitMessage()
                    .replace("%victim%", victim.getName())
                    .replace("%damage%", String.format("%.1f", damage))));
        }
    }

    public static double falloffMultiplier(double distance, double start, double end, double minMultiplier) {
        if (end <= start || distance <= start) return 1.0;
        if (distance >= end) return minMultiplier;
        double t = (distance - start) / (end - start);
        return 1.0 + t * (minMultiplier - 1.0);
    }

    public static void impact(@Nullable Particle particle, @NotNull Location at) {
        if (particle == null || at.getWorld() == null) return;
        at.getWorld().spawnParticle(particle, at, 8, 0.1, 0.1, 0.1, 0.02);
    }

    public static void applyEffects(@NotNull LivingEntity entity, @Nullable List<PotionEffect> effects) {
        if (effects == null || effects.isEmpty()) return;
        for (PotionEffect effect : effects) entity.addPotionEffect(effect);
    }

    public static void disableShield(@NotNull LivingEntity target, int ticks) {
        if (ticks <= 0 || !(target instanceof Player player) || !player.isBlocking()) return;
        player.setCooldown(Material.SHIELD, ticks);
        player.getWorld().playSound(player.getLocation(), Sound.ITEM_SHIELD_BREAK, 1.0f, 1.0f);
    }

    public static boolean rollCrit(double chance) {
        return chance > 0 && ThreadLocalRandom.current().nextDouble() < chance;
    }

    public static void critEffect(@NotNull Player shooter, @NotNull LivingEntity target) {
        target.getWorld().spawnParticle(org.bukkit.Particle.CRIT,
                target.getLocation().add(0, target.getHeight() * 0.6, 0), 14, 0.3, 0.4, 0.3, 0.15);
        shooter.playSound(shooter, Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.0f, 1.2f);
    }

}
