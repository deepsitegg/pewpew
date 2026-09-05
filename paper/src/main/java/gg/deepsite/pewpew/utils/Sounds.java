package gg.deepsite.pewpew.utils;

import gg.deepsite.pewpew.PewpewPlugin;
import gg.deepsite.pewpew.api.enums.SoundEvent;
import gg.deepsite.pewpew.api.objects.PewpewSound;
import lombok.experimental.UtilityClass;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@UtilityClass
public class Sounds {

	@Nullable
	private static List<PewpewSound> get(SoundEvent event) {
		return PewpewPlugin.getSoundsConfig() == null
				? List.of(event.getFallback())
				: PewpewPlugin.getSoundsConfig().get(event);
	}

	public static void at(@NotNull World world, @NotNull Location location, @NotNull SoundEvent event) {
		at(world, location, event, 1.0f);
	}

	public static void at(@NotNull World world, @NotNull Location location, @NotNull SoundEvent event,
	                      float pitchScale) {
		List<PewpewSound> sounds = get(event);
		if (sounds == null) return;
		for (PewpewSound sound : sounds) {
			world.playSound(sound.adventure(pitchScale), location.getX(), location.getY(), location.getZ());
		}
	}

	public static void at(@NotNull Player player, @NotNull SoundEvent event) {
		at(player, event, 1.0f);
	}

	public static void at(@NotNull Player player, @NotNull SoundEvent event, float pitchScale) {
		at(player.getWorld(), player.getLocation(), event, pitchScale);
	}

	public static void to(@NotNull Player player, @NotNull SoundEvent event) {
		List<PewpewSound> sounds = get(event);
		if (sounds == null) return;
		for (PewpewSound sound : sounds) player.playSound(sound.adventure());
	}
}
