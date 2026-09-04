package gg.deepsite.pewpew.api.objects;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.jetbrains.annotations.NotNull;

public record PewpewSound(@NotNull Key key, float volume, float pitch) {

	@NotNull
	public static PewpewSound of(@NotNull String raw, float volume, float pitch) {
		String normalized = raw.contains(":")
				? raw.toLowerCase()
				: "minecraft:" + raw.toLowerCase().replace('_', '.');
		return new PewpewSound(Key.key(normalized), volume, pitch);
	}

	@NotNull
	public Sound adventure() {
		return adventure(1.0f);
	}

	@NotNull
	public Sound adventure(float pitchScale) {
		return Sound.sound(key, Sound.Source.MASTER, volume, pitch * pitchScale);
	}
}
