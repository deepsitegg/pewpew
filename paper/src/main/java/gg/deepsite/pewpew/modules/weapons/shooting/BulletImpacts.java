package gg.deepsite.pewpew.modules.weapons.shooting;

import gg.deepsite.pewpew.PewpewPlugin;
import gg.deepsite.pewpew.api.objects.PewpewGunItem;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Display;
import org.bukkit.entity.TextDisplay;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public class BulletImpacts {

	private static final int LIFETIME_TICKS = 600;
	private static final float SIZE = 0.55f;
	private static final float WIDTH_SCALE = 1.375f;
	private static final double SURFACE_OFFSET = 0.01;
	private static final float VIEW_RANGE = 1.0f;
	private static final Color COLOR = Color.fromARGB(170, 32, 32, 32);

	private final Plugin plugin;
	private final Map<String, Deque<TextDisplay>> impacts = new HashMap<>();

	public BulletImpacts(@NotNull Plugin plugin) {
		this.plugin = plugin;
	}

	@NotNull
	public static double[] faceNormal(double hitX, double hitY, double hitZ, int blockX, int blockY, int blockZ) {
		double dx = hitX - (blockX + 0.5);
		double dy = hitY - (blockY + 0.5);
		double dz = hitZ - (blockZ + 0.5);
		double ax = Math.abs(dx), ay = Math.abs(dy), az = Math.abs(dz);
		if (ax >= ay && ax >= az) return new double[]{dx < 0 ? -1 : 1, 0, 0};
		if (ay >= az) return new double[]{0, dy < 0 ? -1 : 1, 0};
		return new double[]{0, 0, dz < 0 ? -1 : 1};
	}

	public void spawn(@NotNull PewpewGunItem gun, @NotNull Location at, @Nullable Block block) {
		if (block == null || at.getWorld() == null) return;
		if (!PewpewPlugin.getDefaultConfiguration().isImpactsEnabled()) return;

		double[] normal = faceNormal(at.getX(), at.getY(), at.getZ(), block.getX(), block.getY(), block.getZ());
		Location spot = at.clone().add(normal[0] * SURFACE_OFFSET, normal[1] * SURFACE_OFFSET,
				normal[2] * SURFACE_OFFSET);
		spot.setDirection(new Vector(normal[0], normal[1], normal[2]));

		TextDisplay display = at.getWorld().spawn(spot, TextDisplay.class, entity -> {
			entity.setText("M");
			entity.setTextOpacity((byte) 0);
			entity.setBackgroundColor(COLOR);
			entity.setDefaultBackground(false);
			entity.setSeeThrough(false);
			entity.setShadowed(false);
			entity.setBrightness(new Display.Brightness(15, 15));
			entity.setBillboard(Display.Billboard.FIXED);
			entity.setAlignment(TextDisplay.TextAlignment.CENTER);
			entity.setTransformation(new Transformation(new Vector3f(), new Quaternionf(),
					new Vector3f(SIZE * WIDTH_SCALE, SIZE, SIZE), new Quaternionf()));
			entity.setViewRange(VIEW_RANGE);
			entity.setPersistent(false);
		});
		track(gun.getId(), display);
	}

	public void clear() {
		for (Deque<TextDisplay> tracked : impacts.values()) tracked.forEach(BulletImpacts::remove);
		impacts.clear();
	}

	private void track(String gunId, TextDisplay display) {
		Deque<TextDisplay> tracked = impacts.computeIfAbsent(gunId, id -> new ArrayDeque<>());
		tracked.addLast(display);
		while (tracked.size() > PewpewPlugin.getDefaultConfiguration().getImpactsMaxPerGun()) {
			remove(tracked.pollFirst());
		}

		plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
			tracked.remove(display);
			remove(display);
		}, LIFETIME_TICKS);
	}

	private static void remove(@Nullable TextDisplay display) {
		if (display != null && display.isValid()) display.remove();
	}
}
