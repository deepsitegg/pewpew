package gg.deepsite.pewpew.utils;

import gg.deepsite.pewpew.api.enums.AnimationEvent;
import gg.deepsite.pewpew.api.objects.PewPewItem;
import gg.deepsite.pewpew.api.objects.PewpewGunItem;
import org.junit.jupiter.api.Test;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;

class WeaponDeserializerNoAnimationTest {

	private static PewpewGunItem gun(String extra) throws Exception {
		String yaml = """
				plain_gun:
				  type: GUN
				  name: "Plain"
				  itemModel: "minecraft:golden_horse_armor"
				  baseDamage: 5.0
				  fireRate: 4
				  reloadTime: 30
				  ammoType: pistol_9mm
				  maxAmmo: 10
				  range: 40.0
				  firingMode: HITSCAN
				""" + extra;
		ConfigurationNode root = YamlConfigurationLoader.builder()
				.source(() -> new BufferedReader(new StringReader(yaml)))
				.build()
				.load();
		List<PewPewItem> items = WeaponDeserializer.deserializeAll("guns.yml", root);
		return assertInstanceOf(PewpewGunItem.class, items.get(0));
	}

	@Test
	void aGunWithoutAnimationBlocksNeverOffersOne() throws Exception {
		PewpewGunItem plain = gun("");

		assertNull(plain.getAnimations(), "no animations map at all");
		assertNull(plain.getRigs(), "no rigs map at all");
		for (AnimationEvent event : AnimationEvent.values()) {
			assertNull(plain.getAnimation(event), "no frame animation for " + event);
			assertNull(plain.getRig(event), "no rig for " + event);
		}
	}

	@Test
	void animationCooldownIsOnByDefaultAndCanBeTurnedOff() throws Exception {
		assertTrue(gun("").isAnimationCooldown(), "a gun locks itself while animating by default");
		assertFalse(gun("""
				  animationCooldown: false
				""").isAnimationCooldown(), "and it can be opted out");
	}

	@Test
	void anUnusableAnimationBlockStaysInert() throws Exception {
		PewpewGunItem broken = gun("""
				  animations:
				    fire:
				      - model: "no-namespace-here"
				        ticks: 2
				""");

		for (AnimationEvent event : AnimationEvent.values()) {
			assertNull(broken.getAnimation(event), "bad config must not half-enable " + event);
		}
	}
}
