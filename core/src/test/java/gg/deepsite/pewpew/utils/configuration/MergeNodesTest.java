package gg.deepsite.pewpew.utils.configuration;

import org.junit.jupiter.api.Test;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.BufferedReader;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MergeNodesTest {

	private static ConfigurationNode parse(String yaml) throws Exception {
		return YamlConfigurationLoader.builder()
				.source(() -> new BufferedReader(new StringReader(yaml)))
				.build()
				.load();
	}

	@Test
	void unchangedConfigIsNotRewritten() throws Exception {
		ConfigurationNode live = parse("messages:\n  reloading: mine\n");
		ConfigurationNode defaults = parse("messages:\n  reloading: default\n");
		assertFalse(ConfigurateConfig.mergeNodes(live, defaults));
	}

	@Test
	void missingDefaultKeyIsAdded() throws Exception {
		ConfigurationNode live = parse("messages:\n  reloading: mine\n");
		ConfigurationNode defaults = parse("messages:\n  reloading: default\n  reloaded: default\n");
		assertTrue(ConfigurateConfig.mergeNodes(live, defaults));
		assertTrue(live.node("messages", "reloaded").getString("").equals("default"));
	}
}
