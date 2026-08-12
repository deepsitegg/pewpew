package gg.deepsite.pewpew.api.objects;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public record PewpewSound(@NotNull Key key, float volume, float pitch) {

	@NotNull
	public static PewpewSound of(@NotNull String raw, float volume, float pitch) {
		String normalized = raw.contains(":")
				? raw.toLowerCase()
				: "minecraft:" + raw.toLowerCase().replace('_', '.');
		return new PewpewSound(Key.key(normalized), volume, pitch);
	}

	private Sound adventure() {
		return adventure(1.0f);
	}

	private Sound adventure(float pitchScale) {
		return Sound.sound(key, Sound.Source.MASTER, volume, pitch * pitchScale);
	}

	public void play(@NotNull Player player) {
		player.playSound(adventure());
	}

	public void playAt(@NotNull Location location) {
		playAt(location, 1.0f);
	}

	public void playAt(@NotNull Location location, float pitchScale) {
		if (location.getWorld() != null) {
			location.getWorld().playSound(adventure(pitchScale), location.getX(), location.getY(), location.getZ());
		}
	}
}
