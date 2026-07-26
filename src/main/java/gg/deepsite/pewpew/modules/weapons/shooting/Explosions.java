package gg.deepsite.pewpew.modules.weapons.shooting;

import gg.deepsite.pewpew.PewpewPlugin;
import gg.deepsite.pewpew.api.objects.ExplosiveConfig;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class Explosions {

    private Explosions() {
    }

    public static void detonate(@NotNull World world, @NotNull Location center, @NotNull ExplosiveConfig cfg,
                                @Nullable Player source) {
        double radius = cfg.blastRadius();
        world.spawnParticle(Particle.EXPLOSION_EMITTER, center, 1);
        world.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 1.0f);

        for (LivingEntity living : world.getNearbyLivingEntities(center, radius)) {
            double factor = Math.max(0.0, 1.0 - living.getLocation().distance(center) / radius);
            if (factor <= 0.0) continue;
            double damage = cfg.explosionDamage() * factor;
            if (source != null) living.damage(damage, source);
            else living.damage(damage);
            Vector away = living.getLocation().toVector().subtract(center.toVector());
            if (away.lengthSquared() > 0) {
                living.setVelocity(living.getVelocity().add(
                        away.normalize().multiply(cfg.explosionKnockback() * factor).setY(0.4 * factor)));
            }
        }

        if (cfg.damageBlocks()) damageBlocks(world, center, cfg);
    }

    private static void damageBlocks(World world, Location center, ExplosiveConfig cfg) {
        int r = (int) Math.ceil(cfg.blastRadius());
        double r2 = cfg.blastRadius() * cfg.blastRadius();
        List<BlockState> saved = new ArrayList<>();
        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                for (int z = -r; z <= r; z++) {
                    if (x * x + y * y + z * z > r2) continue;
                    Block block = world.getBlockAt(center.getBlockX() + x, center.getBlockY() + y, center.getBlockZ() + z);
                    Material type = block.getType();
                    // ponytail: flattens everything breakable (skips air + indestructible only); no drops, no ore-specific loot
                    if (type.isAir() || type.getHardness() < 0) continue;
                    if (cfg.rebuildEnabled()) saved.add(block.getState());
                    block.setType(Material.AIR, false);
                }
            }
        }
        if (cfg.rebuildEnabled() && !saved.isEmpty()) {
            Collections.shuffle(saved);
            saved.sort(Comparator.comparingInt(BlockState::getY));
            scheduleRebuild(saved, cfg);
        }
    }

    private static void scheduleRebuild(List<BlockState> saved, ExplosiveConfig cfg) {
        int perTick = Math.max(1, cfg.blocksPerTick());
        int[] index = {0};
        PewpewPlugin.getInstance().getServer().getScheduler().runTaskTimer(PewpewPlugin.getInstance(), task -> {
            int placed = 0;
            while (index[0] < saved.size() && placed < perTick) {
                saved.get(index[0]).update(true, false);
                index[0]++;
                placed++;
            }
            if (index[0] >= saved.size()) task.cancel();
        }, Math.max(0, cfg.rebuildDelay()), 1L);
    }
}
