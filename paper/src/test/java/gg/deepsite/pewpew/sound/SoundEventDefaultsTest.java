package gg.deepsite.pewpew.sound;

import gg.deepsite.pewpew.api.enums.SoundEvent;
import gg.deepsite.pewpew.api.objects.PewpewSound;
import gg.deepsite.pewpew.utils.WeaponDeserializer;
import org.junit.jupiter.api.Test;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SoundEventDefaultsTest {

	private static ConfigurationNode bundled() throws Exception {
		try (InputStream in = java.nio.file.Files.newInputStream(Path.of("src/main/resources/sounds.yml"))) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			return YamlConfigurationLoader.builder().source(() -> reader).build().load();
		}
	}

	@Test
	void everyEventHasAParsableFallback() {
		for (SoundEvent event : SoundEvent.values()) {
			PewpewSound fallback = event.getFallback();
			assertNotNull(fallback, event.name());
			assertEquals("minecraft", fallback.key().namespace(), event.name());
		}
	}

	@Test
	void bundledSoundsYamlDefinesEveryEventAtTheRightPath() throws Exception {
		ConfigurationNode root = bundled();
		for (SoundEvent event : SoundEvent.values()) {
			ConfigurationNode node = root.node("sounds");
			for (String segment : event.getPath().split("[.]")) node = node.node(segment);
			assertFalse(node.virtual(), "sounds.yml is missing 'sounds." + event.getPath() + "'");

			List<PewpewSound> parsed = WeaponDeserializer.parseSounds("sounds.yml", event.getPath(), node);
			assertNotNull(parsed, event.getPath());
			assertEquals(List.of(event.getFallback()), parsed,
					"sounds.yml default for '" + event.getPath() + "' drifted from the code fallback");
		}
	}
}
