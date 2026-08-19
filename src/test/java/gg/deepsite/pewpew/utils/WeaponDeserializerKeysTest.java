package gg.deepsite.pewpew.utils;

import gg.deepsite.pewpew.api.enums.ItemType;
import org.junit.jupiter.api.Test;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WeaponDeserializerKeysTest {

	private static ConfigurationNode parse(String yaml) throws Exception {
		return YamlConfigurationLoader.builder()
				.source(() -> new BufferedReader(new StringReader(yaml)))
				.build()
				.load();
	}

	@Test
	void acceptsACompleteGun() throws Exception {
		ConfigurationNode gun = parse("""
				type: GUN
				name: "AK-47"
				itemModel: "minecraft:iron_horse_armor"
				maxStack: 1
				hideItemFlags: true
				baseDamage: 7.0
				fireRate: 2.0
				spread: 2.0
				bulletCount: 1
				damageType: minecraft:arrow
				recoilProfile:
				  verticalMean: 1.0
				""");
		assertEquals(List.of(), WeaponDeserializer.unknownKeys(ItemType.GUN, gun));
	}

	@Test
	void flagsMisspelledFields() throws Exception {
		ConfigurationNode gun = parse("""
				type: GUN
				name: "AK-47"
				itemModel: "minecraft:iron_horse_armor"
				bulletcount: 8
				spred: 2.0
				""");
		List<String> unknown = WeaponDeserializer.unknownKeys(ItemType.GUN, gun);
		assertEquals(2, unknown.size(), "both typos must be reported");
		assertTrue(unknown.contains("bulletcount"));
		assertTrue(unknown.contains("spred"));
	}

	@Test
	void doesNotLookInsideNestedBlocks() throws Exception {
		ConfigurationNode gun = parse("""
				type: GUN
				fireSound:
				  - key: "minecraft:entity.generic.explode"
				    volume: 1.0
				""");
		assertEquals(List.of(), WeaponDeserializer.unknownKeys(ItemType.GUN, gun));
	}

	@Test
	void keysAreScopedToTheItemType() throws Exception {
		ConfigurationNode ammo = parse("""
				type: AMMO
				name: "Rifle round"
				ammoType: rifle_round
				roundsPerItem: 1
				spread: 2.0
				""");
		assertEquals(List.of("spread"), WeaponDeserializer.unknownKeys(ItemType.AMMO, ammo),
				"a gun field on an ammo item is a mistake worth reporting");
	}
}
