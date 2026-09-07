package gg.deepsite.pewpew.modules.weapons.animation;

import gg.deepsite.pewpew.api.enums.AnimationEvent;
import gg.deepsite.pewpew.api.objects.PewpewAnimation;
import gg.deepsite.pewpew.api.objects.PewpewGunItem;
import gg.deepsite.pewpew.modules.items.ItemsModule;
import gg.deepsite.pewpew.modules.weapons.shooting.ScopeState;
import gg.deepsite.pewpew.utils.PersistentDataUtil;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AnimationManager {

	private static final class State {
		private final PewpewGunItem gun;
		private final PewpewAnimation animation;
		private final int slot;
		private int tick;
		private String model;

		private State(PewpewGunItem gun, PewpewAnimation animation, int slot) {
			this.gun = gun;
			this.animation = animation;
			this.slot = slot;
		}
	}

	private final Plugin plugin;
	private final Map<UUID, State> active = new ConcurrentHashMap<>();
	private BukkitTask task;

	public AnimationManager(@NotNull Plugin plugin) {
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

	public int play(@NotNull Player player, @NotNull PewpewGunItem gun, @NotNull AnimationEvent event,
	                int stretchToTicks) {
		PewpewAnimation animation = gun.getAnimation(event);
		if (animation == null) return 0;

		int length = animation.length();
		if (length <= 0) return 0;
		if (stretchToTicks > 0) animation = animation.scaled((double) stretchToTicks / length);

		active.remove(player.getUniqueId());
		State state = new State(gun, animation, player.getInventory().getHeldItemSlot());
		String first = animation.modelAt(0);
		if (first == null || !apply(player, state, first, animation.modelDataAt(0))) return 0;
		state.model = first;
		active.put(player.getUniqueId(), state);
		return animation.length();
	}

	public boolean isActive(@NotNull UUID id) {
		return active.containsKey(id);
	}

	public void cancel(@NotNull UUID id) {
		State state = active.remove(id);
		if (state == null) return;
		Player player = plugin.getServer().getPlayer(id);
		if (player != null) apply(player, state, null, 0);
	}

	private void tick() {
		if (active.isEmpty()) return;
		for (Map.Entry<UUID, State> entry : active.entrySet()) {
			Player player = plugin.getServer().getPlayer(entry.getKey());
			State state = entry.getValue();
			if (player == null) {
				active.remove(entry.getKey());
				continue;
			}

			state.tick++;
			String model = state.animation.modelAt(state.tick);
			if (model == null) {
				cancel(entry.getKey());
				continue;
			}
			if (model.equals(state.model)) continue;
			if (!apply(player, state, model, state.animation.modelDataAt(state.tick))) {
				active.remove(entry.getKey());
				continue;
			}
			state.model = model;
		}
	}

	private boolean apply(Player player, State state, @Nullable String frame, int frameData) {
		ItemStack stack = player.getInventory().getItem(state.slot);
		if (stack == null || !state.gun.getId().equals(PersistentDataUtil.getPewpew(stack, ItemsModule.PDC_KEY))) {
			return false;
		}
		GunModels.apply(stack, state.gun, ScopeState.isScoped(player), frame, frameData);
		player.getInventory().setItem(state.slot, stack);
		return true;
	}
}
