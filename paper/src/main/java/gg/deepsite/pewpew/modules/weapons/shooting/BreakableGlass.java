package gg.deepsite.pewpew.modules.weapons.shooting;

import gg.deepsite.pewpew.PewpewPlugin;
import gg.deepsite.pewpew.api.enums.SoundEvent;
import gg.deepsite.pewpew.shooting.Glass;
import gg.deepsite.pewpew.utils.Sounds;
import lombok.experimental.UtilityClass;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Glass shot out by hitscan bullets. Broken blocks live in memory only and are put back after a delay and on
 * disable, so the saved world keeps its glass.
 */
@UtilityClass
public class BreakableGlass {

	private static final Map<Location, BlockData> BROKEN = new HashMap<>();

	/** Breaks the block if it is glass and breaking is enabled. Returns whether the bullet should fly on. */
	public static boolean shatter(@Nullable Block block) {
		if (block == null || !Glass.isGlass(block.getType().getKey().asString())) return false;
		if (!PewpewPlugin.getDefaultConfiguration().getRootNode().node("glass", "breakable").getBoolean(true)) return false;

		Location at = block.getLocation();
		BlockData original = block.getBlockData();
		BROKEN.putIfAbsent(at, original);
		block.setType(Material.AIR, false);
		Sounds.at(block.getWorld(), at.clone().add(0.5, 0.5, 0.5), SoundEvent.HIT_GLASS_BREAK);
		block.getWorld().spawnParticle(Particle.BLOCK, at.clone().add(0.5, 0.5, 0.5), 12, 0.3, 0.3, 0.3, original);

		long seconds = PewpewPlugin.getDefaultConfiguration().getRootNode().node("glass", "restore-after-seconds").getLong(300);
		if (seconds > 0) {
			PewpewPlugin.getInstance().getServer().getScheduler()
					.runTaskLater(PewpewPlugin.getInstance(), () -> restore(at), seconds * 20);
		}
		return true;
	}

	public static void restoreAll() {
		for (Location at : BROKEN.keySet().toArray(Location[]::new)) restore(at);
	}

	private static void restore(Location at) {
		BlockData original = BROKEN.remove(at);
		if (original == null || at.getWorld() == null) return;
		Block block = at.getBlock();
		if (block.getType().isAir()) block.setBlockData(original, false);
	}
}
