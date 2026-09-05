package gg.deepsite.pewpew.configuration;

import gg.deepsite.pewpew.api.enums.SoundEvent;
import gg.deepsite.pewpew.api.objects.PewpewSound;
import gg.deepsite.pewpew.utils.WeaponDeserializer;
import gg.deepsite.pewpew.utils.configuration.ConfigurateConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;

import java.io.File;
import java.util.List;

public class SoundsConfig extends ConfigurateConfig {

	public SoundsConfig(File file) {
		super(file, "sounds.yml", "sounds.yml", true);
	}

	@Nullable
	public List<PewpewSound> get(@NotNull SoundEvent event) {
		if (getRootNode() == null) return List.of(event.getFallback());

		ConfigurationNode node = getRootNode().node("sounds");
		for (String segment : event.getPath().split("[.]")) node = node.node(segment);

		if (node.virtual()) return List.of(event.getFallback());
		if (node.isNull() || "none".equalsIgnoreCase(node.getString(""))) return null;

		List<PewpewSound> sounds = WeaponDeserializer.parseSounds("sounds.yml", event.getPath(), node);
		return sounds == null ? List.of(event.getFallback()) : sounds;
	}
}
