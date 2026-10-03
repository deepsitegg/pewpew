package gg.deepsite.pewpew.utils;

import org.junit.jupiter.api.Test;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class WeaponDeserializerColorTest {

	private static Integer color(Object raw) throws Exception {
		ConfigurationNode node = BasicConfigurationNode.root();
		if (raw != null) node.node("color").set(raw);
		return WeaponDeserializer.parseColor("test.yml", "item", node);
	}

	@Test
	void parsesHexAndRgb() throws Exception {
		assertEquals(0xFF8000, color("#FF8000"));
		assertEquals(0xFF8000, color("ff8000"));
		assertEquals(0xFF8000, color("255, 128, 0"));
		assertNull(color(null));
		assertNull(color("256, 0, 0"));
		assertNull(color("#FFF"));
		assertNull(color("red"));
	}
}
