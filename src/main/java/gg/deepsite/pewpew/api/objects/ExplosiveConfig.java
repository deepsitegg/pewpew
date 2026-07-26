package gg.deepsite.pewpew.api.objects;

public record ExplosiveConfig(
        double blastRadius,
        double explosionDamage,
        double explosionKnockback,
        boolean damageBlocks,
        boolean rebuildEnabled,
        int rebuildDelay,
        int blocksPerTick) {
}
