package gg.deepsite.pewpew.modules.weapons.animation;

import gg.deepsite.pewpew.api.objects.PewpewRig;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class RigAnimator {

	private static final float DEG = (float) (Math.PI / 180.0);

	private static final float VIEWMODEL_Y_OFFSET = -2500f;

	private static final class Rigged {
		private final PewpewRig rig;
		private final List<ItemDisplay> displays = new ArrayList<>();
		private int tick = -1;

		private Rigged(PewpewRig rig) {
			this.rig = rig;
		}
	}

	private final Plugin plugin;
	private final Map<UUID, Rigged> active = new ConcurrentHashMap<>();
	private BukkitTask task;

	public RigAnimator(@NotNull Plugin plugin) {
		this.plugin = plugin;
	}

	public void start() {
		if (task == null) task = plugin.getServer().getScheduler().runTaskTimer(plugin, this::tick, 1L, 1L);
	}

	public void stop() {
		if (task != null) {
			task.cancel();
			task = null;
		}
		for (UUID id : Map.copyOf(active).keySet()) cancel(id);
	}

	public int play(@NotNull Player player, @NotNull PewpewRig rig, int stretchToTicks) {
		int length = rig.length();
		if (length <= 0 || rig.parts().isEmpty()) return 0;
		if (stretchToTicks > 0) {
			rig = rig.scaled((double) stretchToTicks / length);
		}

		cancel(player.getUniqueId());

		Rigged rigged = new Rigged(rig);
		boolean viewmodel = rig.viewmodel();
		Display.Billboard billboard = billboardOf(rig.billboard());
		Location at = viewmodel ? player.getEyeLocation() : player.getLocation();
		for (PewpewRig.Part part : rig.parts()) {
			ItemDisplay display = player.getWorld().spawn(at, ItemDisplay.class, spawned -> {
				spawned.setItemStack(modelStack(part.itemModel()));
				spawned.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.FIXED);
				spawned.setBillboard(billboard);
				spawned.setInterpolationDelay(0);
				spawned.setViewRange(2.0f);
				spawned.setPersistent(false);
			});
			apply(display, part.keyframes().get(0), 0, viewmodel);
			rigged.displays.add(display);
		}
		if (!viewmodel) {
			for (ItemDisplay display : rigged.displays) player.addPassenger(display);
		}

		active.put(player.getUniqueId(), rigged);
		return rig.length();
	}

	public void cancel(@NotNull UUID id) {
		Rigged rigged = active.remove(id);
		if (rigged == null) return;
		for (ItemDisplay display : rigged.displays) {
			if (!display.isDead()) display.remove();
		}
	}

	private void tick() {
		if (active.isEmpty()) return;
		for (Map.Entry<UUID, Rigged> entry : active.entrySet()) {
			Player player = plugin.getServer().getPlayer(entry.getKey());
			Rigged rigged = entry.getValue();
			if (player == null) {
				cancel(entry.getKey());
				continue;
			}

			rigged.tick++;
			if (rigged.tick > rigged.rig.length()) {
				cancel(entry.getKey());
				continue;
			}

			if (rigged.rig.viewmodel()) {
				Location eye = player.getEyeLocation();
				for (ItemDisplay display : rigged.displays) display.teleport(eye);
			}

			List<PewpewRig.Part> parts = rigged.rig.parts();
			for (int i = 0; i < parts.size() && i < rigged.displays.size(); i++) {
				PewpewRig.Part part = parts.get(i);
				if (part.at(rigged.tick) == null) continue;
				PewpewRig.Keyframe next = part.after(rigged.tick);
				if (next == null) continue;
				apply(rigged.displays.get(i), next, part.durationAfter(rigged.tick), rigged.rig.viewmodel());
			}
		}
	}

	private static void apply(ItemDisplay display, PewpewRig.Keyframe frame, int duration, boolean viewmodel) {
		float[] rotation = frame.rotation();
		Quaternionf left = new Quaternionf().rotateY(rotation[1] * DEG)
				.rotateX(rotation[0] * DEG)
				.rotateZ(rotation[2] * DEG);
		display.setInterpolationDelay(0);
		display.setInterpolationDuration(Math.max(0, duration));
		float y = frame.translation()[1] + (viewmodel ? VIEWMODEL_Y_OFFSET : 0f);
		display.setTransformation(new Transformation(
				new Vector3f(frame.translation()[0], y, frame.translation()[2]),
				left,
				new Vector3f(frame.scale()[0], frame.scale()[1], frame.scale()[2]),
				new Quaternionf()));
	}

	@NotNull
	private static Display.Billboard billboardOf(@NotNull String name) {
		try {
			return Display.Billboard.valueOf(name);
		} catch (IllegalArgumentException e) {
			return Display.Billboard.FIXED;
		}
	}

	@NotNull
	private static ItemStack modelStack(@NotNull String itemModel) {
		ItemStack stack = new ItemStack(Material.PAPER);
		NamespacedKey key = NamespacedKey.fromString(itemModel);
		if (key != null) stack.editMeta(meta -> meta.setItemModel(key));
		return stack;
	}
}
