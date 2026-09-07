package gg.deepsite.pewpew.utils;

import gg.deepsite.pewpew.api.enums.AnimationEvent;
import gg.deepsite.pewpew.api.enums.HoldPose;
import gg.deepsite.pewpew.api.enums.ItemType;
import gg.deepsite.pewpew.api.objects.PewPewItem;
import gg.deepsite.pewpew.api.objects.PewpewAnimation;
import gg.deepsite.pewpew.api.objects.PewpewGunItem;
import org.junit.jupiter.api.Test;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

class WeaponDeserializerAnimationsTest {

	private static final String TEST_GUN = """
			anim_test:
			  type: GUN
			  name: "Animation Test Rifle"
			  itemModel: "minecraft:golden_horse_armor"
			  holdPose: CROSSBOW
			  baseDamage: 2.0
			  fireRate: 10
			  reloadTime: 60
			  ammoType: pistol_9mm
			  maxAmmo: 5
			  range: 40.0
			  firingMode: HITSCAN
			  animations:
			    fire:
			      - model: "minecraft:blaze_rod"
			        ticks: 3
			      - model: "minecraft:stick"
			        ticks: 3
			    scope-in:
			      - model: "minecraft:spyglass"
			        ticks: 10
			    reload:
			      model: "mypack:ak_reload_"
			      frames: 3
			      ticks: 2
			""";

	private static ConfigurationNode parse(String yaml) throws Exception {
		return YamlConfigurationLoader.builder()
				.source(() -> new BufferedReader(new StringReader(yaml)))
				.build()
				.load();
	}

	@Test
	void parsesTheTestGunFromYaml() throws Exception {
		List<PewPewItem> items = WeaponDeserializer.deserializeAll("guns.yml", parse(TEST_GUN));

		assertEquals(1, items.size());
		PewpewGunItem gun = assertInstanceOf(PewpewGunItem.class, items.get(0));
		assertEquals(HoldPose.CROSSBOW, gun.getHoldPose());

		PewpewAnimation fire = gun.getAnimation(AnimationEvent.FIRE);
		assertEquals(6, fire.length());
		assertEquals("minecraft:blaze_rod", fire.modelAt(0));
		assertEquals("minecraft:stick", fire.modelAt(3));

		PewpewAnimation scopeIn = gun.getAnimation(AnimationEvent.SCOPE_IN);
		assertEquals(10, scopeIn.length());
		assertEquals("minecraft:spyglass", scopeIn.modelAt(9));

		PewpewAnimation reload = gun.getAnimation(AnimationEvent.RELOAD);
		assertEquals(6, reload.length());
		assertEquals("mypack:ak_reload_1", reload.modelAt(0));
		assertEquals("mypack:ak_reload_3", reload.modelAt(5));

		assertNull(gun.getAnimation(AnimationEvent.RELOAD_ROUND));
	}

	@Test
	void animationsAndHoldPoseAreKnownFields() throws Exception {
		ConfigurationNode gun = parse(TEST_GUN).node("anim_test");
		assertEquals(List.of(), WeaponDeserializer.unknownKeys(ItemType.GUN, gun));
	}
}
